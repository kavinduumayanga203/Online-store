package com.store.service;

import com.store.entity.Product;
import com.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service // Tells Spring this holds business logic
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // 1. Create or Update a Product (CRUD)
    // Return the saved Product so callers can access generated IDs
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // 2. Read All Products with Pagination (CRUD + Requirement)
    public Page<Product> getAllProducts(Pageable pageable) {
        // This returns a "page" of products, excluding deleted ones
        return productRepository.findAllByDeletedFalse(pageable);
    }

    // 3. Search Products (Beyond CRUD Feature)
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndDeletedFalse(keyword, pageable);
    }

    // 4. Get a Single Product by ID (for editing)
    public Product getProductById(Long id) {
        Optional<Product> optional = productRepository.findById(id);
        Product product = null;
        if (optional.isPresent()) {
            product = optional.get();
        } else {
            throw new RuntimeException("Product not found for id :: " + id);
        }
        return product;
    }

    // 5. Delete Product (CRUD)
    // 5. Delete Product (CRUD) - SOFT DELETE
    public void deleteProductById(Long id) {
        Product product = getProductById(id);
        product.setDeleted(true);
        saveProduct(product);
    }
}
