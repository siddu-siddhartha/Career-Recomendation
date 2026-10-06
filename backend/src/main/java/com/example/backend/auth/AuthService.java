package com.example.backend.auth;

import com.example.backend.profile.UserProfile;
import com.example.backend.profile.UserProfileRepository;
import com.example.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserProfileRepository profiles;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserProfileRepository profiles, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.profiles = profiles;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request) {
        if (profiles.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        UserProfile profile = profiles.save(new UserProfile(
                request.email().trim().toLowerCase(), passwordEncoder.encode(request.password())));
        return responseFor(profile);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.email().trim().toLowerCase(), request.password()));
        return responseFor(profiles.findByEmailIgnoreCase(request.email()).orElseThrow());
    }

    private AuthDtos.AuthResponse responseFor(UserProfile profile) {
        return new AuthDtos.AuthResponse(jwtService.generateToken(profile.getEmail()),
                new AuthDtos.ProfileSummary(profile.getId(), profile.getEmail(), profile.getEducation()));
    }
}