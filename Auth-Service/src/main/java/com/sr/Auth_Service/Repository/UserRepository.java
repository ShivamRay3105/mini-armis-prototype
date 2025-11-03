package com.sr.Auth_Service.Repository;

import org.springframework.stereotype.Repository;
import com.sr.Auth_Service.Model.Users;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username);
}
