package ru.mgubina.hotel_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mgubina.hotel_service.dto.CreateRoomRequest;
import ru.mgubina.hotel_service.dto.RoomDto;
import ru.mgubina.hotel_service.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoomDto> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllAvailableRooms() {
        return ResponseEntity.ok(roomService.getAllAvailableRooms());
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<RoomDto>> getRecommendedRooms() {
        return ResponseEntity.ok(roomService.getRecommendedRooms());
    }
}
