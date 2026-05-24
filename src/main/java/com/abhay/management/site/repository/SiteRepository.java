package com.abhay.management.site.repository;

import com.abhay.management.site.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SiteRepository extends JpaRepository<Site, UUID> {

    List<Site> findAllByIsActiveTrue();

    List<Site> findAllByCreatedById(UUID adminId);
}
