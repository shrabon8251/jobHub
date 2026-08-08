package com.example.jobhub.repository;

import com.example.jobhub.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.jobhub.entity.enums.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("""
            select u from User u
            where (:query = '' or lower(u.name) like lower(concat('%', :query, '%'))
                   or lower(u.email) like lower(concat('%', :query, '%')))
              and (:role is null or u.role = :role)
              and (:enabled is null or u.enabled = :enabled)
            """)
    Page<User> searchAdminUsers(@Param("query") String query, @Param("role") Role role,
                                @Param("enabled") Boolean enabled, Pageable pageable);

    long countByRole(Role role);
    long countByEnabled(boolean enabled);
}
