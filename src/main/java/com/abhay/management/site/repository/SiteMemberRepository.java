package com.abhay.management.site.repository;

import com.abhay.management.site.entity.SiteMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SiteMemberRepository extends JpaRepository<SiteMember, UUID> {

    List<SiteMember> findAllBySiteId(UUID siteId);

    List<SiteMember> findAllByUserId(UUID userId);

    Optional<SiteMember> findBySiteIdAndUserId(UUID siteId, UUID userId);

    boolean existsBySiteIdAndUserId(UUID siteId, UUID userId);
}
