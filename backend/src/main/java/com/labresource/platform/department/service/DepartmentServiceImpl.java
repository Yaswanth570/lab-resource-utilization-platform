package com.labresource.platform.department.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final InstitutionRepository institutionRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 InstitutionRepository institutionRepository) {
        this.departmentRepository = departmentRepository;
        this.institutionRepository = institutionRepository;
    }

    @Override
    @Transactional
    public Department createDepartment(Long institutionId, Department department) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID is required to create a department");
        }
        if (department == null) {
            throw new InvalidOperationException("Department payload cannot be null");
        }
        if (department.getCode() == null || department.getCode().trim().isEmpty()) {
            throw new InvalidOperationException("Department code is required");
        }
        if (department.getName() == null || department.getName().trim().isEmpty()) {
            throw new InvalidOperationException("Department name is required");
        }

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", institutionId));

        String normalizedCode = department.getCode().trim().toUpperCase();
        String normalizedName = department.getName().trim();

        if (departmentRepository.existsByInstitutionIdAndCode(institutionId, normalizedCode)) {
            throw new DuplicateResourceException(String.format("Department with code '%s' already exists in institution '%s'",
                    normalizedCode, institution.getName()));
        }

        departmentRepository.findByInstitutionIdAndName(institutionId, normalizedName).ifPresent(d -> {
            throw new DuplicateResourceException(String.format("Department with name '%s' already exists in institution '%s'",
                    normalizedName, institution.getName()));
        });

        department.setCode(normalizedCode);
        department.setName(normalizedName);
        department.setInstitution(institution);

        return departmentRepository.save(department);
    }

    @Override
    public Department getDepartmentById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Department ID cannot be null");
        }
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    @Override
    public Department getDepartmentByInstitutionAndCode(Long institutionId, String code) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidOperationException("Department code cannot be null or blank");
        }
        return departmentRepository.findByInstitutionIdAndCode(institutionId, code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Department with code '%s' not found in institution id %d",
                        code.trim().toUpperCase(), institutionId)));
    }

    @Override
    public List<Department> listDepartmentsByInstitution(Long institutionId) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", "id", institutionId);
        }
        return departmentRepository.findByInstitutionId(institutionId);
    }

    @Override
    public List<Department> listActiveDepartmentsByInstitution(Long institutionId) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", "id", institutionId);
        }
        return departmentRepository.findByInstitutionIdAndIsActive(institutionId, true);
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, Department updatedData) {
        if (updatedData == null) {
            throw new InvalidOperationException("Updated department data cannot be null");
        }
        Department existing = getDepartmentById(id);
        Long institutionId = existing.getInstitution().getId();

        // Reject cross-institution relocation
        if (updatedData.getInstitution() != null && updatedData.getInstitution().getId() != null
                && !updatedData.getInstitution().getId().equals(institutionId)) {
            throw new InvalidOperationException("Department cannot be moved to a different institution");
        }

        if (updatedData.getCode() != null && !updatedData.getCode().trim().isEmpty()) {
            String newCode = updatedData.getCode().trim().toUpperCase();
            if (!newCode.equalsIgnoreCase(existing.getCode())
                    && departmentRepository.existsByInstitutionIdAndCode(institutionId, newCode)) {
                throw new DuplicateResourceException(String.format("Department with code '%s' already exists in institution id %d",
                        newCode, institutionId));
            }
            existing.setCode(newCode);
        }

        if (updatedData.getName() != null && !updatedData.getName().trim().isEmpty()) {
            String newName = updatedData.getName().trim();
            if (!newName.equalsIgnoreCase(existing.getName())) {
                departmentRepository.findByInstitutionIdAndName(institutionId, newName).ifPresent(d -> {
                    throw new DuplicateResourceException(String.format("Department with name '%s' already exists in institution id %d",
                            newName, institutionId));
                });
            }
            existing.setName(newName);
        }

        if (updatedData.getBillingAccountCode() != null) {
            existing.setBillingAccountCode(updatedData.getBillingAccountCode());
        }

        return departmentRepository.save(existing);
    }

    @Override
    @Transactional
    public void activateDepartment(Long id) {
        Department department = getDepartmentById(id);
        department.setActive(true);
        departmentRepository.save(department);
    }

    @Override
    @Transactional
    public void deactivateDepartment(Long id) {
        Department department = getDepartmentById(id);
        department.setActive(false);
        departmentRepository.save(department);
    }
}
