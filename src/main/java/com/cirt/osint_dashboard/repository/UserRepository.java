package com.cirt.osint_dashboard.repository;

import com.cirt.osint_dashboard.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User authentication
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Find user by username
    Optional<User> findByUsername(String username);

    // Check if username exists
    boolean existsByUsername(String username);
}