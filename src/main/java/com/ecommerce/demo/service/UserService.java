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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
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
    private PasswordEncoder encoder = new BCryptPasswordEncoder();

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
            
            String password = user.getPassword();
                    

            boolean isPasswordMatch = encoder.matches(password, hashedPassword);

        	if((existUser.get().getEmail()).equals(user.getEmail())) {
        		//encode the password
        		if(isPasswordMatch) {
					// checking if the no of device slot is full?
					String ans = loginDevice(user.getEmail(), user.getDevices().get(0).getDeviceId(), user.getDevices().get(0).getLocation(), user.getDevices().get(0).getDeviceName());
					if(ans.equalsIgnoreCase("Device already logged in.")){
						return user.getDevices().get(0).getDeviceName() + " already logged in";
					} else if(ans.equalsIgnoreCase("Already 2 devices logged in, remove one device to login.")){
						return "Already 2 devices logged in, remove one device to login.";
					} else if(ans.equalsIgnoreCase("Logged in Back") ){
						return "Welcome back in this device!!";
					} else {
						return "Device logged in successfully";
					}
        		} else {
        			return user.getEmail() + " password incorrect";
        		}
        	}
    	} 
		return user.getEmail() + " invalid.";    	
    }
   
    
    public String delete(User user) {
    	Optional<User> existUser = userRepository.findByEmail(user.getEmail());
    	
    	if(existUser.isPresent()) {
        	String hashedPassword = existUser.get().getPassword();
            
            String password = user.getPassword();
                    

            boolean isPasswordMatch = encoder.matches(password, hashedPassword);

        	if((existUser.get().getEmail()).equals(user.getEmail())) {
        		//encode the password
        		if(isPasswordMatch) {
        			userRepository.deleteById(existUser.get().getId());
        			return user.getEmail() + " deleted in successfully";
        		} else {
        			return user.getEmail() + " password incorrect";
        		}
        	}
    	} 
		return user.getEmail() + " not found.";
    }
    
    public Optional<UserDTO> findUserByEmail(String email) {
        Optional<User> userDetails = userRepository.findByEmail(email);
        if(userDetails.isEmpty()) {
    		throw new UsernameNotFoundException("Email ID : " + email + " is not found");
        } else {
        	return userDetails.map(
        			order -> {
    					User user = userDetails.get();
    					UserDTO userDTO = new UserDTO(user.getUsername(), user.getEmail());

    			            // Create and return the UserDTO
    			            return userDTO;
			        });
        }
        
    }



	// below is new code
	// ********************
	public String loginDevice(String email, String deviceId, String location, String deviceName) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		List<LoginDevice> devices = user.getDevices();

		// Check if the device is already logged in
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

		// Check if there are already two devices logged in
		long loggedInDevicesCount = devices.stream().filter(LoginDevice::isLoggedIn).count();
		if (loggedInDevicesCount >= 2) {
			return "Already 2 devices logged in, remove one device to login.";
		}

		// Log in the new device
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

	// this method must be used inside when the user is logged in
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
