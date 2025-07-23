package com.ecommerce.demo.controller;

import com.ecommerce.demo.dto.CartResponseDTO;
import com.ecommerce.demo.dto.ItemDTO;
import com.ecommerce.demo.dto.ProductDTO;
import com.ecommerce.demo.dto.ReviewDTO;
import com.ecommerce.demo.dto.UserOrderResponse;
import com.ecommerce.demo.exception.CartItemNotFoundException;
import com.ecommerce.demo.exception.OrderItemNotFoundException;
import com.ecommerce.demo.exception.OrderNotFoundException;
import com.ecommerce.demo.exception.ProductNotFoundException;
import com.ecommerce.demo.exception.ReviewNotFoundException;
import com.ecommerce.demo.model.CartItem;
import com.ecommerce.demo.model.Order;
import com.ecommerce.demo.model.Product;
import com.ecommerce.demo.model.Review;
import com.ecommerce.demo.service.CartItemService;
//import com.ecommerce.demo.service.OrderItemService;
import com.ecommerce.demo.service.OrderService;
import com.ecommerce.demo.service.ProductService;
import com.ecommerce.demo.service.ReviewService;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/api/user")
public class UserAccessController {
	
    @Autowired
    private ReviewService reviewService;
    private static final Logger logger = LoggerFactory.getLogger(UserAccessController.class);


//    @Autowired
//    private OrderItemService orderItemService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartItemService cartItemService;
    
    @Autowired
    private ProductService productService;

    
    
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
    // add searching product
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

    // *********** GET PRODUCT BY ID *********//
    @GetMapping("/getProductById/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<ProductDTO> data = productService.getProductById(id);
        if(data==null){
            return new ResponseEntity<>("Product with id: " + id + " not found....", HttpStatus.NOT_FOUND);
        }
    	return new ResponseEntity<>(data, HttpStatus.OK);
    }
    
    // ###################################//
	// ********* ORDER ENTITY ************//
    //------------------------------------//

    
    // *********** CREATE ORDER *********//
    // done
    @PostMapping("/createOrder")
    public ResponseEntity<Optional<UserOrderResponse>> createOrder(@RequestBody Order order) {
        
    	Optional<UserOrderResponse> orderDetails = orderService.createOrder(order);
    	if(!orderDetails.isEmpty()) {
    		return new ResponseEntity<Optional<UserOrderResponse>>(orderDetails, HttpStatus.OK);
    		
    	} else {
	  		throw new OrderNotFoundException("Order already exist with id : " + order);
    	}
    }
    
    
    // *********** DELETE ORDER (done) *********//
    @DeleteMapping("/deleteOrder/{id}")
    public ResponseEntity<String> deletOrder(@PathVariable Long id) {
        
    	String deleted =  orderService.deleteOrder(id);
    	if(deleted != "Order ID Not Exists") {
    		return new ResponseEntity<String>("Order id with " + id + " deleted succesfully" ,HttpStatus.OK);
    	} else {
    		throw new OrderNotFoundException("Order not found with id: " + id);
    	}
    }
    
    
    // *********** GET ALL ORDER (done) ********** //
    @GetMapping("/getAllOrdersByEmail/{email}")
    public ResponseEntity<List<UserOrderResponse>> getAllOrders(@PathVariable String email) {
        List<UserOrderResponse> ordersList = orderService.getAllOrdersByEmail(email);
        if(ordersList.isEmpty()) {
        	throw new OrderNotFoundException("No User Found in this ID");
        } else {
        	return new ResponseEntity<List<UserOrderResponse>>(ordersList, HttpStatus.OK);        
        }
        
    }
    
