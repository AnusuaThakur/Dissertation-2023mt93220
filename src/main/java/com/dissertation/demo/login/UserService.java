package com.dissertation.demo.login;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	
	@Autowired
    private UserRepository userRepository;

    public User getOrCreateUser(String email, String password) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User(email, password);
            return userRepository.save(newUser); // Save and return the new user
        });
    }
    public User updateUserProfile(Long userId, String name, String phone, String address, String dob) {
        Optional<User> optionalUser = userRepository.findById(userId);
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(name);
            user.setPhone(phone);
            user.setAddress(address);
            user.setDob(dob);
            return userRepository.save(user); 
        } else {
            throw new RuntimeException("User not found");
        }
    }
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

}
