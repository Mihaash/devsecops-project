package com.myapp;

import com.myapp.controller.CarController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = MyappServer.class)
class MyappServerTests
{
    @Autowired
    private CarController carController;

    @Test
    void contextLoads()
    {
        assertNotNull(carController);
    }

}
