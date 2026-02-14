package com.store.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;

@SpringBootTest
public class ProductControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();
    }
    // import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
    // import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
    void testHomePageLoads() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("listProducts"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testShowNewProductForm_AsAdmin() throws Exception {
        mockMvc.perform(get("/showNewProductForm"))
            .andExpect(status().isOk())
            .andExpect(view().name("new_product"));
    }

    @Test
    void testProtectedRoute_AccessDenied() throws Exception {
        mockMvc.perform(get("/showNewProductForm"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    // 6. Test: File Upload (The "Beyond CRUD" Feature)
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSaveProduct_WithImageUpload() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/saveProduct")
                        .file(imageFile)
                        .with(csrf())
                        .param("name", "Camera")
                        .param("price", "500.00")
                        .param("description", "Best camera"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
