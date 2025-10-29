package ru.mgubina.hotel_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.hotel_service.config.RoomDtoMapper;
import ru.mgubina.hotel_service.dto.CreateRoomRequest;
import ru.mgubina.hotel_service.dto.RoomDto;
import ru.mgubina.hotel_service.entity.Hotel;
import ru.mgubina.hotel_service.entity.Room;
import ru.mgubina.hotel_service.repository.HotelRepository;
import ru.mgubina.hotel_service.repository.RoomRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final IdempotencyService idempotencyService;
    private final RoomDtoMapper roomDtoMapper;

    @Transactional
    public RoomDto createRoom(CreateRoomRequest request) {

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with ID: " + request.getHotelId()));

        Room savedRoom = roomRepository.save(Room.builder()
                .hotel(hotel)
                .number(request.getNumber())
                .build());
        return roomDtoMapper.mapToDto(savedRoom);
    }

    public List<RoomDto> getAllAvailableRooms() {
        return roomRepository.findByAvailableTrue().stream()
                .map(roomDtoMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<RoomDto> getRecommendedRooms() {
        return roomRepository.findAvailableRoomsOrderedByTimesBooked().stream()
                .map(roomDtoMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean confirmAvailability(Long roomId, String requestId) {
        if (idempotencyService.isProcessed(requestId)) {
            log.info("Request {} already processed - returning cached result (true)", requestId);
            return true;
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        if (!room.getAvailable()) {
            log.warn("Room {} is not available", roomId);
            return false;
        }

        idempotencyService.markAsProcessed(requestId);
        return true;
    }

    @Transactional
    public void releaseSlot(Long roomId, String requestId) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));
    }

    @Transactional
    public void incrementTimesBooked(Long roomId, String requestId) {
        String incrementKey = requestId + "-increment";
        if (idempotencyService.isProcessed(incrementKey)) {
            return;
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        room.setTimesBooked(room.getTimesBooked()+1);
        roomRepository.save(room);
        idempotencyService.markAsProcessed(incrementKey);
    }
}
