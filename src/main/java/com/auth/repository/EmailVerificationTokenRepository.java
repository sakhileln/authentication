package com.auth.repository;

import com.auth.entity.EmailVerificationToken;
import com.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUserAndConfirmedAtIsNull(User user);

    @Query("SELECT evt FROM EmailVerificationToken evt WHERE evt.user = :user AND evt.confirmedAt IS NULL AND evt.expiresAt > :now ORDER BY evt.createdAt DESC")
    Optional<EmailVerificationToken> findValidTokenByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken evt WHERE evt.expiresAt < :expiredBefore")
    void deleteExpiredTokens(@Param("expiredBefore") LocalDateTime expiredBefore);

    @Modifying
    @Query("UPDATE EmailVerificationToken evt SET evt.confirmedAt = :confirmedAt WHERE evt.token = :token")
    void confirmToken(@Param("token") String token, @Param("confirmedAt") LocalDateTime confirmedAt);
}