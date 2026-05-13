package com.myapp.service.driver;

import com.myapp.domainobject.CarDO;
import com.myapp.domainobject.DriverDO;
import com.myapp.domainvalue.OnlineStatus;
import com.myapp.exception.ConstraintsViolationException;
import com.myapp.exception.EntityNotFoundException;
import java.util.List;

public interface DriverService
{

    DriverDO find(Long driverId) throws EntityNotFoundException;

    DriverDO create(DriverDO driverDO) throws ConstraintsViolationException;

    void delete(Long driverId) throws EntityNotFoundException;

    void updateLocation(long driverId, double longitude, double latitude) throws EntityNotFoundException;

    void updateCar(long driverId, CarDO carDO) throws EntityNotFoundException;

    List<DriverDO> find(OnlineStatus onlineStatus);

    Iterable<DriverDO> findAll();

    List<DriverDO> findByUsername(String username);

}
