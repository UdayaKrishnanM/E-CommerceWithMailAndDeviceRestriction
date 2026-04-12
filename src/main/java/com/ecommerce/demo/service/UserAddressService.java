package com.ecommerce.demo.service;

import com.ecommerce.demo.exception.AddressNotFoundException;
import com.ecommerce.demo.exception.ProductNotFoundException;
import com.ecommerce.demo.exception.UserNameNotFoundException;
import com.ecommerce.demo.model.User;
import com.ecommerce.demo.model.UserAddress;
import com.ecommerce.demo.repository.UserAddressRepository;
import com.ecommerce.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserAddressService {

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private UserRepository userRepository;

    private String getAuthenticatedEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();
    }

    public List<UserAddress> getMyAddresses() {
        String email = getAuthenticatedEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNameNotFoundException("User not found"));
        return userAddressRepository.findByUserId(user.getId());
    }

    @Transactional
    public UserAddress addAddress(UserAddress address) {
        String email = getAuthenticatedEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNameNotFoundException("User not found"));
        address.setUser(user);

        // If this is the first address, make it default
        List<UserAddress> existing = userAddressRepository.findByUserId(user.getId());
        if (existing.isEmpty()) {
            address.setDefault(true);
        }

        return userAddressRepository.save(address);
    }

    @Transactional
    public UserAddress updateAddress(Long addressId, UserAddress updatedAddress) {
        String email = getAuthenticatedEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNameNotFoundException("User not found"));

        UserAddress existing = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ProductNotFoundException("Address not found with id: " + addressId));

        if (!existing.getUser().getId().equals(user.getId())) {
            throw new UserNameNotFoundException("Access denied: Not your address");
        }

        existing.setFullName(updatedAddress.getFullName());
        existing.setAddressLine1(updatedAddress.getAddressLine1());
        existing.setAddressLine2(updatedAddress.getAddressLine2());
        existing.setCity(updatedAddress.getCity());
        existing.setState(updatedAddress.getState());
        existing.setPinCode(updatedAddress.getPinCode());
        existing.setCountry(updatedAddress.getCountry());
        existing.setPhoneNumber(updatedAddress.getPhoneNumber());

        return userAddressRepository.save(existing);
    }

    @Transactional
    public String setDefaultAddress(Long addressId) {
        String email = getAuthenticatedEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNameNotFoundException("User not found"));

        // Remove default from current default address
        userAddressRepository.findByUserIdAndIsDefault(user.getId(), true)
                .ifPresent(addr -> {
                    addr.setDefault(false);
                    userAddressRepository.save(addr);
                });

        UserAddress newDefault = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ProductNotFoundException("Address not found with id: " + addressId));

        if (!newDefault.getUser().getId().equals(user.getId())) {
            throw new UserNameNotFoundException("Access denied: Not your address");
        }

        newDefault.setDefault(true);
        userAddressRepository.save(newDefault);
        return "Default address updated";
    }

    @Transactional
    public String deleteAddress(Long addressId) {
        String email = getAuthenticatedEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNameNotFoundException("User not found"));

        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ProductNotFoundException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new UserNameNotFoundException("Access denied: Not your address");
        }

        userAddressRepository.delete(address);
        return "Address deleted successfully";
    }
}
