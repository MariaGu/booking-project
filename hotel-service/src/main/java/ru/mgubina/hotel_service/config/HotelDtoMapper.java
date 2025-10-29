package ru.mgubina.hotel_service.config;

import org.mapstruct.Mapper;
import ru.mgubina.hotel_service.dto.HotelDto;
import ru.mgubina.hotel_service.entity.Hotel;

@Mapper
public interface HotelDtoMapper {

    HotelDto mapToDto(Hotel hotel);
}
