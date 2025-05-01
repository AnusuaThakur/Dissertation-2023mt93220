package com.dissertation.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

//This service will handle the transmission of the data (the process of "sending" the data after modification detection).

@Service
public class DataTransmissionService {
	private static final Logger logger = LoggerFactory.getLogger(DataTransmissionService.class);

    public void transmitData(String data) {
    	logger.info("Transmitting data: {}", data);
    	
          	try {
            // Code to transmit the data
            Thread.sleep(500);  // Simulate delay
            logger.info("Data transmitted successfully: {}", data);
        } catch (InterruptedException e) {
            logger.error("Error during data transmission", e);
        }
    }
}

