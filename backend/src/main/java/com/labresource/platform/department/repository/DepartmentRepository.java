package com.labresource.platform.department.repository;

import com.labresource.platform.department.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByInstitutionId(Long institutionId);

    List<Department> findByInstitutionIdAndIsActive(Long institutionId, boolean isActive);

    Optional<Department> findByInstitutionIdAndCode(Long institutionId, String code);

    Optional<Department> findByInstitutionIdAndName(Long institutionId, String name);

    boolean existsByInstitutionIdAndCode(Long institutionId, String code);
}
