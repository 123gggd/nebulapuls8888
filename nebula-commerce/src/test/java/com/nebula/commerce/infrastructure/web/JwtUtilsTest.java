package com.nebula.commerce.infrastructure.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    @Test
    void shouldGenerateAndParseToken() {
        JwtUtils jwtUtils = new JwtUtils("NebulaCommerceSecretKeyForDevelopmentPhase2026!", 60_000);
        String token = jwtUtils.generateToken("alice");

        assertNotNull(token);
        assertEquals("alice", jwtUtils.extractUsername(token));
        assertTrue(jwtUtils.validateToken(token, "alice"));
        assertFalse(jwtUtils.validateToken(token, "bob"));
    }

    @Test
    void shouldExpireToken() throws InterruptedException {
        JwtUtils jwtUtils = new JwtUtils("NebulaCommerceSecretKeyForDevelopmentPhase2026!", 20);
        String token = jwtUtils.generateToken("alice");

        Thread.sleep(30);

        assertFalse(jwtUtils.validateToken(token, "alice"));
    }
}
