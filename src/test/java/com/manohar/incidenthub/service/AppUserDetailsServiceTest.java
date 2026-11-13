package com.manohar.incidenthub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.manohar.incidenthub.model.AppUser;
import com.manohar.incidenthub.model.UserRole;
import com.manohar.incidenthub.repositroy.UserRepository;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService userDetailsService;

    @Test
    void shouldNormalizeEmailAndReturnUserDetails() {
        AppUser appUser = new AppUser("user@example.com", "{bcrypt}encoded", UserRole.RESPONDER);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(appUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("  USER@Example.COM  ");

        assertEquals("user@example.com", userDetails.getUsername());
        assertEquals("{bcrypt}encoded", userDetails.getPassword());
        assertEquals("ROLE_RESPONDER", userDetails.getAuthorities().iterator().next().getAuthority());
        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void shouldThrowWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@example.com"));

        assertEquals("User not found", exception.getMessage());
    }
}
