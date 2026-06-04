package com.abhay.management.site.controller;

import com.abhay.management.site.dto.AuthDto;
import com.abhay.management.site.dto.SiteDto;
import com.abhay.management.site.security.EmployeePrincipal;
import com.abhay.management.site.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    // ── GET /api/sites ─────────────────────────────────────────────────────────
    // Admin  → all active sites
    // Worker → only sites they are assigned to
    @GetMapping
    public ResponseEntity<List<SiteDto.SiteResponse>> getSites(
            @AuthenticationPrincipal EmployeePrincipal principal) {

        List<SiteDto.SiteResponse> sites;
        if ("ADMIN".equals(principal.getRole())) {
            sites = siteService.getAllSites();
        } else {
            sites = siteService.getSitesForUser(UUID.fromString(principal.getUserId()));
        }
        return ResponseEntity.ok(sites);
    }

    // ── POST /api/sites — admin only ───────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDto.SiteResponse> createSite(
            @Valid @RequestBody SiteDto.SiteRequest request,
            @AuthenticationPrincipal EmployeePrincipal principal) {

        SiteDto.SiteResponse site = siteService.createSite(
                request, UUID.fromString(principal.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(site);
    }

    // ── GET /api/sites/{id} ────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<SiteDto.SiteResponse> getSiteById(@PathVariable UUID id) {
        return ResponseEntity.ok(siteService.getSiteById(id));
    }

    // ── PUT /api/sites/{id} — admin only ──────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDto.SiteResponse> updateSite(
            @PathVariable UUID id,
            @Valid @RequestBody SiteDto.SiteRequest request) {
        return ResponseEntity.ok(siteService.updateSite(id, request));
    }

    // ── DELETE /api/sites/{id} — admin only (soft delete) ─────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthDto.ApiResponse> deactivateSite(@PathVariable UUID id) {
        siteService.deactivateSite(id);
        return ResponseEntity.ok(AuthDto.ApiResponse.ok("Site deactivated successfully."));
    }

    // ── GET /api/sites/{id}/members ────────────────────────────────────────────
    @GetMapping("/{id}/members")
    public ResponseEntity<List<SiteDto.MemberResponse>> getSiteMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(siteService.getSiteMembers(id));
    }

    // ── POST /api/sites/{id}/members — admin only ─────────────────────────────
    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDto.MemberResponse> assignMember(
            @PathVariable UUID id,
            @Valid @RequestBody SiteDto.AssignMemberRequest request,
            @AuthenticationPrincipal EmployeePrincipal principal) {

        SiteDto.MemberResponse member = siteService.assignMember(
                id,
                UUID.fromString(request.getUserId()),
                UUID.fromString(principal.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    // ── DELETE /api/sites/{siteId}/members/{userId} — admin only ─────────────
    @DeleteMapping("/{siteId}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthDto.ApiResponse> removeMember(
            @PathVariable UUID siteId,
            @PathVariable UUID userId) {
        siteService.removeMember(siteId, userId);
        return ResponseEntity.ok(AuthDto.ApiResponse.ok("Member removed from site."));
    }
}