    // *********** GET ORDER BY ID NEW CODE (done)*********//
	@GetMapping("/getOrderById/{id}")
	public ResponseEntity<Optional<UserOrderResponse>> getOrderById(@PathVariable Long id) {
		 	
		Optional<UserOrderResponse> data = orderService.getOrderById(id);
	  	if(data.isPresent()) {
	  		return new ResponseEntity<Optional<UserOrderResponse>>(data, HttpStatus.OK);
	  	} else {
	  		throw new OrderNotFoundException("Order Not Found with id : " + id);
	  	}
	}
	
	
    // *********** ORDER EXCEPTION HANDLER *********//
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> handleOrderNotFoundException(OrderNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
	
	
    // ###################################//
	// ********* ORDER_ITEM ENTITY this is not neeeded to be accessed *******//
    //------------------------------------//

//    // *********** GET ALL ORDER_ITEM *********//
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
//    // *********** GET ORDER_ITEM BY ID (done) (auth needed to be added) *********//
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
//
//    @ExceptionHandler(OrderItemNotFoundException.class)
//    public ResponseEntity<String> handleOrderItemNotFoundException(OrderItemNotFoundException ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
//    }
    
    // ###################################//
	// ********* REVIEW ENTITY ***********//
    //------------------------------------//

    // *********** CREATE REVIEW (done) *********//
    @PostMapping("/createReview")
    public ResponseEntity<ReviewDTO> createReview(@RequestBody Review review) {
        return new ResponseEntity<ReviewDTO>(reviewService.createReview(review), HttpStatus.OK);
    }

    

    // *********** GET ALL REVIEW (done) *********//
    @GetMapping("/getReviewByUserId/{id}")
    public ResponseEntity<List<ReviewDTO>> getReviewByUser(@PathVariable Long id) {
        List<ReviewDTO> getAllReviews = reviewService.getMyReviews(id);
        if(getAllReviews.isEmpty()) {
     	   throw new ReviewNotFoundException("User didnt buy any product to review");
        } else {
     	   return new ResponseEntity<List<ReviewDTO>>(getAllReviews, HttpStatus.OK);
        }
     }

    // *********** GET ALL REVIEW FOR A PRODUCT(done) *********//
     @GetMapping("/getReviewByProductId/{productId}")
     public List<ReviewDTO> getReviewsByProduct(@PathVariable Long productId) {
         return reviewService.getReviewsByProductId(productId);
     }

    // *********** DELETE REVIEW BY ID (done) *********//
    @DeleteMapping("/deleteReviewByProductId/{productId}")
    public ResponseEntity<String> deleteReviewByProduct(
            @PathVariable Long productId,
            @RequestParam Long userId
    ) {
        String result = reviewService.deleteReviewByProductId(productId, userId);
        return ResponseEntity.ok(result);
    }


    // *********** UPDATE REVIEW BY ID (done) *********//
    // remove this endpoint bec we are using product and updating. not via review id @PutMapping("/updateReview/{id}")
    @PutMapping("/updateReviewByProductId/{productId}")
    public ResponseEntity<ReviewDTO> updateReviewByProductId(
            @PathVariable Long productId,
            @RequestParam Long userId,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) int rating
    ) {
        return reviewService.updateReviewByProductId(productId, userId, comment, rating)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ReviewNotFoundException("Update failed"));
    }


    // *********** REVIEW CODE EXCEPTION HANDLER *********//
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<String> handleReviewNotFoundException(ReviewNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }


    // ###############################//
	// ******* CART ITEM ENTITY ******//
    //--------------------------------//
    
    
    // *********** GET CART ITEMS (Done) *********//
    @GetMapping("/myCartItems")
    public ResponseEntity<CartResponseDTO> getCartItemByUserId() {
    	CartResponseDTO userOrderResponse = cartItemService.getCartItemByUserId();
    	 return ResponseEntity.ok(userOrderResponse);        
    }


    
    // *********** CREATE CART ITEMS (Done) *********//
    @PostMapping("/addcartItems")
    public ResponseEntity<String> addCartItem(@RequestBody CartItem cartItem) {

    	String msg =  cartItemService.addCartItem(cartItem);
    	if(msg == "Item added to cart successfully!") {
    		return new ResponseEntity<String>(msg, HttpStatus.OK);
    	} else if(msg == "Cart item updated with merged quantity!"){
            return new ResponseEntity<String>(msg,HttpStatus.OK);
        }else {
    		throw new CartItemNotFoundException(msg);
    	}   
    
    }
    
    
    // *********** UPDATE CART ITEMS (Done) *********//
    @PutMapping("/updateCartItemsById")
    public ResponseEntity<Optional<CartResponseDTO>> updateCartItem(@RequestBody CartItem cartItem) {
    	Optional<CartResponseDTO> item =  cartItemService.updateCartItem(cartItem);
    	if(item.isEmpty()) {
    		throw new CartItemNotFoundException("Cart Item not found");
    	} else {
    		return new ResponseEntity<Optional<CartResponseDTO>>(item,HttpStatus.OK);
    	}
    }

    
    // *********** DELETE CART ITEMS (Done) *********//
    @DeleteMapping("/deleteCartItemById/{id}")
    public ResponseEntity<String> deleteCartItem(@PathVariable Long id,@RequestBody CartItem cartItemUserDetails) {

    	String deleted =  cartItemService.deleteCartItem(id, cartItemUserDetails);
        logger.info("-----------" + deleted);
    	if(deleted == "Deleted successfully") {
    		return new ResponseEntity<String>("Cart Item id with " + id + " deleted succesfully", HttpStatus.OK);
    	} else {
    		throw new CartItemNotFoundException("Cart Item not found with id: " + id);
    	}
	}

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<String> handleOrderNotFoundException(CartItemNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
    

}

