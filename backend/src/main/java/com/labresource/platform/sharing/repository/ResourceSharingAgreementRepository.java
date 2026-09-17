package com.labresource.platform.sharing.repository;

import com.labresource.platform.sharing.ResourceSharingAgreement;
import com.labresource.platform.sharing.SharingAgreementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceSharingAgreementRepository extends JpaRepository<ResourceSharingAgreement, Long> {

    Optional<ResourceSharingAgreement> findByAgreementCode(String agreementCode);

    Optional<ResourceSharingAgreement> findBySharingRequestId(Long sharingRequestId);

    List<ResourceSharingAgreement> findByRequestingInstitutionId(Long requestingInstitutionId);

    List<ResourceSharingAgreement> findByOwnerInstitutionId(Long ownerInstitutionId);

    List<ResourceSharingAgreement> findByStatus(SharingAgreementStatus status);

    boolean existsByAgreementCode(String agreementCode);
}
