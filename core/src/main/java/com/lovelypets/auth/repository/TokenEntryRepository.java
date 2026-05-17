package com.lovelypets.auth.repository;

import com.lovelypets.entities.TokenEntry;
import com.lovelypets.enums.TokenType;
import com.lovelypets.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenEntryRepository extends JpaRepository<TokenEntry, Long> {

    Optional<TokenEntry> findByValue(String value);

    /** Находим все активные токены пользователя (нужно для revoke при новом логине). */
    List<TokenEntry> findAllByUserIdAndUserTypeAndRevokedFalse(Long userId, UserType userType);

    /** Массовый revoke всех токенов пользователя (например, при смене пароля). */
    @Modifying
    @Query("UPDATE TokenEntry t SET t.revoked = true WHERE t.userId = :userId AND t.userType = :userType AND t.revoked = false")
    void revokeAllByUserIdAndUserType(Long userId, UserType userType);

    /** Revoke конкретного типа (e.g. только ACCESS при рефреше). */
    @Modifying
    @Query("UPDATE TokenEntry t SET t.revoked = true WHERE t.userId = :userId AND t.userType = :userType AND t.tokenType = :tokenType AND t.revoked = false")
    void revokeAllByUserIdAndUserTypeAndTokenType(Long userId, UserType userType, TokenType tokenType);
}
