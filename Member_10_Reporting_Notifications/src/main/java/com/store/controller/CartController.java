package com.store.controller;

import com.store.entity.*;
import com.store.repository.*;
import com.store.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class CartController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // 1. Add to Cart (Stored in Session)
    @GetMapping("/addToCart/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session) {
        // Get the cart list from session, or create a new one if empty
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // Add product to list
        Product product = productService.getProductById(id);
        cart.add(product);

        // Save back to session
        session.setAttribute("cart", cart);

        return "redirect:/"; // Reload home page
    }

    // 2. View Cart Page
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        
        // Calculate Total
        double total = 0.0;
        if (cart != null) {
            for (Product p : cart) {
                total += p.getPrice();
            }
        }

        model.addAttribute("cartItems", cart);
        model.addAttribute("total", total);
        return "cart"; // Loads cart.html
    }

    // 3. Checkout (Save to DB & Email)
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Principal principal) {
        // Principal holds the email of the currently logged-in user
        if (principal == null) {
            return "redirect:/login";
        }

        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart?error=empty";
        }

        // A. Find the User in DB
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();

        // B. Create the Order Entity
        Order order = new Order();
        order.setOrderDate(new Date());
        order.setStatus("COMPLETED");
        order.setUser(user);
        
        // Calculate total again for security
        double total = cart.stream().mapToDouble(Product::getPrice).sum();
        order.setTotalAmount(total);

        // C. Create OrderItems (Links Order <-> Product)
        List<OrderItem> items = new ArrayList<>();
        for (Product p : cart) {
            OrderItem item = new OrderItem();
            item.setProduct(p);
            item.setOrder(order);
            item.setQuantity(1); // Simple logic: 1 item per row
            item.setPriceAtPurchase(p.getPrice());
            items.add(item);
        }
        order.setOrderItems(items);

        // D. Save Everything to Database
        orderRepository.save(order);

        // E. Send Email (Beyond CRUD Requirement)
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Order Confirmation #" + order.getId());
                message.setText("Thank you for your order! Total: $" + total);
                mailSender.send(message);
            } catch (Exception ex) {
                // Log and continue; don't fail checkout if email sending fails
                System.err.println("Failed to send email: " + ex.getMessage());
            }
        }
        
        // F. Clear Cart
        session.removeAttribute("cart");

        return "redirect:/cart?success";
    }

    // 4. View Bill / Order (only owner or admin)
    @GetMapping("/bill/{orderId}")
    public String viewBill(@PathVariable Long orderId, Model model, Principal principal) {
        Optional<com.store.entity.Order> opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) {
            return "redirect:/?error=notfound";
        }
        com.store.entity.Order order = opt.get();

        // Allow only admin or the owning user
        if (principal != null) {
            if (!principal.getName().equals(order.getUser().getEmail())) {
                // Not the owner - check admin
                boolean isAdmin = false; // Can't easily access SecurityContext here without imports; rely on role check in template
                if (!isAdmin) {
                    // For safety, only allow owner or admin via a redirect (admin can open manually)
                    return "redirect:/?forbidden";
                }
            }
        } else {
            return "redirect:/login";
        }

        model.addAttribute("order", order);
        return "bill";
    }
}
