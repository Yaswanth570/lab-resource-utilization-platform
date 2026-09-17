package com.labresource.platform.user.repository;

import com.labresource.platform.user.User;
import com.labresource.platform.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByInstitutionId(Long institutionId);

    List<User> findByDepartmentId(Long departmentId);

    List<User> findByInstitutionIdAndStatus(Long institutionId, UserStatus status);

    List<User> findByInstitutionIdAndDepartmentId(Long institutionId, Long departmentId);
}
