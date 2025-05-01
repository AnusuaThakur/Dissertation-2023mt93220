package com.dissertation.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dissertation.demo.login.User;
import com.dissertation.demo.login.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class UserUIController {
	private static final Logger logger = LoggerFactory.getLogger(UserUIController.class);

	@Autowired
	private DataModificationDetectionService dataModificationDetectionService;

	@Autowired
	private DataTransmissionService dataTransmissionService;

	@Autowired
	private UserDataService userDataService;
	@Autowired
	private UserService userService;

	@GetMapping("/user-ui")
	public String showUserForm(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		if (user == null) {
			
			return "redirect:/login";
		}
		model.addAttribute("userId", user.getId());
		model.addAttribute("name", user.getName());
		model.addAttribute("email", user.getEmail());
		model.addAttribute("phone", user.getPhone());
		model.addAttribute("address", user.getAddress());
		model.addAttribute("dob", user.getDob());
		return "userForm";
	}

	@PostMapping("/user-ui/submit")
	public String submitUserData(@RequestParam Map<String, String> formData, HttpSession session, Model model) {
		long startTime = System.currentTimeMillis();

		// Retrieve the logged-in user from the session
		User user = (User) session.getAttribute("user");
		if (user == null) {
			model.addAttribute("message", "User not logged in!");
			return "redirect:/login"; // Redirect to login if not authenticated
		}
		logger.info("User ID: " + user.getId() + " - Data Received: " + formData);
		// Fetch user from DB to ensure correct email comparison
		User existingUser = userService.getUserById(user.getId());
		Long userId = user.getId(); // Get user ID

		// Retrieve last submitted data for this specific user
		Map<String, String> lastData = userDataService.getLastSubmittedData(userId);

		if (lastData == null || lastData.isEmpty()) {
			lastData = new ConcurrentHashMap<>();
			lastData.put("name", existingUser.getName() != null ? existingUser.getName() : "");
			lastData.put("email", existingUser.getEmail() != null ? existingUser.getEmail() : "");
			lastData.put("phone", existingUser.getPhone() != null ? existingUser.getPhone() : "");
			lastData.put("address", existingUser.getAddress() != null ? existingUser.getAddress() : "");
			lastData.put("dob", existingUser.getDob() != null ? existingUser.getDob() : "");
			userDataService.addUserData(userId, lastData);
		}
		
		// Detect changes
		Map<String, String> changedFields = dataModificationDetectionService.getChangedFields(lastData, formData);

		int changedFieldCount = changedFields.size();
		model.addAttribute("lastChangedFieldCount", changedFieldCount);
		if (!changedFields.isEmpty()) {
			dataTransmissionService.transmitData("User ID: " + userId + " -> " + changedFields.toString()); // Log with
																											// user ID
			userDataService.addUserData(userId, formData); // Store the latest data for this user
			model.addAttribute("message",
					"Data Transmitted for User ID: " + userId + " (Modified Fields: " + changedFields.keySet() + ")");
		} else {
			userDataService.addUserData(userId, formData);
			userDataService.getModifiedFields(userId);
			userDataService.resetUserFieldCount(userId);
			model.addAttribute("message", "No Change in Data for User ID: " + userId);
			logger.info("No changes detected");
		}

		user = userService.updateUserProfile(user.getId(), formData.getOrDefault("name", ""),
				formData.getOrDefault("phone", ""), formData.getOrDefault("address", ""),
				formData.getOrDefault("dob", ""));
		session.setAttribute("user", user);

		model.addAttribute("name", formData.getOrDefault("name", ""));
		model.addAttribute("email", formData.getOrDefault("email", ""));
		model.addAttribute("phone", formData.getOrDefault("phone", ""));
		model.addAttribute("address", formData.getOrDefault("address", ""));
		model.addAttribute("dob", formData.getOrDefault("dob", ""));
		return "userForm";
	}

}
