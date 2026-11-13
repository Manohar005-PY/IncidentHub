package com.manohar.incidenthub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.manohar.incidenthub.exception.EmailAlreadyRegisteredException;
import com.manohar.incidenthub.exception.InvalidRegistrationRequestException;
import com.manohar.incidenthub.model.AppUser;
import com.manohar.incidenthub.model.UserRole;
import com.manohar.incidenthub.repositroy.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldNormalizeEmailEncodePasswordAndRegisterResponder() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("long-enough-password")).thenReturn("{bcrypt}encoded");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser registered = authService.register("  USER@Example.COM  ", "long-enough-password");

        assertEquals("user@example.com", registered.getEmail());
        assertEquals("{bcrypt}encoded", registered.getPasswordHash());
        assertEquals(UserRole.RESPONDER, registered.getRole());
        verify(userRepository).existsByEmail("user@example.com");
        verify(passwordEncoder).encode("long-enough-password");
        verify(userRepository).save(registered);
    }

    @Test
    void shouldRejectDuplicateEmailBeforeEncodingPassword() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authService.register("USER@example.com", "long-enough-password"));

        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void shouldAcceptPasswordAt72Utf8Bytes() {
        String password = "🔐".repeat(18);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("{bcrypt}encoded");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser registered = authService.register("user@example.com", password);

        assertEquals("{bcrypt}encoded", registered.getPasswordHash());
        verify(passwordEncoder).encode(password);
    }

    @Test
    void shouldRejectPasswordAbove72Utf8BytesBeforeEncoding() {
        String password = "🔐".repeat(19);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);

        InvalidRegistrationRequestException exception = assertThrows(
                InvalidRegistrationRequestException.class,
                () -> authService.register("user@example.com", password));

        assertEquals("Password must not exceed 72 UTF-8 bytes", exception.getMessage());
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(AppUser.class));
    }
}
