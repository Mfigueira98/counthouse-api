package io.github.mfigueira98.counthouse.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("select u from User u where lower(u.email) = lower(:email)")
    Optional<User> findForLoginByEmail(@Param("email") String email);
    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);
}
