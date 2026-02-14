package com.store.repository;

import com.store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. Requirement: Pagination
    // This allows you to say "Give me page 1, with 10 items"
    Page<Product> findAllByDeletedFalse(Pageable pageable);

    // 2. Requirement: Advanced Search (Beyond CRUD)
    // "Find products where the name contains 'XYZ'"
    Page<Product> findByNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);
}
