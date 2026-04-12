package com.ecommerce.demo.controller;

import com.ecommerce.demo.dto.ApiResponse;
import com.ecommerce.demo.dto.UserDTO;
import com.ecommerce.demo.exception.OrderNotFoundException;
import com.ecommerce.demo.model.LoginDevice;
import com.ecommerce.demo.model.User;
import com.ecommerce.demo.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@RequestBody User user) {
        String result = userService.registerNewUserAccount(user);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{email}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@PathVariable String email) {
    	Optional<UserDTO> user = userService.findUserByEmail(email);
    	if(user.isPresent()) {
    		return ResponseEntity.ok(ApiResponse.ok(user.get()));
    	} else {
    		throw new OrderNotFoundException("User with email : " + email + " not found");
    	}
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> loginToApplication(@RequestBody User user) {
    	String token = userService.login(user);
    	return ResponseEntity.ok(ApiResponse.ok("Login successful", token));
	}
    
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteUserAccount(@RequestBody User user) {
    	String result = userService.delete(user);
    	return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/devices/login")
    public ResponseEntity<ApiResponse<String>> loginDevice(@RequestParam String email, @RequestParam String deviceId, @RequestParam String location, @RequestParam String deviceName) {
        String result = userService.loginDevice(email, deviceId, location, deviceName);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/devices/logout")
    public ResponseEntity<ApiResponse<String>> logoutDevice(@RequestParam String email, @RequestParam String deviceId) {
        String result = userService.logoutDevice(email, deviceId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/devices/user/{email}")
    public ResponseEntity<ApiResponse<List<LoginDevice>>> getUserDevices(@PathVariable String email) {
        List<LoginDevice> devices = userService.getUserDevices(email);
        return ResponseEntity.ok(ApiResponse.ok(devices));
    }

    @DeleteMapping("/devices/remove")
    public ResponseEntity<ApiResponse<String>> removeDevice(@RequestParam Long userId, @RequestParam String deviceId) {
        String result = userService.removeDevice(userId, deviceId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

}

