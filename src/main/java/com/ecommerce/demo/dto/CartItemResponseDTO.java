package com.ecommerce.demo.dto;


public class CartItemResponseDTO {

    private ProductDTO product;

	private int quantity;
    
    
	public ProductDTO getProduct() {
		return product;
	}
	public void setProduct(ProductDTO product) {
		this.product = product;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public CartItemResponseDTO(ProductDTO product, int quantity) {
		super();
		this.product = product;
		this.quantity = quantity;
	}

	public CartItemResponseDTO() {
		super();
	}

    
    
}
