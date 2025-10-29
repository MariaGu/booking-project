package ru.mgubina.booking_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mgubina.booking_service.dto.AuthRequest;
import ru.mgubina.booking_service.dto.RegisterRequest;
import ru.mgubina.booking_service.dto.TokenResponse;
import ru.mgubina.booking_service.dto.UserDto;
import ru.mgubina.booking_service.entity.User;
import ru.mgubina.booking_service.service.JwtService;
import ru.mgubina.booking_service.service.UserService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserDto user = userService.registerUser(request);
        String token = jwtService.generateToken(convertToEntity(user));
        return ResponseEntity.ok(new TokenResponse(token, "Bearer"));
    }

    @PostMapping("/auth")
    public ResponseEntity<TokenResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        UserDto user = userService.authenticateUser(request.getUsername(), request.getPassword());
        String token = jwtService.generateToken(convertToEntity(user));
        return ResponseEntity.ok(new TokenResponse(token, "Bearer"));
    }

    private User convertToEntity(UserDto userDto) {
         return User.builder()
                .username(userDto.getUsername())
                .id(userDto.getId())
                .role(userDto.getRole())
                .build();
    }
}
