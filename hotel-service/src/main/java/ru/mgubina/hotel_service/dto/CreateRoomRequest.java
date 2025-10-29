package ru.mgubina.hotel_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    @NotNull(message = "ID is required")
    private Long hotelId;

    @NotBlank(message = "Number is required")
    private String number;
}
