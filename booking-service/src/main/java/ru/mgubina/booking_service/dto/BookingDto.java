package ru.mgubina.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mgubina.booking_service.entity.Booking;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private Long userId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Booking.Status status;
    private LocalDateTime createdAt;
}
