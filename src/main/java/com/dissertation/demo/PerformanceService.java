package com.dissertation.demo;

import org.springframework.stereotype.Service;

@Service
public class PerformanceService {

    public void logTransmissionDetails(String data, long startTime, long endTime) {
        long latency = endTime - startTime;
    }

    public void logThroughput(long dataSize, long duration) {
        long throughput = dataSize / duration;
    }
}
