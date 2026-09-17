package com.labresource.platform.institution.service;

import com.labresource.platform.institution.Institution;

import java.util.List;

public interface InstitutionService {

    Institution createInstitution(Institution institution);

    Institution getInstitutionById(Long id);

    Institution getInstitutionByCode(String code);

    Institution getInstitutionByName(String name);

    List<Institution> listInstitutions();

    List<Institution> listInstitutionsByActiveStatus(boolean isActive);

    Institution updateInstitution(Long id, Institution updatedData);

    void activateInstitution(Long id);

    void deactivateInstitution(Long id);
}
