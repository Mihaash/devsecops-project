package com.myapp.service.car;

import com.myapp.dataaccessobject.CarRepository;
import com.myapp.domainobject.CarDO;
import com.myapp.domainobject.DriverDO;
import com.myapp.domainvalue.EngineType;
import com.myapp.domainvalue.OnlineStatus;
import com.myapp.exception.CarAlreadyInUseException;
import com.myapp.exception.ConstraintsViolationException;
import com.myapp.exception.EntityNotFoundException;
import com.myapp.service.driver.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private DriverService driverService;

    private DefaultCarService carService;

    private static final String LICENSE_PLATE = "XYZ-123";
    private static final Long DRIVER_ID = 1L;

    @BeforeEach
    void setUp() {
        carService = new DefaultCarService(carRepository);
        carService.setDriverService(driverService);
    }

    @Test
    void find_Success() throws EntityNotFoundException {
        CarDO carDO = new CarDO(LICENSE_PLATE, 4, false, 5, EngineType.GAS, "BMW");
        when(carRepository.findByLicensePlate(LICENSE_PLATE)).thenReturn(carDO);

        CarDO result = carService.find(LICENSE_PLATE);

        assertEquals(carDO, result);
        verify(carRepository).findByLicensePlate(LICENSE_PLATE);
    }

    @Test
    void find_NotFound() {
        when(carRepository.findByLicensePlate(LICENSE_PLATE)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> carService.find(LICENSE_PLATE));
    }

    @Test
    void create_Success() throws ConstraintsViolationException {
        CarDO carDO = new CarDO(LICENSE_PLATE, 4, false, 5, EngineType.GAS, "BMW");
        when(carRepository.save(carDO)).thenReturn(carDO);

        CarDO result = carService.create(carDO);

        assertEquals(carDO, result);
        verify(carRepository).save(carDO);
    }

    @Test
    void create_ConstraintViolation() {
        CarDO carDO = new CarDO(LICENSE_PLATE, 4, false, 5, EngineType.GAS, "BMW");
        when(carRepository.save(carDO)).thenThrow(new DataIntegrityViolationException("error"));

        assertThrows(ConstraintsViolationException.class, () -> carService.create(carDO));
    }

    @Test
    void delete_Success() throws EntityNotFoundException, ConstraintsViolationException {
        CarDO carDO = new CarDO(LICENSE_PLATE, 4, false, 5, EngineType.GAS, "BMW");
        when(carRepository.findByLicensePlate(LICENSE_PLATE)).thenReturn(carDO);

        carService.delete(LICENSE_PLATE);

        assertTrue(carDO.getDeleted());
    }

    @Test
    void addDriver_Success() throws Exception {
        CarDO carDO = new CarDO(LICENSE_PLATE, 4, false, 5, EngineType.GAS, "BMW");
        DriverDO driverDO = new DriverDO("driver1", "pass1");
        driverDO.setId(DRIVER_ID);
        driverDO.setOnlineStatus(OnlineStatus.ONLINE);

        when(carRepository.findByLicensePlate(LICENSE_PLATE)).thenReturn(carDO);
        when(driverService.find(DRIVER_ID)).thenReturn(driverDO);
        when(carRepository.save(carDO)).thenReturn(carDO);

        carService.addDriver(DRIVER_ID, LICENSE_PLATE);

        assertEquals(driverDO, carDO.getDriver());
        verify(driverService).updateCar(DRIVER_ID, carDO);
    }

    @Test
    void addDriver_CarAlreadyInUse() throws EntityNotFoundException {
        CarDO carDO = new CarDO(LICENSE_PLATE, 4, false, 5, EngineType.GAS, "BMW");
        carDO.setDriver(new DriverDO());
        when(carRepository.findByLicensePlate(LICENSE_PLATE)).thenReturn(carDO);
        when(driverService.find(DRIVER_ID)).thenReturn(new DriverDO());

        assertThrows(CarAlreadyInUseException.class, () -> carService.addDriver(DRIVER_ID, LICENSE_PLATE));
    }

    @Test
    void addDriver_DriverOffline() throws EntityNotFoundException {
        CarDO carDO = new CarDO(LICENSE_PLATE, 4, false, 5, EngineType.GAS, "BMW");
        DriverDO driverDO = new DriverDO("driver1", "pass1");
        driverDO.setOnlineStatus(OnlineStatus.OFFLINE);

        when(carRepository.findByLicensePlate(LICENSE_PLATE)).thenReturn(carDO);
        when(driverService.find(DRIVER_ID)).thenReturn(driverDO);

        assertThrows(ConstraintsViolationException.class, () -> carService.addDriver(DRIVER_ID, LICENSE_PLATE));
    }

    @Test
    void deleteDriver_Success() throws Exception {
        CarDO carDO = new CarDO(LICENSE_PLATE, 4, false, 5, EngineType.GAS, "BMW");
        DriverDO driverDO = new DriverDO("driver1", "pass1");
        driverDO.setId(DRIVER_ID);
        carDO.setDriver(driverDO);

        when(carRepository.findByLicensePlate(LICENSE_PLATE)).thenReturn(carDO);
        when(driverService.find(DRIVER_ID)).thenReturn(driverDO);

        carService.deleteDriver(DRIVER_ID, LICENSE_PLATE);

        assertNull(carDO.getDriver());
        verify(driverService).updateCar(DRIVER_ID, null);
    }

    @Test
    void deleteDriver_NoDriver() throws EntityNotFoundException {
        CarDO carDO = new CarDO(LICENSE_PLATE, 4, false, 5, EngineType.GAS, "BMW");
        when(carRepository.findByLicensePlate(LICENSE_PLATE)).thenReturn(carDO);
        when(driverService.find(DRIVER_ID)).thenReturn(new DriverDO());

        assertThrows(ConstraintsViolationException.class, () -> carService.deleteDriver(DRIVER_ID, LICENSE_PLATE));
    }

    @Test
    void deleteDriver_WrongDriver() throws EntityNotFoundException {
        CarDO carDO = new CarDO(LICENSE_PLATE, 4, false, 5, EngineType.GAS, "BMW");
        DriverDO driver1 = new DriverDO();
        driver1.setId(1L);
        DriverDO driver2 = new DriverDO();
        driver2.setId(2L);
        carDO.setDriver(driver1);

        when(carRepository.findByLicensePlate(LICENSE_PLATE)).thenReturn(carDO);
        when(driverService.find(2L)).thenReturn(driver2);

        assertThrows(ConstraintsViolationException.class, () -> carService.deleteDriver(2L, LICENSE_PLATE));
    }

    @Test
    void findAll_Success() {
        List<CarDO> cars = Collections.singletonList(new CarDO());
        when(carRepository.findAll()).thenReturn(cars);

        Iterable<CarDO> result = carService.findAll();

        assertEquals(cars, result);
    }
}
