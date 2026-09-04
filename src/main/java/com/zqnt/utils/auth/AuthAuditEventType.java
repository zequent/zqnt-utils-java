package com.zqnt.utils.auth;

/**
 * Shared vocabulary for {@code AuthAuditEventEntity.eventType} (connector) — plain string
 * constants, not a proto enum, so recording a new event type never needs a proto/codegen change
 * (see {@code RecordAuthAuditEventRequest.event_type}'s doc). Every producer (admin-console) and
 * the one consumer (connector's {@code AuthAuditService}) reference these instead of duplicating
 * the literal, so the two sides can't drift apart.
 */
public final class AuthAuditEventType {

    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILURE = "LOGIN_FAILURE";
    public static final String LOGOUT = "LOGOUT";
    public static final String PASSWORD_RESET = "PASSWORD_RESET";
    public static final String SESSIONS_REVOKED = "SESSIONS_REVOKED";

    private AuthAuditEventType() {
    }
}
