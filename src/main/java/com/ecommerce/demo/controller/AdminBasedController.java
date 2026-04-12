package com.ecommerce.demo.controller;

import com.ecommerce.demo.dto.*;
import com.ecommerce.demo.exception.OrderItemNotFoundException;
import com.ecommerce.demo.exception.OrderNotFoundException;
import com.ecommerce.demo.exception.ProductNotFoundException;
import com.ecommerce.demo.exception.ReviewNotFoundException;
import com.ecommerce.demo.model.*;
//import com.ecommerce.demo.service.OrderItemService;
import com.ecommerce.demo.service.AdminStatsService;
import com.ecommerce.demo.service.OrderService;
import com.ecommerce.demo.service.ProductService;
import com.ecommerce.demo.service.ReviewService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.jaxb.SpringDataJaxb.OrderDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/api/admin")
public class AdminBasedController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

//    @Autowired
//    private OrderItemService orderItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdminStatsService adminStatsService;
    
    
	private static final Logger logger = LoggerFactory.getLogger(AdminBasedController.class);

    
    // #################################//
	// ********* PRODUCTS ENTITY *******//
    //----------------------------------//

	
    // ********* GET ALL PRODUCTS ******//
    @GetMapping("/getAllProducts")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> list =  productService.getAllProducts();
        if(list.isEmpty()) {
        	throw new ProductNotFoundException("Product list is empty");
        } else {
        	return new ResponseEntity<List<Product>>(list, HttpStatus.OK);
        }
        
    }

    // *********** GET PRODUCT BY ID *********//

    //done//
    @GetMapping("/getProductById/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<ProductDTO> data = productService.getProductById(id);
        if(data==null){
            return new ResponseEntity<>("Product with id: " + id + " not found....", HttpStatus.NOT_FOUND);
        }
    	return new ResponseEntity<>(data, HttpStatus.OK);
    }

    // ********* FILTER PRODUCT ***********//
    @GetMapping("/filter")
    public Page<ProductDTO> filterProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return productService.getFilteredProducts(category, minPrice, maxPrice, name, page, size, sortBy, sortDir);
    }

    // *********** CREATE PRODUCT *********//
    @PostMapping("/createProduct")
    public String createProductPost(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    
    // *********** UPDATE PRODUCT BY ID *********//
    @PutMapping("/updateProduct/{id}")
    public ResponseEntity<Optional<Product>> updateProduct(@PathVariable Long id, @RequestBody UpdateProductDTO updateProductDTO) {
        Optional<Product> data = productService.updateProduct(id, updateProductDTO.getProductDetails(), updateProductDTO.getRequestedSize(), updateProductDTO.getNewStockQuantity());

        if (data.isPresent()) {
            return ResponseEntity.ok(data);
        } else {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
    }

    
    
    // *********** DELETE PRODUCT BY ID *********//
    @DeleteMapping("/deleteProduct/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
    	String deleted = productService.deleteProduct(id);
    	if(!"Product Not Exists".equals(deleted)) {
    		return new ResponseEntity<String>("Order id with " + id + " deleted succesfully" ,HttpStatus.OK);
    	} else {
    		throw new OrderNotFoundException("Order not found with id: " + id);
    	}
    }
    
    // *********** PRODUCT EXCEPTION HANDLER handled by GlobalExceptionHandler *********//

    
    // ###############################//
	// ********* ORDER ENTITY ********//
    //--------------------------------//

        
    // *********** GET ALL ORDER for User (done) *********//
    @GetMapping("/getAllMyOrdersByEmail/{email}")
    public ResponseEntity<List<UserOrderResponse>> getAllOrders(@PathVariable String email) {
        List<UserOrderResponse> ordersList = orderService.getAllOrdersByEmail(email);
        if(ordersList.isEmpty()) {
        	throw new OrderNotFoundException("No orders placed yet");
        } else {
        	return new ResponseEntity<List<UserOrderResponse>>(ordersList, HttpStatus.OK);        }
        
    }


    // *********** GET ORDER BY ID *********//
    @GetMapping("/getOrderById/{id}")
    public ResponseEntity<Optional<UserOrderResponse>> getOrderById(@PathVariable Long id) {
    	
    	Optional<UserOrderResponse> data = orderService.getOrderById(id);
    	if(data.isPresent()) {
    		return new ResponseEntity<Optional<UserOrderResponse>>(data, HttpStatus.OK);
    	} else {
    		throw new OrderNotFoundException("Order Not Found with id : " + id);
    	}
    }

    

    // *********** UPDATE ORDER-status BY ID *********//
    @PutMapping("/updateOrderStatus/{id}")
    public ResponseEntity<Optional<UserOrderResponse>> updateOrderStatus(@PathVariable Long id, @RequestBody Order updateOrder) {	
    	Optional<UserOrderResponse> order = orderService.updateOrderStatus(id, updateOrder);

    	if(order.isPresent()) {
    		return new ResponseEntity<Optional<UserOrderResponse>>(order, HttpStatus.OK);
    	} else {
    		throw new OrderNotFoundException("Order with ID : " + id + " not found");
    	}
    
    }

    
    // *********** ORDER EXCEPTION HANDLER handled by GlobalExceptionHandler *********//


    // ###################################//
	// ********* ORDER_ITEM ENTITY this is not neeeded to be accessed *******//
    //------------------------------------//
   
//    // *********** GET ALL ORDER_ITEM  *********//
//    @GetMapping("/getAllOrderItems")
//    public ResponseEntity<List<ItemDTO>> getAllOrderItems() {
//
//    	List<ItemDTO> orderItems = orderItemService.getAllOrderItems();
//    	if(orderItems.isEmpty()) {
//    		throw new OrderItemNotFoundException("Order Items empty");
//    	} else {
//    		return new ResponseEntity<List<ItemDTO>>(orderItems, HttpStatus.OK);
//    	}
//
//    }
//
//    // *********** GET ALL ORDER_ITEM *********//
//    @GetMapping("/getAllOrderItemsByUserId/{id}")
//    public ResponseEntity<List<ItemDTO>> getAllOrderItemsByUserId(@PathVariable Long id) {
//
//    	List<ItemDTO> orderItems = orderItemService.getOrderItemByUserId(id);
//    	if(orderItems.isEmpty()) {
//    		throw new OrderItemNotFoundException("Order Items empty");
//    	} else {
//    		return new ResponseEntity<List<ItemDTO>>(orderItems, HttpStatus.OK);
//    	}
//
//    }
//
//
//    // *********** GET ORDER_ITEM BY ID *********//
//    @GetMapping("/getOrderItemById/{id}")
//    public ResponseEntity<List<ItemDTO>> getOrderItemById(@PathVariable Long id) {
//    	List<ItemDTO> orderItemList =  orderItemService.getOrderItemById(id);
//        if(orderItemList.isEmpty()) {
//        	throw new OrderItemNotFoundException("Order Item with id : " + id + " not found");
//        } else {
//        	return new ResponseEntity<List<ItemDTO>>(orderItemList, HttpStatus.OK);
//        }
//    }
//
//    @ExceptionHandler(OrderItemNotFoundException.class)
//    public ResponseEntity<String> handleOrderItemNotFoundException(OrderItemNotFoundException ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
//    }
//
    
    
    // ###############################//
	// ********* REVIEW ENTITY *******//
    //--------------------------------//


    // *********** DELETE REVIEW BY ID (done) *********//
    @DeleteMapping("/deleteReviewByProductId/{productId}")
    public ResponseEntity<String> deleteReviewByProduct(
            @PathVariable Long productId,
            @RequestParam Long userId
    ) {
        String result = reviewService.deleteReviewByProductId(productId, userId);
        return ResponseEntity.ok(result);
    }


    // *********** GET REVIEW BY ID NEW CODE (Done) *********//
    @GetMapping("/getReviewById/{id}")
    public ResponseEntity<Optional<ReviewDTO>> getReviewById(@PathVariable Long id) {
        Optional<ReviewDTO> data = reviewService.getReviewById(id);
    	if(data.isPresent()) {
    		return new ResponseEntity<Optional<ReviewDTO>>(data, HttpStatus.OK);
    	} else {
    		throw new ReviewNotFoundException("Review ID : " + id + " not found");
    	}
    }

    
    // *********** GET ALL REVIEW *********//
    @GetMapping("/getAllReviews")
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
    	List<ReviewDTO> getAllReviews = reviewService.getAllReviews();
        if(getAllReviews.isEmpty()) {
     	   throw new ReviewNotFoundException("Reviews empty");
        } else {
     	   return new ResponseEntity<List<ReviewDTO>>(getAllReviews, HttpStatus.OK);
        }
    }

    // *********** GET ALL REVIEW FOR A PRODUCT(done) *********//
    @GetMapping("/getReviewByProductId/{productId}")
    public List<ReviewDTO> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsByProductId(productId);
    }
    
    // *********** REVIEW EXCEPTION HANDLER handled by GlobalExceptionHandler *********//


    // #################################//
	// ********* ADMIN DASHBOARD *******//
    //----------------------------------//

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(adminStatsService.getDashboardStats());
    }

   
}
