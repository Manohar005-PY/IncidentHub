package com.manohar.incidenthub.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.manohar.incidenthub.exception.EmailAlreadyRegisteredException;
import com.manohar.incidenthub.exception.InvalidRegistrationRequestException;
import com.manohar.incidenthub.model.AppUser;
import com.manohar.incidenthub.model.UserRole;
import com.manohar.incidenthub.service.AuthService;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        AppUser user = new AppUser("user@example.com", "{bcrypt}encoded", UserRole.RESPONDER);
        ReflectionTestUtils.setField(user, "id", 42L);
        when(authService.register("user@example.com", "long-enough-password")).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "user@example.com",
                          "password": "long-enough-password"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("RESPONDER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(authService).register("user@example.com", "long-enough-password");
    }

    @Test
    void shouldRejectInvalidRegistrationRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "not-an-email",
                          "password": "short"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void shouldMapDuplicateEmailToConflictResponse() throws Exception {
        when(authService.register("user@example.com", "long-enough-password"))
                .thenThrow(new EmailAlreadyRegisteredException());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "user@example.com",
                          "password": "long-enough-password"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists"));
    }

    @Test
    void shouldMapPasswordByteLimitFailureToBadRequest() throws Exception {
        String password = "🔐".repeat(19);
        when(authService.register("user@example.com", password))
                .thenThrow(new InvalidRegistrationRequestException("Password must not exceed 72 UTF-8 bytes"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "user@example.com",
                          "password": "%s"
                        }
                        """.formatted(password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must not exceed 72 UTF-8 bytes"));
    }

}
