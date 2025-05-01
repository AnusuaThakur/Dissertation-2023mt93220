package com.dissertation.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserDataService {

	private static final Logger logger = LoggerFactory.getLogger(UserDataService.class);

	// Store user data per user ID
	private Map<Long, Map<String, String>> userSubmittedData = new ConcurrentHashMap<>();
	private long totalDataSize = 0;
	private long startTime = 0;
	private long lastSubmissionTime = 0;
	private int transmittedSize = 0;
	private Map<Long, Integer> lastChangedFieldCount = new ConcurrentHashMap<>();
	private Map<Long, String> lastModifiedFields = new ConcurrentHashMap<>(); // Stores modified field names
	private Map<Long, Integer> userTransmittedSize = new ConcurrentHashMap<>();
	private Long lastActiveUserId;
	private boolean optimizationEnabled=true;

	// Add user data for a specific user
	public void addUserData(Long userId, Map<String, String> newData) {
		startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		if (startTime == 0) {
			startTime = endTime;
		}
		lastSubmissionTime = endTime;

		// Retrieve last submitted data for the user (if exists)
		Map<String, String> lastSubmittedData = userSubmittedData.getOrDefault(userId, new HashMap<>());

		// Find changed fields

		Map<String, String> changedFields = newData.entrySet().stream()
				.filter(entry -> !entry.getValue().equals(lastSubmittedData.get(entry.getKey())))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		logger.info("Transmitted Data: {}", changedFields);
		int changedFieldCount = changedFields.size();

		lastChangedFieldCount.put(userId, changedFieldCount);
		logger.info("DEBUG: Updated changed field count for {}: {}", userId, lastChangedFieldCount.get(userId));
		if(optimizationEnabled) {
		transmittedSize = changedFields.values().stream().mapToInt(val -> val.getBytes().length).sum();
		}
		else {
			transmittedSize = newData.values().stream().mapToInt(val -> val.getBytes().length).sum();
		}
		userTransmittedSize.put(userId, transmittedSize);

		String modifiedFieldsStr = String.join(", ", changedFields.keySet());
		lastModifiedFields.put(userId, modifiedFieldsStr);

		logger.info("User ID: {}", userId);
		logger.info("Previous Data: {}", lastSubmittedData);
		logger.info("New Data Submitted: {}", newData);
		logger.info("Changed Fields: {}", changedFields);
		logger.info("Total transmitted data size in KB ::" + transmittedSize);

		if (!changedFields.isEmpty()) {
			logger.info("Transmitted Data: Only the modified fields --> {}", changedFields);
			logger.info("Modified Fields for {}: {}", userId, modifiedFieldsStr);
		} else {
			logger.info("No changes detected. No data transmitted.");
			lastModifiedFields.put(userId, "No Changes");
		}

		// Update stored data per user
		userSubmittedData.put(userId, new HashMap<>(newData));
		lastSubmissionTime = System.currentTimeMillis();

		totalDataSize += newData.values().stream().mapToInt(String::length).sum();

		logger.info("Bandwidth Saved: {} bytes (compared to full form submission)", totalDataSize - transmittedSize);
		long latency = getLatency();
		setLastActiveUserId(userId);
	}

	// Retrieve last submitted data for a specific user
	public Map<String, String> getLastSubmittedData(Long userId) {
		return new HashMap<>(userSubmittedData.getOrDefault(userId, new HashMap<>()));
	}

	public long getNetworkLoad() {
		return transmittedSize % 100;
	}

	public double getThroughput() {
		long latency = getLatency();
		double dataSizeKB = transmittedSize / 1024.0;
		return (latency == 0) ? 0.0 : (dataSizeKB / latency);
	}

	public long getLatency() {
		return lastSubmissionTime - startTime;
	}

	// Retrieve all user-submitted data
	public Map<Long, Map<String, String>> getAllUserSubmittedData() {
		return userSubmittedData;
	}

	public int getLastChangedFieldCount(Long userId) {
		return lastChangedFieldCount.get(userId);
	}

	// Retrieve change counts for all users
	public int getAllChangedFieldCounts() {
		return lastChangedFieldCount.values().stream().mapToInt(Integer::intValue).sum();
	}

	public void resetUserFieldCount(Long userId) {
		lastChangedFieldCount.put(userId, 0);
	}

	public String getModifiedFields(Long userId) {
		return lastModifiedFields.getOrDefault(userId, "No changes");
	}

	public long getNetworkLoadForUser(Long userId) {
		Integer size = userTransmittedSize.get(userId);
		return size % 100;
	}
	public void setLastActiveUserId(Long userId) {
	    this.lastActiveUserId = userId;
	}

	public Long getLastActiveUserId() {
	    return lastActiveUserId;
	}
}
