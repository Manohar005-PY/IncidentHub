package com.manohar.incidenthub.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manohar.incidenthub.exception.EmailAlreadyRegisteredException;
import com.manohar.incidenthub.exception.InvalidRegistrationRequestException;
import com.manohar.incidenthub.model.AppUser;
import com.manohar.incidenthub.model.UserRole;
import com.manohar.incidenthub.repositroy.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(String email, String rawPassword) {

        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException();
        }

        int passwordBytes = rawPassword
                .getBytes(StandardCharsets.UTF_8).length;

        if (passwordBytes > 72) {
            throw new InvalidRegistrationRequestException(
                    "Password must not exceed 72 UTF-8 bytes");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        AppUser user = new AppUser(
                normalizedEmail,
                encodedPassword,
                UserRole.RESPONDER);

        return userRepository.save(user);
    }
}
