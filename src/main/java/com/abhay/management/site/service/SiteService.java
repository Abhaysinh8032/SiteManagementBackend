package com.abhay.management.site.service;

import com.abhay.management.site.dto.SiteDto;
import com.abhay.management.site.entity.Site;
import com.abhay.management.site.entity.SiteMember;
import com.abhay.management.site.entity.User;
import com.abhay.management.site.repository.SiteMemberRepository;
import com.abhay.management.site.repository.SiteRepository;
import com.abhay.management.site.repository.UserRepository;
import com.abhay.management.site.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteService {

    private final SiteRepository       siteRepository;
    private final SiteMemberRepository siteMemberRepository;
    private final UserRepository       userRepository;

    // ── Get all active sites ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SiteDto.SiteResponse> getAllSites() {
        return siteRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Get sites for a specific worker ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SiteDto.SiteResponse> getSitesForUser(UUID userId) {
        return siteMemberRepository.findAllByUserId(userId)
                .stream()
                .map(SiteMember::getSite)
                .filter(Site::getIsActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Create site (admin only) ──────────────────────────────────────────────

    @Transactional
    public SiteDto.SiteResponse createSite(SiteDto.SiteRequest request, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        Site site = Site.builder()
                .name(request.getName())
                .location(request.getLocation())
                .description(request.getDescription())
                .isActive(true)
                .createdBy(admin)
                .build();

        site = siteRepository.save(site);
        log.info("Site created: {} by admin: {}", site.getName(), admin.getEmployeeId());
        return toResponse(site);
    }

    // ── Get single site ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SiteDto.SiteResponse getSiteById(UUID siteId) {
        Site site = findSiteById(siteId);
        return toResponse(site);
    }

    // ── Update site ───────────────────────────────────────────────────────────

    @Transactional
    public SiteDto.SiteResponse updateSite(UUID siteId, SiteDto.SiteRequest request) {
        Site site = findSiteById(siteId);
        site.setName(request.getName());
        site.setLocation(request.getLocation());
        site.setDescription(request.getDescription());
        return toResponse(siteRepository.save(site));
    }

    // ── Deactivate site ───────────────────────────────────────────────────────

    @Transactional
    public void deactivateSite(UUID siteId) {
        Site site = findSiteById(siteId);
        site.setIsActive(false);
        siteRepository.save(site);
        log.info("Site deactivated: {}", site.getName());
    }

    // ── Assign member to site ─────────────────────────────────────────────────

    @Transactional
    public SiteDto.MemberResponse assignMember(UUID siteId, UUID userId, UUID assignedByAdminId) {
        Site site = findSiteById(siteId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        User admin = userRepository.findById(assignedByAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign an inactive or pending user to a site.");
        }

        if (siteMemberRepository.existsBySiteIdAndUserId(siteId, userId)) {
            throw new IllegalArgumentException("User is already a member of this site.");
        }

        SiteMember member = SiteMember.builder()
                .site(site)
                .user(user)
                .assignedBy(admin)
                .build();

        member = siteMemberRepository.save(member);
        log.info("User {} assigned to site {} by {}", user.getEmployeeId(),
                site.getName(), admin.getEmployeeId());
        return toMemberResponse(member);
    }

    // ── Remove member from site ───────────────────────────────────────────────

    @Transactional
    public void removeMember(UUID siteId, UUID userId) {
        SiteMember member = siteMemberRepository.findBySiteIdAndUserId(siteId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found in this site."));
        siteMemberRepository.delete(member);
    }

    // ── Get all members of a site ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SiteDto.MemberResponse> getSiteMembers(UUID siteId) {
        findSiteById(siteId); // validate site exists
        return siteMemberRepository.findAllBySiteId(siteId)
                .stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Site findSiteById(UUID siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found."));
    }

    private SiteDto.SiteResponse toResponse(Site site) {
        int memberCount = siteMemberRepository.findAllBySiteId(site.getId()).size();
        return SiteDto.SiteResponse.builder()
                .id(site.getId())
                .name(site.getName())
                .location(site.getLocation())
                .description(site.getDescription())
                .isActive(site.getIsActive())
                .memberCount(memberCount)
                .createdByName(site.getCreatedBy().getName())
                .createdById(site.getCreatedBy().getId())
                .createdAt(site.getCreatedAt())
                .build();
    }

    private SiteDto.MemberResponse toMemberResponse(SiteMember member) {
        return SiteDto.MemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .employeeId(member.getUser().getEmployeeId())
                .name(member.getUser().getName())
                .role(member.getUser().getRole().name())
                .assignedAt(member.getAssignedAt())
                .build();
    }
}
