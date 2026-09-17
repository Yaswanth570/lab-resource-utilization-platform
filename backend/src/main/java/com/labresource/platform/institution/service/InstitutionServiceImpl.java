package com.labresource.platform.institution.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepository;

    public InstitutionServiceImpl(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    @Override
    @Transactional
    public Institution createInstitution(Institution institution) {
        if (institution == null) {
            throw new InvalidOperationException("Institution payload cannot be null");
        }
        if (institution.getCode() == null || institution.getCode().trim().isEmpty()) {
            throw new InvalidOperationException("Institution code is required");
        }
        if (institution.getName() == null || institution.getName().trim().isEmpty()) {
            throw new InvalidOperationException("Institution name is required");
        }

        String normalizedCode = institution.getCode().trim().toUpperCase();
        String normalizedName = institution.getName().trim();

        if (institutionRepository.existsByCode(normalizedCode)) {
            throw new DuplicateResourceException("Institution", "code", normalizedCode);
        }
        if (institutionRepository.existsByName(normalizedName)) {
            throw new DuplicateResourceException("Institution", "name", normalizedName);
        }

        institution.setCode(normalizedCode);
        institution.setName(normalizedName);

        return institutionRepository.save(institution);
    }

    @Override
    public Institution getInstitutionById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        return institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", id));
    }

    @Override
    public Institution getInstitutionByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidOperationException("Institution code cannot be null or blank");
        }
        return institutionRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "code", code.trim().toUpperCase()));
    }

    @Override
    public Institution getInstitutionByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidOperationException("Institution name cannot be null or blank");
        }
        return institutionRepository.findByName(name.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "name", name.trim()));
    }

    @Override
    public List<Institution> listInstitutions() {
        return institutionRepository.findAll();
    }

    @Override
    public List<Institution> listInstitutionsByActiveStatus(boolean isActive) {
        return institutionRepository.findByIsActive(isActive);
    }

    @Override
    @Transactional
    public Institution updateInstitution(Long id, Institution updatedData) {
        if (updatedData == null) {
            throw new InvalidOperationException("Updated institution data cannot be null");
        }
        Institution existing = getInstitutionById(id);

        if (updatedData.getCode() != null && !updatedData.getCode().trim().isEmpty()) {
            String newCode = updatedData.getCode().trim().toUpperCase();
            if (!newCode.equalsIgnoreCase(existing.getCode()) && institutionRepository.existsByCode(newCode)) {
                throw new DuplicateResourceException("Institution", "code", newCode);
            }
            existing.setCode(newCode);
        }

        if (updatedData.getName() != null && !updatedData.getName().trim().isEmpty()) {
            String newName = updatedData.getName().trim();
            if (!newName.equalsIgnoreCase(existing.getName()) && institutionRepository.existsByName(newName)) {
                throw new DuplicateResourceException("Institution", "name", newName);
            }
            existing.setName(newName);
        }

        if (updatedData.getDomain() != null) {
            existing.setDomain(updatedData.getDomain().trim().toLowerCase());
        }
        if (updatedData.getAddressLine1() != null) {
            existing.setAddressLine1(updatedData.getAddressLine1());
        }
        if (updatedData.getAddressLine2() != null) {
            existing.setAddressLine2(updatedData.getAddressLine2());
        }
        if (updatedData.getCity() != null) {
            existing.setCity(updatedData.getCity());
        }
        if (updatedData.getState() != null) {
            existing.setState(updatedData.getState());
        }
        if (updatedData.getCountry() != null) {
            existing.setCountry(updatedData.getCountry());
        }
        if (updatedData.getPostalCode() != null) {
            existing.setPostalCode(updatedData.getPostalCode());
        }
        if (updatedData.getContactEmail() != null) {
            existing.setContactEmail(updatedData.getContactEmail());
        }
        if (updatedData.getContactPhone() != null) {
            existing.setContactPhone(updatedData.getContactPhone());
        }

        return institutionRepository.save(existing);
    }

    @Override
    @Transactional
    public void activateInstitution(Long id) {
        Institution institution = getInstitutionById(id);
        institution.setActive(true);
        institutionRepository.save(institution);
    }

    @Override
    @Transactional
    public void deactivateInstitution(Long id) {
        Institution institution = getInstitutionById(id);
        institution.setActive(false);
        institutionRepository.save(institution);
    }
}
