package com.lovelypets.auth.repository;

import com.lovelypets.entities.ClientCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientCredentialsRepository extends JpaRepository<ClientCredentials, Long> {

    Optional<ClientCredentials> findByEmail(String email);

    boolean existsByEmail(String email);
}
