package com.ecommerce.demo.email.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.demo.email.dto.MailBody;
import com.ecommerce.demo.email.entities.ChangePassword;
import com.ecommerce.demo.email.entities.ForgetPassword;
import com.ecommerce.demo.email.entities.ForgetPasswordRepo;
import com.ecommerce.demo.email.services.EmailServices;
import com.ecommerce.demo.exception.UserNameNotFoundException;
import com.ecommerce.demo.model.User;
import com.ecommerce.demo.repository.UserRepository;


@RestController
@RequestMapping("/api/forgetPassword")
public class ForgetPasswordController {
	
	
	@Autowired
	public UserRepository userRepository;
	
	
	@Autowired
	public EmailServices emailServices;
	
	@Autowired
	public ForgetPasswordRepo forgetPasswordRepo;
	
	@Autowired
	public PasswordEncoder passwordEncoder;

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");


	//send mail for email verification
	@PostMapping("/verifyEmail/{email}")
	public ResponseEntity<String> sendEmail(@PathVariable String email){

		Optional<User> user = userRepository.findByEmail(email);

		if(user.isEmpty()){
			LoggerFactory.getLogger(getClass()).info("--- NOT FOUND ----");
			return new ResponseEntity<String>("Email id not found", HttpStatus.NOT_FOUND);
		}
		if(user.get().getForgetPassword()!=null && user.get().getForgetPassword().getExpirationTime().before(new Date())){
			LoggerFactory.getLogger(getClass()).info("OTP expired, regenerating new otp");
			forgetPasswordRepo.deleteById(user.get().getForgetPassword().getForgetID());
		}

		if(user.get().getForgetPassword()!=null && user.get().getForgetPassword().getExpirationTime().after(new Date())){
			LoggerFactory.getLogger(getClass()).info("OTP already generated");
			return new ResponseEntity<String>("OTP already generated", HttpStatus.CONFLICT);
		}

		int otp = generateOtp();
		LoggerFactory.getLogger(getClass()).info(" ------- " + otp);


		MailBody mailBody = MailBody.builder()
				.to(email)
				.text("This is the OTP for your Forgotten Password : " + otp)
				.subject("OTP for forgotten password")
				.build();


		ForgetPassword fp = ForgetPassword.builder()
				.otp(otp)
				.expirationTime(new Date(System.currentTimeMillis() + 700 * 1000))
				.user(user.get())
				.build();


		emailServices.sendSimpleMessage(mailBody);

		forgetPasswordRepo.save(fp);
		return ResponseEntity.ok("Email sent for verification");
	}


	public Integer generateOtp() {
		Random random = new Random();
		return random.nextInt(100_000, 999_999);
	}

	
	@PostMapping("/verifyOtp/{otp}/{email}")
	public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email){

		Optional<User> user = userRepository.findByEmail(email);
		if(user.isEmpty()){
			return new ResponseEntity<String>("Provide a valid Email", HttpStatus.NOT_FOUND);
		}

		Optional<ForgetPassword> fp = forgetPasswordRepo.findByOtpAndUser(otp, user.get());
		if(fp.isEmpty()){
			return new ResponseEntity<String>("Invalid OTP", HttpStatus.UNAUTHORIZED);
		}

		// if otp time expired so tell user to resend otp
		if(fp.get().getExpirationTime().before(Date.from(Instant.now()))) {
			forgetPasswordRepo.deleteById(fp.get().getForgetID());
			return new ResponseEntity<>("OTP has expired!!!", HttpStatus.EXPECTATION_FAILED);
		}
		
		return ResponseEntity.ok("OTP is verified !!");
	}

	@PostMapping("/changePassword/{email}/{otp}")
	public ResponseEntity<String> changePassword(@RequestBody ChangePassword changePassword, @PathVariable String email, @PathVariable Integer otp){
		// Find user by email
		Optional<User> user = userRepository.findByEmail(email);

		if(user.isEmpty()){
			return new ResponseEntity<String>("Provide a valid Email", HttpStatus.NOT_FOUND);
		}

		// Retrieve the OTP record associated with the user
		Optional<ForgetPassword> fp = forgetPasswordRepo.findByOtpAndUser(otp, user.get());

		LoggerFactory.getLogger(getClass()).info("---  ----" + fp);

		// Check if the OTP exists
		if(fp.isEmpty()){
			return new ResponseEntity<>("OTP not found! Please generate a new OTP.", HttpStatus.NOT_FOUND);
		}


		// Check if OTP is expired
		if(fp.get().getExpirationTime().before(Date.from(Instant.now()))) {
			forgetPasswordRepo.deleteById(fp.get().getForgetID());
			return new ResponseEntity<>("OTP has expired! Please request a new one.", HttpStatus.EXPECTATION_FAILED);
		}

		// Validate password fields match
		if(!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
			return new ResponseEntity<>("Please enter the password again!", HttpStatus.EXPECTATION_FAILED);
		}

		// Encode the new password
		String encodedPassword = passwordEncoder.encode(changePassword.password());
		userRepository.updatePassword(email, encodedPassword);

		// Clean up the OTP record after successful password change
		forgetPasswordRepo.deleteById(fp.get().getForgetID());

		return ResponseEntity.ok("Password has been changed successfully!");
	}


//	@PostMapping("/changePassword/{email}")
//	public ResponseEntity<String> changePassword(@RequestBody ChangePassword changePassword, @PathVariable String email){
//		Optional<User> userDetails = userRepository.findByEmail(email);
//		if(userDetails.isEmpty()){
//			return new ResponseEntity<>("User Email not found", HttpStatus.NOT_FOUND);
//		}
//		if(!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
//			return new ResponseEntity<>("Please enter the password again!", HttpStatus.EXPECTATION_FAILED);
//		}
//		// encode the password
//		String encodedPassword = passwordEncoder.encode(changePassword.password());
//		userRepository.updatePassword(email, encodedPassword);
//
//		return ResponseEntity.ok("Password has been changed !!");
//	}
	
}
