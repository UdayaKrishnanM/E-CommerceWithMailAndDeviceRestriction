package com.ecommerce.demo.service;

import com.ecommerce.demo.dto.ProductDTO;
import com.ecommerce.demo.exception.ProductNotFoundException;
import com.ecommerce.demo.model.Product;
import com.ecommerce.demo.model.ProductSize;
import com.ecommerce.demo.model.SizeType;
import com.ecommerce.demo.pagination.ProductSpecification;
import com.ecommerce.demo.repository.ProductRepository;
import com.ecommerce.demo.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
	
	
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
	public Page<ProductDTO> getFilteredProducts(
			String category,
			Double minPrice,
			Double maxPrice,
			String name,
			int page,
			int size,
			String sortBy,
			String sortDir
	) {
		Specification<Product> spec = ProductSpecification.getFilteredProducts(category, minPrice, maxPrice, name);

		Sort sort = sortDir.equalsIgnoreCase("desc") ?
				Sort.by(sortBy).descending() :
				Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		return productRepository.findAll(spec, pageable).map(this::toDTO);
	}

	private ProductDTO toDTO(Product product) {
		// map entity to DTO (can use MapStruct or manual mapping)
		return new ProductDTO(product.getName(), product.getDescription(), product.getPrice(), product.getCategory());
	}

    public Optional<ProductDTO> getProductById(Long id) {
    	Optional<Product> productList = productRepository.findById(id);
    	
    	if(productList.isEmpty()) {
//            throw new ProductNotFoundException("Product not found with ID: " + id);
			return null;
    	} else {
    		ProductDTO productDTO = new ProductDTO(productList.get().getName(),
    				productList.get().getDescription(), productList.get().getPrice(), productList.get().getCategory());
    		return Optional.of(productDTO);
    	}
  }
    
    public String addProduct(Product product) {
    	Optional<Product> productExists = productRepository.findByName(product.getName());
    	if(productExists.isPresent()) {
    		if(productExists.get().getDescription().equalsIgnoreCase(product.getDescription())) {    			
    			return "Product already added";
    		} else {
    			productRepository.save(product);
    			return "Added Products";
    		}
    	} else {
    		productRepository.save(product);
    		return "Added Products";    		
    	}
    }
    

    public String deleteProduct(Long id) {    	
    	Optional<Product> product = productRepository.findById(id);
    	if(product.isPresent()) {
    		productRepository.deleteById(product.get().getId());
    		return "Deleted Successfully ID: " + id;
    	} else {
    		return "Product Not Exists";
    	}
    	
	}

	public int getStockQuantity(Product product, SizeType requestedSize) {
		Optional<ProductSize> productSize = product.getSizes().stream()
				.filter(size -> size.getSize() == requestedSize)
				.findFirst();

		return productSize.map(ProductSize::getStockQuantity).orElse(0); // Default to 0 if size not found
	}


	public Optional<Product> updateProduct(Long id, Product productDetails, SizeType requestedSize, int newStockQuantity) {
		Optional<Product> productOptional = productRepository.findById(id);

		if (productOptional.isPresent()) {
			Product product = productOptional.get();

			// Update basic product details
			if (productDetails.getName() != null) {
				product.setName(productDetails.getName());
			}
			if (productDetails.getPrice() != 0) {
				product.setPrice(productDetails.getPrice());
			}
			if (productDetails.getDescription() != null) {
				product.setDescription(productDetails.getDescription());
			}
			if (productDetails.getCategory() != null) {
				product.setCategory(productDetails.getCategory());
			}

			// Find the specific size to update stock
			Optional<ProductSize> productSize = product.getSizes().stream()
					.filter(size -> size.getSize() == requestedSize)
					.findFirst();

			if (productSize.isPresent()) {
				productSize.get().setStockQuantity(newStockQuantity);
			} else {
				return Optional.empty(); // Size not found
			}

			productRepository.save(product);
			return Optional.of(product);
		} else {
			return Optional.empty(); // Product not found
		}
	}



}
