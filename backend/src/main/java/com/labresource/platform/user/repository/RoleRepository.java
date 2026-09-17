package com.labresource.platform.user.repository;

import com.labresource.platform.user.Role;
import com.labresource.platform.user.UserRoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(UserRoleType name);

    boolean existsByName(UserRoleType name);
}
