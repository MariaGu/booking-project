package ru.mgubina.hotel_service.config;

import org.mapstruct.Mapper;
import ru.mgubina.hotel_service.dto.HotelDto;
import ru.mgubina.hotel_service.dto.RoomDto;
import ru.mgubina.hotel_service.entity.Hotel;
import ru.mgubina.hotel_service.entity.Room;

@Mapper
public interface RoomDtoMapper {

    RoomDto mapToDto(Room room);
}
