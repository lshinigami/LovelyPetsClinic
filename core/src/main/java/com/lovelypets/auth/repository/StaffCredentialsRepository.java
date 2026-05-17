package com.lovelypets.auth.repository;

import com.lovelypets.entities.StaffCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffCredentialsRepository extends JpaRepository<StaffCredentials, Long> {

    Optional<StaffCredentials> findByEmail(String email);

    boolean existsByEmail(String email);
}
