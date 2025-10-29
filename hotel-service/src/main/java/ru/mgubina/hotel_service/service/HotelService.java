package ru.mgubina.hotel_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.hotel_service.config.HotelDtoMapper;
import ru.mgubina.hotel_service.dto.CreateHotelRequest;
import ru.mgubina.hotel_service.dto.HotelDto;
import ru.mgubina.hotel_service.entity.Hotel;
import ru.mgubina.hotel_service.repository.HotelRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelDtoMapper hotelDtoMapper;

    @Transactional
    public HotelDto createHotel(CreateHotelRequest request) {
       Hotel savedHotel = hotelRepository.save(Hotel.builder()
                .name(request.getName())
                .address(request.getAddress())
                .build());
        return hotelDtoMapper.mapToDto(savedHotel);
    }

    public List<HotelDto> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotelDtoMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public HotelDto getHotelById(Long id) {
        log.info("Retrieving hotel by ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with ID: " + id));
        return hotelDtoMapper.mapToDto(hotel);
    }
}
