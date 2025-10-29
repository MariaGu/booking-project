package ru.mgubina.booking_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.booking_service.config.UserDtoMapper;
import ru.mgubina.booking_service.dto.RegisterRequest;
import ru.mgubina.booking_service.dto.UserDto;
import ru.mgubina.booking_service.entity.User;
import ru.mgubina.booking_service.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Nodes.collect;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDtoMapper userDtoMapper;

    @Transactional
    public UserDto registerUser(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        User savedUser = userRepository.save(User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build());
        return userDtoMapper.convertToDto(savedUser);
    }

    public UserDto authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password for user: " + username);
        }
        return userDtoMapper.convertToDto(user);
    }

    public List<UserDto> getAllUsers() {
        log.info("Retrieving all users");
        return userRepository.findAll().stream()
                .map(userDtoMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public User findByUsername(String username) {
        log.info("Finding user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
