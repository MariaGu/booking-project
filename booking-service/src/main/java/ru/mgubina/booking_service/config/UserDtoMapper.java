package ru.mgubina.booking_service.config;

import org.mapstruct.Mapper;
import ru.mgubina.booking_service.dto.UserDto;
import ru.mgubina.booking_service.entity.User;

@Mapper
public interface UserDtoMapper {

    UserDto convertToDto(User user);

}
