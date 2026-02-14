package com.store.service;

import com.store.entity.Product;
import com.store.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock // This mocks the database interaction (Repository)
    private ProductRepository productRepository;

    @InjectMocks // This is the actual class we are testing
    private ProductService productService;

    @Test
    void testGetProductById_success() {
        // Arrange
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("Test Product");

        // When the repository's findById is called, return our mock product
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        // Act
        Product foundProduct = productService.getProductById(1L);

        // Assert
        assertEquals("Test Product", foundProduct.getName());
        // Verify that the repository method was actually called
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testSaveProduct() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("New Phone");

        // Act
        productService.saveProduct(newProduct);

        // Assert: Verify that the save method was called once
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    void testGetAllProducts_withPagination() {
        // Arrange
        List<Product> products = Arrays.asList(new Product(), new Product());
        Page<Product> mockPage = new PageImpl<>(products);
        PageRequest pageable = PageRequest.of(0, 5);

        when(productRepository.findAllByDeletedFalse(pageable)).thenReturn(mockPage);

        // Act
        Page<Product> resultPage = productService.getAllProducts(pageable);

        // Assert
        assertEquals(2, resultPage.getContent().size());
        verify(productRepository, times(1)).findAllByDeletedFalse(pageable);
    }
}
