package com.lovelypets.auth.repository;

import com.lovelypets.entities.OtpEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpEntryRepository extends JpaRepository<OtpEntry, Long> {

    Optional<OtpEntry> findByEmail(String email);

    void deleteByEmail(String email);
}
