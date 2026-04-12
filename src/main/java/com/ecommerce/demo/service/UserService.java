package com.ecommerce.demo.service;

import com.ecommerce.demo.dto.ItemDTO;
import com.ecommerce.demo.dto.ProductDTO;
import com.ecommerce.demo.dto.UserDTO;
import com.ecommerce.demo.dto.UserOrderResponse;
import com.ecommerce.demo.exception.DeviceNotFoundException;
import com.ecommerce.demo.exception.OrderNotFoundException;
import com.ecommerce.demo.exception.UserNameNotFoundException;
import com.ecommerce.demo.model.LoginDevice;
import com.ecommerce.demo.model.Product;
import com.ecommerce.demo.model.User;
import com.ecommerce.demo.repository.LoginDeviceRepository;
import com.ecommerce.demo.repository.UserRepository;
import com.ecommerce.demo.security.JwtUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

	@Autowired
	private LoginDeviceRepository deviceRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public String registerNewUserAccount(User user) {
    	if(userRepository.existsByEmail(user.getEmail())){    		
            return "Mail ID Already Exists";
        } else {
            user.setPassword(encoder.encode(user.getPassword()));
            userRepository.save(user);
            return "Done";
        }
    }

    public String login(User user){
    	Optional<User> existUser = userRepository.findByEmail(user.getEmail());
    	
    	if(existUser.isPresent()) {
        	String hashedPassword = existUser.get().getPassword();
            boolean isPasswordMatch = encoder.matches(user.getPassword(), hashedPassword);

        	if(isPasswordMatch) {
				String deviceResult = loginDevice(user.getEmail(), user.getDevices().get(0).getDeviceId(), user.getDevices().get(0).getLocation(), user.getDevices().get(0).getDeviceName());
				if(deviceResult.equalsIgnoreCase("Device already logged in.") || deviceResult.equalsIgnoreCase("Logged in Back") || deviceResult.equalsIgnoreCase("Device logged in successfully.")) {
					return jwtUtil.generateToken(existUser.get().getEmail(), existUser.get().getRoles());
				} else if(deviceResult.equalsIgnoreCase("Already 2 devices logged in, remove one device to login.")){
					return "Already 2 devices logged in, remove one device to login.";
				} else {
					return "Login failed: " + deviceResult;
				}
        	} else {
        		return user.getEmail() + " password incorrect";
        	}
    	} 
		return user.getEmail() + " invalid.";    	
    }
   
    
    public String delete(User user) {
    	Optional<User> existUser = userRepository.findByEmail(user.getEmail());
    	
    	if(existUser.isPresent()) {
        	String hashedPassword = existUser.get().getPassword();
            boolean isPasswordMatch = encoder.matches(user.getPassword(), hashedPassword);

        	if(isPasswordMatch) {
        		userRepository.deleteById(existUser.get().getId());
        		return user.getEmail() + " deleted successfully";
        	} else {
        		return user.getEmail() + " password incorrect";
        	}
    	} 
		return user.getEmail() + " not found.";
    }
    
    public Optional<UserDTO> findUserByEmail(String email) {
        Optional<User> userDetails = userRepository.findByEmail(email);
        if(userDetails.isEmpty()) {
    		throw new UsernameNotFoundException("Email ID : " + email + " is not found");
        } else {
        	return userDetails.map(user -> new UserDTO(user.getUsername(), user.getEmail()));
        }
    }



	public String loginDevice(String email, String deviceId, String location, String deviceName) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		List<LoginDevice> devices = user.getDevices();

		for (LoginDevice device : devices) {
			if (device.getDeviceId().equals(deviceId) && device.isLoggedIn()) {
				return "Device already logged in.";
			}
		}
		for(LoginDevice device : devices){
			if(device.getDeviceId().equals(deviceId) && !device.isLoggedIn()){
				device.setLoggedIn(true);
				deviceRepository.save(device);
				return "Logged in Back";
			}
		}

		long loggedInDevicesCount = devices.stream().filter(LoginDevice::isLoggedIn).count();
		if (loggedInDevicesCount >= 2) {
			return "Already 2 devices logged in, remove one device to login.";
		}

		LoginDevice device = new LoginDevice(deviceId, location, true, deviceName, user);
		deviceRepository.save(device);
		return "Device logged in successfully.";
	}

	public String logoutDevice(String email, String deviceId) {
		logger.info(deviceId + " ------ " + email);
		LoginDevice device = deviceRepository.findByDeviceIdAndEmailId(deviceId, email)
				.orElseThrow(() -> new RuntimeException("Device not found"));

		device.setLoggedIn(false);
		deviceRepository.save(device);
		return "Device logged out successfully.";
	}

	public List<LoginDevice> getUserDevices(String email) {
		return deviceRepository.findByUserEmail(email);
	}

	public String removeDevice(Long userId, String deviceId) {
		Optional<LoginDevice> device = deviceRepository.findByDeviceIdAndUserId(deviceId, userId);
		if(device.isEmpty()){
			return "Device Not Found";
		}
		deviceRepository.delete(device.get());
		return "Device removed successfully.";
	}

}

