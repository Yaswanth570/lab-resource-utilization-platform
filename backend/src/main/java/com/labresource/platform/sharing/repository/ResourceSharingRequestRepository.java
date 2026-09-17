package com.labresource.platform.sharing.repository;

import com.labresource.platform.sharing.ResourceSharingRequest;
import com.labresource.platform.sharing.SharingRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceSharingRequestRepository extends JpaRepository<ResourceSharingRequest, Long> {

    List<ResourceSharingRequest> findByRequestingInstitutionId(Long requestingInstitutionId);

    List<ResourceSharingRequest> findByOwnerInstitutionId(Long ownerInstitutionId);

    List<ResourceSharingRequest> findByRequestedByUserId(Long requestedByUserId);

    List<ResourceSharingRequest> findByStatus(SharingRequestStatus status);

    List<ResourceSharingRequest> findByOwnerInstitutionIdAndStatus(Long ownerInstitutionId, SharingRequestStatus status);

    List<ResourceSharingRequest> findByRequestingInstitutionIdAndStatus(Long requestingInstitutionId, SharingRequestStatus status);
}
