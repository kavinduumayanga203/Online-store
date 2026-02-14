package com.store.config;

import com.store.entity.Product;
import com.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Configuration
public class ProductDataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) {
            return; // don't overwrite existing data
        }

        List<Product> seeds = List.of(
                createProduct("Camera Pro", "High quality camera", 699.99, "test-image.jpg", 10),
                createProduct("Vintage Lamp", "Stylish lamp", 49.99, "Screenshot 2025-11-07 at 22.30.23.png", 5),
                createProduct("Artwork", "Generated artwork", 12.00, "Gemini_Generated_Image_l8bxyll8bxyll8bx.png", 3)
        );

        for (Product p : seeds) {
            Product saved = productRepository.save(p);

            // If there is already an image under product-images/<saved.getId()>, do nothing.
            Path destDir = Paths.get("product-images", String.valueOf(saved.getId()));
            if (Files.exists(destDir) && Files.list(destDir).findAny().isPresent()) {
                continue;
            }

            // Look for the image file anywhere under product-images and copy it
            try {
                Path imagesRoot = Paths.get("product-images");
                if (Files.exists(imagesRoot)) {
                    // search for a file with same name
                    Files.walk(imagesRoot, 2)
                            .filter(pth -> pth.getFileName().toString().equals(p.getImageUrl()))
                            .findFirst()
                            .ifPresent(source -> {
                                try {
                                    Files.createDirectories(destDir);
                                    Files.copy(source, destDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    // ignore copy failures
                                }
                            });
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private Product createProduct(String name, String desc, double price, String imageUrl, int qty) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setImageUrl(imageUrl);
        p.setStockQuantity(qty);
        return p;
    }
}
