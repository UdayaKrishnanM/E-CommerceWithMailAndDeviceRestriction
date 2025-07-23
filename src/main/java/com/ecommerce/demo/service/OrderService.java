package com.ecommerce.demo.service;


import com.ecommerce.demo.controller.AdminBasedController;
import com.ecommerce.demo.dto.*;
import com.ecommerce.demo.exception.CartItemNotFoundException;
import com.ecommerce.demo.exception.OrderNotFoundException;
import com.ecommerce.demo.exception.ProductNotFoundException;
import com.ecommerce.demo.model.*;
import com.ecommerce.demo.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb.OrderDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

	@Autowired
	private ProductRepository productRepository;
	
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;

	@Autowired
	private ProductSizeRepository productSizeRepository;
    
    @Autowired
    private UserRepository userRepository;

	public List<UserOrderResponse> getAllOrdersByEmail(String email) {

		Optional<User> userExist = userRepository.findByEmail(email);
		if(userExist.isEmpty()){
			throw new OrderNotFoundException("User ID : " + email + " not exists.");
		}
		List<Order> ordersList = orderRepository.findByUserId(userExist.get().getId());
		if (ordersList.isEmpty()) {
			throw new OrderNotFoundException("User ID : " + email + " has no orders.");
		}

		return ordersList.stream().map(order -> {
			// Convert User to UserDTO
			User user = order.getUser();
			UserDTO userDTO = new UserDTO(user.getUsername(), user.getEmail());

			// Convert OrderItems to ItemDTOs
			List<ItemDTO> itemsDTO = order.getOrderItems().stream().map(item -> {
				Product product = item.getProduct();

				// Convert ProductSize list to ProductSizeDTOs
//				List<ProductSizeDTO> sizeDTOs = product.getSizes().stream()
//						.map(size -> new ProductSizeDTO(size.getSize().toString(), size.getStockQuantity()))
//						.collect(Collectors.toList());

//				ProductSize orderedSize = item.getProductSize();
//				List<ProductSizeDTO> sizeDTOs = List.of(
//						new ProductSizeDTO(orderedSize.getSize().toString(), orderedSize.getStockQuantity())
//				);

				String sizeDTOs = item.getSize().getSize().toString();

				// Create ProductDTO with sizes
				ProductDTO productDTO = new ProductDTO(
						product.getName(),
						product.getDescription(),
						product.getPrice(),
						product.getCategory(),
						sizeDTOs
				);

				// Return ItemDTO
				return new ItemDTO(productDTO, item.getQuantity(), item.getPrice());
			}).collect(Collectors.toList());

			// Return UserOrderResponse
			return new UserOrderResponse(
					userDTO,
					itemsDTO,
					order.getTotalAmount(),
					order.getStatus(),
					order.getOrderDate()
			);
		}).collect(Collectors.toList());
	}


	public Optional<UserOrderResponse> getOrderById(Long id) {

		Optional<Order> orderOpt = orderRepository.findById(id);

		if (orderOpt.isEmpty()) {
			throw new OrderNotFoundException("Order ID : " + id + " not found.");
		}

		return orderOpt.map(order -> {
			// Convert User to UserDTO
			User user = order.getUser();
			UserDTO userDTO = new UserDTO(user.getUsername(), user.getEmail());

			// Convert OrderItems to ItemDTOs
			List<ItemDTO> itemsDTO = order.getOrderItems().stream().map(item -> {
				Product product = item.getProduct();

				// Convert ProductSize list to ProductSizeDTOs
//				List<ProductSizeDTO> sizeDTOs = product.getSizes().stream()
//						.map(size -> new ProductSizeDTO(size.getSize().toString(), size.getStockQuantity()))
//						.collect(Collectors.toList());

				String sizeDTOs = item.getSize().getSize().toString();



				// Create ProductDTO with size info
				ProductDTO productDTO = new ProductDTO(
						product.getName(),
						product.getDescription(),
						product.getPrice(),
						product.getCategory(),
						sizeDTOs
				);

				// Return ItemDTO
				return new ItemDTO(productDTO, item.getQuantity(), item.getPrice());
			}).collect(Collectors.toList());

			// Build and return UserOrderResponse
			return new UserOrderResponse(
					userDTO,
					itemsDTO,
					order.getTotalAmount(),
					order.getStatus(),
					order.getOrderDate()
			);
		});
	}



	// old method
	// ---------
