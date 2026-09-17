package com.labresource.platform.institution.repository;

import com.labresource.platform.institution.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    Optional<Institution> findByCode(String code);

    Optional<Institution> findByName(String name);

    List<Institution> findByIsActive(boolean isActive);

    boolean existsByCode(String code);

    boolean existsByName(String name);
}
