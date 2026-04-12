package com.ecommerce.demo.service;

import com.ecommerce.demo.dto.WishlistDTO;
import com.ecommerce.demo.exception.ProductNotFoundException;
import com.ecommerce.demo.exception.UserNameNotFoundException;
import com.ecommerce.demo.model.Product;
import com.ecommerce.demo.model.User;
import com.ecommerce.demo.model.Wishlist;
import com.ecommerce.demo.repository.ProductRepository;
import com.ecommerce.demo.repository.UserRepository;
import com.ecommerce.demo.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private String getAuthenticatedEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();
    }

    public List<WishlistDTO> getMyWishlist() {
        String email = getAuthenticatedEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNameNotFoundException("User not found"));
        return wishlistRepository.findByUserId(user.getId()).stream()
                .map(w -> new WishlistDTO(
                        w.getId(),
                        w.getProduct().getId(),
                        w.getProduct().getName(),
                        w.getProduct().getDescription(),
                        w.getProduct().getPrice(),
                        w.getProduct().getCategory()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public String addToWishlist(Long productId) {
        String email = getAuthenticatedEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNameNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            return "Product already in wishlist";
        }
        wishlistRepository.save(new Wishlist(user, product));
        return "Product added to wishlist";
    }

    @Transactional
    public String removeFromWishlist(Long productId) {
        String email = getAuthenticatedEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNameNotFoundException("User not found"));
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found in wishlist"));
        wishlistRepository.delete(wishlist);
        return "Product removed from wishlist";
    }
}
