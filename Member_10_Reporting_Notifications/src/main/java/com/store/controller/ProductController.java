package com.store.controller;

import com.store.entity.Product;
import com.store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.*;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. Show Home Page with Pagination & Search
    @GetMapping("/")
    public String viewHomePage(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword) {
        // Pagination: Get 5 items per page.
        // We use PageRequest to handle the limit and offset automatically.
        Page<Product> pageProducts;

        if (keyword != null && !keyword.isEmpty()) {
            pageProducts = productService.searchProducts(keyword, PageRequest.of(page, 5)); // Search
        } else {
            pageProducts = productService.getAllProducts(PageRequest.of(page, 5)); // Normal List
        }

        model.addAttribute("listProducts", pageProducts.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageProducts.getTotalPages());
        model.addAttribute("totalItems", pageProducts.getTotalElements());
        model.addAttribute("keyword", keyword); // Keep the search term in the box

        return "index"; // This loads index.html
    }

    // 2. Show "Add Product" Form
    @GetMapping("/showNewProductForm")
    public String showNewProductForm(Model model) {
        Product product = new Product();
        model.addAttribute("product", product);
        return "new_product"; // This loads new_product.html
    }

    // 5. View single product details
    // 5. View single product details
    @GetMapping("/product/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product_detail";
    }

    // 6. Show "Edit Product" Form
    @GetMapping("/showEditProductForm/{id}")
    public String showEditProductForm(@PathVariable(value = "id") Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "new_product"; // Reuse the new_product form for editing
    }

    // 3. Save Product (Handles Text + Image Upload)
    @PostMapping("/saveProduct")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
            BindingResult result,
            @RequestParam("image") MultipartFile multipartFile) throws IOException {

        // Validation Check (Source: 18)
        if (result.hasErrors()) {
            return "new_product"; // Return to form if there are errors
        }

        // File Upload Logic (Source: 38)
        if (!multipartFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            product.setImageUrl(fileName); // Save filename to DB

            Product savedProduct = productService.saveProduct(product); // Save to DB first to get ID

            // Save file to "product-images" folder
            String uploadDir = "product-images/" + savedProduct.getId();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (var inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            // If no new image, just save the text data
            productService.saveProduct(product);
        }

        return "redirect:/"; // Go back to home page
    }

    // 4. Delete Product
    @DeleteMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable(value = "id") Long id) {
        this.productService.deleteProductById(id);
        return "redirect:/";
    }
}
