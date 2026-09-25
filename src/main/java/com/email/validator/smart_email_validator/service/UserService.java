package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.dto.request.UserCreateRequest;
import com.email.validator.smart_email_validator.dto.response.UserResponse;
import com.email.validator.smart_email_validator.entity.User;
import com.email.validator.smart_email_validator.enums.UserStatus;
import com.email.validator.smart_email_validator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository repository;

    public UserResponse create(UserCreateRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String username = request.username()
                .trim();

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Email already exists"
            );
        }

        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        User user = User.builder()
                .email(email)
                .username(username)
                .status(UserStatus.ACTIVE)
                .build();

        return toResponse(repository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + id
                        ));

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {

        User user = repository.findByEmailIgnoreCase(
                        email.trim()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}