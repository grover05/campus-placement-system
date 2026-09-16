package com.pms.service;

import com.pms.dto.Dtos.AuthResponse;
import com.pms.dto.Dtos.LoginRequest;
import com.pms.dto.Dtos.RegisterRequest;
import com.pms.exception.GlobalExceptionHandler.DuplicateResourceException;
import com.pms.model.Recruiter;
import com.pms.model.Student;
import com.pms.model.User;
import com.pms.model.User.Role;
import com.pms.repository.RecruiterRepository;
import com.pms.repository.StudentRepository;
import com.pms.repository.UserRepository;
import com.pms.security.JwtSecurity.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final RecruiterRepository recruiterRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        user = userRepository.save(user);

        if (request.getRole() == Role.STUDENT) {
            Student student = Student.builder()
                    .user(user)
                    .backlogs(0)
                    .build();
            studentRepository.save(student);
        } else if (request.getRole() == Role.RECRUITER) {
            Recruiter recruiter = Recruiter.builder()
                    .user(user)
                    .companyName(request.getCompanyName())
                    .build();
            recruiterRepository.save(recruiter);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