//	public Optional<UserOrderResponse> createOrderMethod(Order order) {
//		double totalAmount = 0.0;
//
//		// Check stock for all items before saving the order
//		for (OrderItem item : order.getOrderItems()) {
//			Product product = productRepository.findById(item.getProduct().getId())
//					.orElseThrow(() -> new ProductNotFoundException("Product not found"));
//
//			ProductSize selectedSize = product.getSizes().stream()
//					.filter(size -> size.getSize() == item.getProductSize().getSize())
//					.findFirst()
//					.orElseThrow(() -> new OrderNotFoundException("Selected size " + item.getProductSize().getSize() + " not available for product: " + product.getName()));
//
//			logger.info(selectedSize.getStockQuantity() + " ------------ " + item.getQuantity());
//
//			if (selectedSize.getStockQuantity() < item.getQuantity()) {
//				throw new OrderNotFoundException("Insufficient stock for product: " + product.getName() + " size: " + selectedSize.getSize());
//			}
//
//			if (item.getQuantity() <= 0) {
//				throw new OrderNotFoundException("Product quantity can't be 0 or negative");
//			}
//
//			// Set the managed ProductSize instance
//			item.setProductSize(selectedSize);
//
//		}
//
//		// Save the order first
//		order = orderRepository.save(order);
//
//		for (OrderItem item : order.getOrderItems()) {
//			Product product = productRepository.findById(item.getProduct().getId())
//					.orElseThrow(() -> new RuntimeException("Product not found"));
//
//			ProductSize selectedSize = product.getSizes().stream()
//					.filter(size -> size.getSize() == item.getProductSize().getSize())
//					.findFirst()
//					.orElseThrow(() -> new OrderNotFoundException("Selected size not available for product: " + product.getName()));
//
//			// Update the product's stock quantity for the selected size
//			selectedSize.setStockQuantity(selectedSize.getStockQuantity() - item.getQuantity());
//			productRepository.save(product);
//
//			// Set price and calculate subtotal
//			item.setPrice(product.getPrice());
//			totalAmount += item.calculateSubtotal();
//
//			// Link item to order
//			item.setOrder(order);
//		}
//
//		// Set total amount
//		order.setTotalAmount(totalAmount);
//
//		// Save order items
//		orderItemRepository.saveAll(order.getOrderItems());
//
//		// Prepare response
//		UserOrderResponse response = new UserOrderResponse();
//		response.setUser(new UserDTO(order.getUser().getId(), order.getUser().getUsername(), order.getUser().getEmail()));
//
//		response.setItems(order.getOrderItems().stream()
//				.map(item -> {
//					Product product = item.getProduct();
//					List<ProductSizeDTO> sizeDTOs = product.getSizes().stream()
//							.map(size -> new ProductSizeDTO(size.getSize(), size.getStockQuantity()))
//							.collect(Collectors.toList());
//
//					ProductDTO productDTO = new ProductDTO(
//							product.getName(),
//							product.getDescription(),
//							product.getPrice(),
//							product.getCategory(),
//							sizeDTOs
//					);
//
//					return new ItemDTO(item.getId(), productDTO, item.getQuantity(), item.getPrice());
//				})
//				.collect(Collectors.toList())
//		);
//
//		response.setTotalAmount(totalAmount);
//		response.setStatus(order.getStatus());
//		response.setOrderDate(order.getOrderDate());
//
//		return Optional.of(response);
//	}

	public Optional<UserOrderResponse> createOrderMethod(Order order) {
		double totalAmount = 0.0;

		// Group total quantity per product-size
		Map<String, Integer> productSizeQuantityMap = new HashMap<>();

		for (OrderItem item : order.getOrderItems()) {
			String key = item.getProduct().getId() + "_" + item.getProductSize().getSize().name();
			productSizeQuantityMap.put(key, productSizeQuantityMap.getOrDefault(key, 0) + item.getQuantity());
		}

		// Check stock for all items before saving the order
		for (Map.Entry<String, Integer> entry : productSizeQuantityMap.entrySet()) {
			String[] parts = entry.getKey().split("_");
			Long productId = Long.valueOf(parts[0]);
			SizeType size = SizeType.valueOf(parts[1]);
			int totalQuantity = entry.getValue();

			Product product = productRepository.findById(productId)
					.orElseThrow(() -> new ProductNotFoundException("Product not found"));

			ProductSize selectedSize = product.getSizes().stream()
					.filter(s -> s.getSize() == size)
					.findFirst()
					.orElseThrow(() -> new OrderNotFoundException("Selected size " + size + " not available for product: " + product.getName()));

			if (selectedSize.getStockQuantity() < totalQuantity) {
				throw new OrderNotFoundException("Insufficient stock for product: " + product.getName() + " size: " + size);
			}
		}

		// Assign managed ProductSize instance to each OrderItem
		for (OrderItem item : order.getOrderItems()) {
			Product product = productRepository.findById(item.getProduct().getId())
					.orElseThrow(() -> new RuntimeException("Product not found"));

			ProductSize selectedSize = product.getSizes().stream()
					.filter(size -> size.getSize() == item.getProductSize().getSize())
					.findFirst()
					.orElseThrow(() -> new OrderNotFoundException("Selected size not available for product: " + product.getName()));

			item.setProductSize(selectedSize);
		}

		// Save the order first
		order = orderRepository.save(order);

		for (OrderItem item : order.getOrderItems()) {
			Product product = productRepository.findById(item.getProduct().getId())
					.orElseThrow(() -> new RuntimeException("Product not found"));

			ProductSize selectedSize = product.getSizes().stream()
					.filter(size -> size.getSize() == item.getProductSize().getSize())
					.findFirst()
					.orElseThrow(() -> new OrderNotFoundException("Selected size not available for product: " + product.getName()));

			selectedSize.setStockQuantity(selectedSize.getStockQuantity() - item.getQuantity());
			productRepository.save(product);

			item.setPrice(product.getPrice());
			totalAmount += item.calculateSubtotal();

			item.setOrder(order);
		}

		order.setTotalAmount(totalAmount);
		orderItemRepository.saveAll(order.getOrderItems());

		UserOrderResponse response = new UserOrderResponse();
		response.setUser(new UserDTO(order.getUser().getUsername(), order.getUser().getEmail()));

		response.setItems(order.getOrderItems().stream()
				.map(item -> {
					Product product = item.getProduct();
//					List<ProductSizeDTO> sizeDTOs = product.getSizes().stream()
//							.map(size -> new ProductSizeDTO(size.getSize().toString(), size.getStockQuantity()))
//							.collect(Collectors.toList());
					String sizeDTOs = item.getSize().getSize().toString();

					ProductDTO productDTO = new ProductDTO(
							product.getName(),
							product.getDescription(),
							product.getPrice(),
							product.getCategory(),
							sizeDTOs
					);

					return new ItemDTO(productDTO, item.getQuantity(), item.getPrice());
				})
				.collect(Collectors.toList())
		);

		response.setTotalAmount(totalAmount);
		response.setStatus(order.getStatus());
		response.setOrderDate(order.getOrderDate());

		return Optional.of(response);
	}



	public Optional<UserOrderResponse> createOrder(Order order) {
    	
    	Optional<Order> orderDetails = orderRepository.findById(order.getId());

		// testing the authentication if works keep else remove
		String authenticatedEmail = null;

		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			authenticatedEmail = ((UserDetails) principal).getUsername(); // email in your case
		} else {
			authenticatedEmail = principal.toString(); // fallback
		}
		Optional<User> userDetailsTest = userRepository.findById(order.getUser().getId());

		if (userDetailsTest.isEmpty()) {
			throw new OrderNotFoundException("User not found for order");
		}

		if (!userDetailsTest.get().getEmail().equals(authenticatedEmail)) {
			logger.warn("Access denied: Authenticated user email '{}' does not match order's user email '{}'",
					authenticatedEmail, userDetailsTest.get().getEmail());
			throw new OrderNotFoundException("Access denied: Unauthorized order request");
		}

		logger.info("Access granted for user: {}", authenticatedEmail);
		// auth testing

		if (orderDetails.isPresent()) {
			throw new OrderNotFoundException("Order already exists");
		}

		return createOrderMethod(order);

    }



	public Optional<UserOrderResponse> updateOrderStatus(Long id, Order updateOrder) {
		Optional<Order> orderOptional = orderRepository.findById(id);

		if (orderOptional.isEmpty()) {
			throw new OrderNotFoundException("Order ID : " + id + " not found");
		}

		Order existingOrder = orderOptional.get();
		// Check stored user ID and sent user ID match
		if (!updateOrder.getUser().getId().equals(existingOrder.getUser().getId())) {
			throw new OrderNotFoundException("Unable to access: user mismatch");
		}

		// Update order status
		existingOrder.setStatus(updateOrder.getStatus());
		orderRepository.save(existingOrder);

		// Build UserOrderResponse
		return Optional.ofNullable(existingOrder).map(order -> {
			User user = order.getUser();
			UserDTO userDTO = new UserDTO(user.getUsername(), user.getEmail());

			List<ItemDTO> itemsDTO = order.getOrderItems().stream().map(item -> {
				Product product = item.getProduct();

				// Convert product sizes to DTO
//				List<ProductSizeDTO> sizeDTOs = product.getSizes().stream()
//						.map(size -> new ProductSizeDTO(size.getSize().toString(), size.getStockQuantity()))
//						.collect(Collectors.toList());
//				logger.info(sizeDTOs.toString() + " -------------- ");

				String sizeDTOs = item.getSize().getSize().toString();


				ProductDTO productDTO = new ProductDTO(
						product.getName(),
						product.getDescription(),
						product.getPrice(),
						product.getCategory(),
						sizeDTOs
				);

				return new ItemDTO(productDTO, item.getQuantity(), item.getPrice());
			}).collect(Collectors.toList());

			return new UserOrderResponse(
					userDTO,
					itemsDTO,
					order.getTotalAmount(),
					order.getStatus(),
					order.getOrderDate()
			);
		});
	}

    
    public String deleteOrder(Long id) {
        Optional<Order> order = orderRepository.findById(id);
		if(order.isEmpty()){
			return "Order ID Not Exists";

		}

		// testing the authentication if works keep else remove
		String authenticatedEmail = null;

		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			authenticatedEmail = ((UserDetails) principal).getUsername(); // email in your case
		} else {
			authenticatedEmail = principal.toString(); // fallback
		}
		logger.info(authenticatedEmail.toString() + " " + " -----------");
		Optional<User> userDetailsTest = userRepository.findById(order.get().getUser().getId());

		if (userDetailsTest.isEmpty()) {
			throw new OrderNotFoundException("User not found for order");
		}

		if (!userDetailsTest.get().getEmail().equals(authenticatedEmail)) {
			logger.warn("Access denied: Authenticated user email '{}' does not match order's user email '{}'",
					authenticatedEmail, userDetailsTest.get().getEmail());
			throw new OrderNotFoundException("Access denied: Unauthorized order request");
		}

		logger.info("Access granted for user: {}", authenticatedEmail);
		// auth testing

		orderRepository.deleteById(order.get().getId());
		return "Deleted Order Successfully ID: " + id;

    }


}
