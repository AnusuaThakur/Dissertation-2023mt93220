package com.dissertation.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataModificationDetectionService {
	private static final Logger logger = LoggerFactory.getLogger(DataModificationDetectionService.class);

    public String generateHash(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public Map<String, String> getChangedFields(Map<String, String> oldData, Map<String, String> newData) {
        Map<String, String> changedFields = new HashMap<>();
        try {
            for (String key : newData.keySet()) {
                String newValue = newData.getOrDefault(key, "");
                
                String oldValue = oldData.getOrDefault(key, "");
                String oldHash = generateHash(oldValue);
                logger.info("Last Submitted hash value: {}", oldHash);
                String newHash = generateHash(newValue);
                logger.info("New hash value: {}", newHash);
                
                logger.info("Last Submitted Data: {}", oldValue);
                logger.info("Checking field '{}': old='{}', new='{}'", key, oldValue, newValue);

                if (!generateHash(newValue).equals(generateHash(oldValue))) {
                    changedFields.put(key, newValue);  // Add only modified fields
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return changedFields;
    }
}
