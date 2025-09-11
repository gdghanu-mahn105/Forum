package com.example.forum.repository;

import com.example.forum.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByProviderAndProviderId(String provider, String providerId);

    Optional<UserEntity> findById(Long id);

    Page<UserEntity> findByIsDeletedFalseAndUserNameContainingIgnoreCase(String keyword, Pageable pageable);


}
