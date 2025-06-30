package com.auth.repository;

import com.auth.entity.RefreshToken;
import com.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    List<RefreshToken> findByUserAndRevokedAtIsNull(User user);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt, rt.revokedBy = :revokedBy WHERE rt.user = :user AND rt.revokedAt IS NULL")
    void revokeAllTokensForUser(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt,
            @Param("revokedBy") String revokedBy);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :expiredBefore")
    void deleteExpiredTokens(@Param("expiredBefore") LocalDateTime expiredBefore);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revokedAt IS NULL")
    long countValidTokensByUser(@Param("user") User user);
}