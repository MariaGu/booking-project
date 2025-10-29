package ru.mgubina.booking_service.config;

import org.mapstruct.Mapper;
import ru.mgubina.booking_service.dto.BookingDto;
import ru.mgubina.booking_service.entity.Booking;

@Mapper
public interface BookingDtoMapper {

    BookingDto convertToDto(Booking booking);
}
