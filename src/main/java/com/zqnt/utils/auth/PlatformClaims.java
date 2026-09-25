package com.zqnt.utils.auth;

import java.time.Instant;
import java.util.Set;

/**
 * The identity/tenant claims carried by a verified platform auth token — every service's notion of
 * "who is calling, for which organization, with which roles" comes from exactly this shape,
 * regardless of which identity provider actually issued the token (see the tenancy blueprint's
 * pluggable-IdP design: whatever mints the token, it resolves to this).
 *
 * <p>{@code organizationId} is {@code null} for a caller with no tenant of their own — today that's
 * every caller, since no issuer exists yet (see {@link PlatformTokenVerifier}); once auth is live
 * it's null only for {@link #isSystemAdmin()} callers, who aren't scoped to any single organization
 * by design.</p>
 */
public record PlatformClaims(
        String issuer,
        String subject,
        String organizationId,
        Set<String> roles,
        Instant issuedAt,
        Instant expiresAt,
        // Unique per issued token (null only for a token minted before this field existed — such a
        // token simply can't be individually revoked, it can still be killed platform-wide via a
        // security-stamp check on `subject`+`issuedAt`, and it naturally expires within its own
        // lifetime regardless). See admin-console's TokenRevocationService for how this is used.
        String jti) {

    /** The one role that bypasses tenant filtering everywhere — a platform operator, not a member
     * of any one customer's organization. Deliberately a single fixed role, not a permission bit
     * some other role could accumulate into by accident.
     *
     * <p>Stays a plain string literal (not derived from {@link PlatformRole#SYSTEM_ADMIN}'s
     * {@code wireValue()}) on purpose — every existing {@code @RequireRole(SYSTEM_ADMIN_ROLE)}
     * usage needs this to be a compile-time constant expression, which a method call on an enum
     * constant, even a {@code static final}-assigned one, is not (javac: "element value must be a
     * constant expression"). {@link PlatformRole#SYSTEM_ADMIN} points back at this constant
     * instead, so the two still can't drift apart despite this constant being the literal. */
    public static final String SYSTEM_ADMIN_ROLE = "system_admin";

    /** Administers one organization's own users/seats/configuration — the role
     * {@code @RequireRole(ORG_ADMIN_ROLE)} names on the user-management endpoints so an org-admin
     * caller passes too (a {@code system_admin} caller always passes regardless of which role is
     * named — see {@link com.zqnt.core.adminconsole.security.RoleCheckFilter} — so naming this one
     * doesn't narrow who system_admin can still reach). Same "literal constant, not a method call"
     * reasoning as {@link #SYSTEM_ADMIN_ROLE} — {@link PlatformRole#ORG_ADMIN} points back at this
     * constant instead of the reverse, so the two can't drift apart. */
    public static final String ORG_ADMIN_ROLE = "org-admin";

    public PlatformClaims {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isSystemAdmin() {
        return hasRole(SYSTEM_ADMIN_ROLE);
    }
}
