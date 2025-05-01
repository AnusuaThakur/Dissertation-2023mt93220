package com.dissertation.demo.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {
	@Autowired
	private UserService userService;

	@PostMapping("/login")
	public String login(@RequestParam String email, @RequestParam String password,
			HttpSession session) {
		User user = userService.getOrCreateUser(email, password); // Get or create user ID
		session.setAttribute("user", user); // Store in session

		return "redirect:/user-ui"; // Redirect to user form
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login"; // Redirect to login page
	}

	@GetMapping("/login")
	public String showLoginForm() {
		return "login"; // Returns the login.html page
	}

}
