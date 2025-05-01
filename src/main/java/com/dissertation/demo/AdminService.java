package com.dissertation.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class AdminService {
	
	private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
	
	@Autowired
    private UserDataService userDataService;


    public String getNetworkMetrics() {
    	 try {
    	        Thread.sleep(500); // Simulate a delay of 0.5 seconds
    	    } catch (InterruptedException e) {
    	        e.printStackTrace();
    	    }
        long load = userDataService.getNetworkLoad();  // Get network load (data size % 100)
        long latency = userDataService.getLatency();  // Get latency (time taken for submissions)
        double throughput = userDataService.getThroughput();  // Get throughput (size / time)
        

        logger.info("Network Metrics - Load: {}%, Latency: {} ms, Throughput: {} KB/s", load, latency, throughput);
        return String.format("Network Load: %d%%, Throughput: %.3f KB/s, Latency: %d ms", load, throughput, latency);
    }
}

