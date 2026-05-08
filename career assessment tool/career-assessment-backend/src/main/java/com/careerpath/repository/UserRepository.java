package com.careerpath.repository;

import com.careerpath.model.User;
import com.careerpath.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole_Name(RoleName roleName);

    Page<User> findByRole_Name(RoleName roleName, Pageable pageable);

    long countByRole_Name(RoleName roleName);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :role AND u.createdAt >= :since")
    long countNewByRoleSince(RoleName role, LocalDateTime since);
}
