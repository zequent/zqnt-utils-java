package com.zqnt.utils.auth;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The platform's real, fixed role vocabulary — every value a {@link PlatformClaims#roles()} entry
 * or a {@code UserEntity.roles} column is ever allowed to actually contain (see the tenancy
 * blueprint's Part B). Before this existed, {@code system_admin} was the only role anything checked
 * for; the rest ({@code org-admin}/{@code operator}/{@code approver}/{@code viewer}) were named only
 * in comments, with nothing validating that a caller-supplied role string was one of them.
 *
 * <p>Wire values intentionally keep the exact spellings already in use elsewhere in the codebase
 * ({@code "system_admin"} with an underscore predates this catalog; the rest use a hyphen) — this
 * enum doesn't renormalize them, it just gives the existing strings one canonical, validated home.</p>
 */
public enum PlatformRole {

    /** A platform operator, not a member of any one customer's organization — bypasses tenant
     * filtering everywhere (see {@link PlatformClaims#isSystemAdmin()}). Deliberately the only
     * {@link Scope#GLOBAL} role: every other role always belongs to exactly one organization.
     * Wire value points at {@link PlatformClaims#SYSTEM_ADMIN_ROLE} (not a literal here) so the
     * two can't drift apart — that constant, not this one, is the one every existing
     * {@code @RequireRole(...)} annotation needs to stay a compile-time constant expression. */
    SYSTEM_ADMIN(PlatformClaims.SYSTEM_ADMIN_ROLE, Scope.GLOBAL,
            "Platform operator. Not scoped to any single organization; bypasses tenant filtering."),

    /** Manages their own organization's users and seats — the caller of the user-management
     * endpoints (see {@link PlatformClaims#ORG_ADMIN_ROLE}, which this points at so the two can't
     * drift apart). Can create/list/reset-password/revoke-sessions for any user in its own
     * organization, including granting the {@code org-admin} role to someone else -- but never
     * {@code system_admin}, to anyone, under any circumstance (see
     * {@code UserAPIImpl#validateGrantableRoles}). */
    ORG_ADMIN(PlatformClaims.ORG_ADMIN_ROLE, Scope.ORGANIZATION,
            "Administers one organization: its users, seats, and configuration."),

    /** Day-to-day operational access within one organization: runs/monitors missions and assets,
     * but doesn't manage who else has access. */
    OPERATOR("operator", Scope.ORGANIZATION,
            "Operates assets and missions within one organization."),

    /** Can approve gated steps (e.g. a capability graph's Human Approval node) within one
     * organization, without the broader operational or admin access {@code operator}/{@code
     * org-admin} carry. */
    APPROVER("approver", Scope.ORGANIZATION,
            "Approves gated actions (e.g. mission Human Approval steps) within one organization."),

    /** Read-only access within one organization. */
    VIEWER("viewer", Scope.ORGANIZATION,
            "Read-only access within one organization.");

    /** Whether a role belongs to exactly one organization, or (like {@link #SYSTEM_ADMIN}) to none. */
    public enum Scope { GLOBAL, ORGANIZATION }

    private final String wireValue;
    private final Scope scope;
    private final String description;

    PlatformRole(String wireValue, Scope scope, String description) {
        this.wireValue = wireValue;
        this.scope = scope;
        this.description = description;
    }

    public String wireValue() {
        return wireValue;
    }

    public Scope scope() {
        return scope;
    }

    public String description() {
        return description;
    }

    /** Looks up a role by its exact wire spelling (case-sensitive — role strings are compared
     * exactly everywhere else in the platform, e.g. {@link PlatformClaims#hasRole}, so a catalog
     * lookup that silently case-folded would be inconsistent with every other role check). */
    public static Optional<PlatformRole> fromWireValue(String value) {
        if (value == null) return Optional.empty();
        for (PlatformRole role : values()) {
            if (role.wireValue.equals(value)) return Optional.of(role);
        }
        return Optional.empty();
    }

    public static boolean isValid(String wireValue) {
        return fromWireValue(wireValue).isPresent();
    }

    /** Every entry in {@code candidates} that isn't a known role's wire value, preserving input
     * order — empty when every candidate is valid. What {@code UserAdminService.createUser} uses
     * to reject an unknown role with a message naming exactly which ones are the problem, rather
     * than a generic "invalid roles" that makes the caller re-derive which of possibly several
     * values was the typo. */
    public static Set<String> unknownOf(Set<String> candidates) {
        if (candidates == null || candidates.isEmpty()) return Set.of();
        return candidates.stream().filter(c -> !isValid(c))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<String> allWireValues() {
        return Arrays.stream(values()).map(PlatformRole::wireValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
