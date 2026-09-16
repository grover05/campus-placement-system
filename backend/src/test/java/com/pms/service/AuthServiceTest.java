package com.pms.service;

import com.pms.dto.Dtos.AuthResponse;
import com.pms.dto.Dtos.LoginRequest;
import com.pms.dto.Dtos.RegisterRequest;
import com.pms.exception.GlobalExceptionHandler.DuplicateResourceException;
import com.pms.model.User;
import com.pms.model.User.Role;
import com.pms.repository.RecruiterRepository;
import com.pms.repository.StudentRepository;
import com.pms.repository.UserRepository;
import com.pms.security.JwtSecurity.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private RecruiterRepository recruiterRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Jane Doe");
        registerRequest.setEmail("jane@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(Role.STUDENT);
    }

    @Test
    @DisplayName("Registering with a new email creates a user and returns a token")
    void register_newEmail_returnsAuthResponse() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals(Role.STUDENT, response.getRole());
        verify(studentRepository, times(1)).save(any());
        verify(recruiterRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registering with a duplicate email throws DuplicateResourceException")
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registering a recruiter also creates a recruiter profile")
    void register_recruiterRole_createsRecruiterProfile() {
        registerRequest.setRole(Role.RECRUITER);
        registerRequest.setCompanyName("Acme Corp");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

        authService.register(registerRequest);

        verify(recruiterRepository, times(1)).save(any());
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login with valid credentials returns a token")
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jane@example.com");
        loginRequest.setPassword("password123");

        User user = User.builder().id(1L).email("jane@example.com").fullName("Jane Doe").role(Role.STUDENT).build();

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("jane@example.com", response.getEmail());
    }

    @Test
    @DisplayName("Login with bad credentials throws BadCredentialsException")
    void login_invalidCredentials_throwsException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jane@example.com");
        loginRequest.setPassword("wrongpassword");

        doThrow(new org.springframework.security.authentication.BadCredentialsException("bad creds"))
                .when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
