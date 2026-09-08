package com.example.minishop.repository;

import com.example.minishop.constant.AuthProvider;
import com.example.minishop.constant.Role;
import com.example.minishop.entity.User;
import com.example.minishop.projection.AdminUserAnalytics;
import com.example.minishop.projection.AuthProviderStat;
import com.example.minishop.projection.MonthlyUserGrowth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByAuthProviderAndProviderId(
            AuthProvider authProvider,
            String providerId
    );

    long countByRole(Role role);

    long countByActive(Boolean active);

    long countByEmailVerified(Boolean emailVerified);

    @Query("""
    SELECT
        u.id AS userId,
        u.fullName AS fullName,
        u.email AS email,
        u.phone AS phone,
        u.role AS role,
        u.active AS active,
        u.emailVerified AS emailVerified,
        u.authProvider AS authProvider,
        u.createdAt AS createdAt
    FROM User u
    WHERE (
        :keyword IS NULL
        OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR u.phone LIKE CONCAT('%', :keyword, '%')
    )
    AND (
        :role IS NULL
        OR u.role = :role
    )
    AND (
        :active IS NULL
        OR u.active = :active
    )
    AND (
        :emailVerified IS NULL
        OR u.emailVerified = :emailVerified
    )
    ORDER BY u.createdAt DESC
""")
    Page<AdminUserAnalytics> findAdminUserAnalytics(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            @Param("active") Boolean active,
            @Param("emailVerified") Boolean emailVerified,
            @Param("authProvider") AuthProvider authProvider,
            Pageable pageable
    );

    @Query("""
    SELECT
        u.authProvider AS authProvider,
        COUNT(u.id) AS users
    FROM User u
    GROUP BY u.authProvider
    ORDER BY COUNT(u.id) DESC
""")
    List<AuthProviderStat> countUsersByAuthProvider();

    @Query("""
    SELECT
        MONTH(u.createdAt) AS month,
        COUNT(u.id) AS users
    FROM User u
    WHERE YEAR(u.createdAt) = :year
    GROUP BY MONTH(u.createdAt)
    ORDER BY MONTH(u.createdAt)
""")
    List<MonthlyUserGrowth> monthlyUserGrowth(
            @Param("year") Integer year
    );
}