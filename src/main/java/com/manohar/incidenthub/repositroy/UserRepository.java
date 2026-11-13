package com.manohar.incidenthub.repositroy;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manohar.incidenthub.model.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
