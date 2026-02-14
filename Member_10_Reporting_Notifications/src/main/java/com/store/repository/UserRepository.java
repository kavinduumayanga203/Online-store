package com.store.repository;

import com.store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data automatically generates the SQL for this method
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
}
