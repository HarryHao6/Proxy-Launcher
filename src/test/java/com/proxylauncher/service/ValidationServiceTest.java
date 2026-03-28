package com.proxylauncher.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidationServiceTest {
    private final ValidationService validationService = new ValidationService();

    @Test
    public void acceptsSupportedProxyFormats() {
        assertFalse(validationService.validateProxy("http://127.0.0.1:7890").isPresent());
        assertFalse(validationService.validateProxy("https://localhost:8443").isPresent());
        assertFalse(validationService.validateProxy("socks5://proxy.example.com:1080").isPresent());
        assertFalse(validationService.validateProxy("").isPresent());
    }

    @Test
    public void rejectsUnsupportedProxyScheme() {
        Optional<String> error = validationService.validateProxy("ftp://127.0.0.1:21");

        assertTrue(error.isPresent());
        assertEquals("Proxy must start with http://, https://, or socks5://.", error.get());
    }

    @Test
    public void rejectsProxyWithoutHostOrPort() {
        Optional<String> missingHost = validationService.validateProxy("http://:7890");
        Optional<String> missingPort = validationService.validateProxy("http://127.0.0.1");

        assertTrue(missingHost.isPresent());
        assertEquals("Proxy must include a host.", missingHost.get());
        assertTrue(missingPort.isPresent());
        assertEquals("Proxy must include a valid port number.", missingPort.get());
    }

    @Test
    public void rejectsProxyWithExtraPathOrQuery() {
        Optional<String> pathError = validationService.validateProxy("http://127.0.0.1:7890/path");
        Optional<String> queryError = validationService.validateProxy("http://127.0.0.1:7890?x=1");

        assertTrue(pathError.isPresent());
        assertEquals("Proxy format must look like scheme://host:port.", pathError.get());
        assertTrue(queryError.isPresent());
        assertEquals("Proxy format must look like scheme://host:port.", queryError.get());
    }
}
