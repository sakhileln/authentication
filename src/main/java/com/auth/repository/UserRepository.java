package com.auth.repository;

import com.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);

    @Query("SELECT COUNT(u) FROM User u WHERE u.email = :email")
    long countByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.username = :username")
    long countByUsername(@Param("username") String username);
}