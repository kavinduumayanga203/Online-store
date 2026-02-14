package com.store.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded product images from the local filesystem folder `product-images/`
        // e.g. a request to /product-images/1/photo.jpg will be served from
        // <project-working-dir>/product-images/1/photo.jpg
        registry.addResourceHandler("/product-images/**")
                .addResourceLocations("file:product-images/")
                .setCachePeriod(3600);
    }
}
