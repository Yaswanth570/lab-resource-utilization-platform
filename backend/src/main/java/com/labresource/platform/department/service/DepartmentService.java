package com.labresource.platform.department.service;

import com.labresource.platform.department.Department;

import java.util.List;

public interface DepartmentService {

    Department createDepartment(Long institutionId, Department department);

    Department getDepartmentById(Long id);

    Department getDepartmentByInstitutionAndCode(Long institutionId, String code);

    List<Department> listDepartmentsByInstitution(Long institutionId);

    List<Department> listActiveDepartmentsByInstitution(Long institutionId);

    Department updateDepartment(Long id, Department updatedData);

    void activateDepartment(Long id);

    void deactivateDepartment(Long id);
}
