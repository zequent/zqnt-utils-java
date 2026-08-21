package com.zqnt.utils.auth;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformRoleTest {

    @Test
    void catalogCoversExactlyTheFiveBlueprintRoles() {
        assertEquals(Set.of("system_admin", "org-admin", "operator", "approver", "viewer"),
                PlatformRole.allWireValues());
    }

    @Test
    void fromWireValueFindsAKnownRole() {
        assertEquals(PlatformRole.ORG_ADMIN, PlatformRole.fromWireValue("org-admin").orElseThrow());
    }

    @Test
    void fromWireValueIsCaseSensitive() {
        assertTrue(PlatformRole.fromWireValue("Operator").isEmpty());
    }

    @Test
    void fromWireValueIsEmptyForNullOrUnknown() {
        assertTrue(PlatformRole.fromWireValue(null).isEmpty());
        assertTrue(PlatformRole.fromWireValue("not-a-role").isEmpty());
    }

    @Test
    void unknownOfReturnsOnlyTheInvalidOnesInInputOrder() {
        assertEquals(Set.of("bogus", "also-bogus"),
                PlatformRole.unknownOf(new java.util.LinkedHashSet<>(
                        Set.of("operator", "bogus", "viewer", "also-bogus"))));
    }

    @Test
    void unknownOfIsEmptyWhenEveryCandidateIsValid() {
        assertTrue(PlatformRole.unknownOf(Set.of("operator", "viewer")).isEmpty());
    }

    @Test
    void unknownOfIsEmptyForNullOrEmptyInput() {
        assertTrue(PlatformRole.unknownOf(null).isEmpty());
        assertTrue(PlatformRole.unknownOf(Set.of()).isEmpty());
    }

    @Test
    void systemAdminIsTheOnlyGlobalRole() {
        for (PlatformRole role : PlatformRole.values()) {
            if (role == PlatformRole.SYSTEM_ADMIN) {
                assertEquals(PlatformRole.Scope.GLOBAL, role.scope());
            } else {
                assertEquals(PlatformRole.Scope.ORGANIZATION, role.scope());
            }
        }
    }

    @Test
    void platformClaimsConstantMatchesTheCatalog() {
        assertEquals(PlatformRole.SYSTEM_ADMIN.wireValue(), PlatformClaims.SYSTEM_ADMIN_ROLE);
    }
}
