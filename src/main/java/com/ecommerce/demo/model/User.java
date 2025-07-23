
package com.ecommerce.demo.model;


import com.ecommerce.demo.email.entities.ForgetPassword;

import com.ecommerce.demo.repository.LoginDeviceRepository;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    //create as "ROLE_ADMIN" or "ROLE_USER" not as "user" or "admin"
    private String roles; // Roles: "user" or "admin"
    
    @OneToOne(mappedBy = "user")
    private ForgetPassword forgetPassword;
    
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<LoginDevice> devices;


	public User(Long id, String username, String password, String email, String roles, ForgetPassword forgetPassword, List<LoginDevice> devices) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.email = email;
		this.roles = roles;
		this.forgetPassword = forgetPassword;
		this.devices = devices;
	}

	public List<LoginDevice> getDevices() {
		return devices;
	}

	public void setDevices(List<LoginDevice> devices) {
		this.devices = devices;
	}

	public ForgetPassword getForgetPassword() {
		return forgetPassword;
	}

	public void setForgetPassword(ForgetPassword forgetPassword) {
		this.forgetPassword = forgetPassword;
	}

	public User(Long id, String username, String password, String email, String roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
    }

    public User() {
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}
    
    
    
}