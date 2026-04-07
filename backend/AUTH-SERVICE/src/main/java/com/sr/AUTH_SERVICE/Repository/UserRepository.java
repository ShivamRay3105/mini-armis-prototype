package com.sr.AUTH_SERVICE.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sr.AUTH_SERVICE.DB.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}