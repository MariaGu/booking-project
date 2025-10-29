package ru.mgubina.booking_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mgubina.booking_service.client.HotelServiceClient;
import ru.mgubina.booking_service.config.BookingDtoMapper;
import ru.mgubina.booking_service.dto.BookingDto;
import ru.mgubina.booking_service.dto.CreateBookingRequest;
import ru.mgubina.booking_service.entity.Booking;
import ru.mgubina.booking_service.entity.User;
import ru.mgubina.booking_service.exception.RetryExhaustedException;
import ru.mgubina.booking_service.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelServiceClient hotelServiceClient;
    private final BookingDtoMapper bookingDtoMapper;

    @Transactional
    public BookingDto createBooking(CreateBookingRequest request, User user) {
        String requestId = UUID.randomUUID().toString();
        Booking booking = Booking.builder()
                .user(user)
                .roomId(request.getRoomId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Booking.Status.PENDING)
                .createdAt(LocalDateTime.now())
                .requestId(requestId)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        try {
            boolean confirmed = confirmAvailabilityWithRetry(request.getRoomId(), requestId);
            if (confirmed) {
                savedBooking.setStatus(Booking.Status.CONFIRMED);
                bookingRepository.save(savedBooking);
                incrementTimesBookedWithRetry(request.getRoomId(), requestId);
            } else {
                savedBooking.setStatus(Booking.Status.CANCELLED);
                bookingRepository.save(savedBooking);
            }
        } catch (RetryExhaustedException e) {
            savedBooking.setStatus(Booking.Status.CANCELLED);
            bookingRepository.save(savedBooking);
            try {
                releaseSlotWithRetry(request.getRoomId(), requestId);
            } catch (Exception compensationException) {
                log.error("Compensation failed for booking {}: {}", savedBooking.getId(), compensationException.getMessage());
            }
            log.error("Booking {} failed after retry exhaustion: {}", savedBooking.getId(), e.getMessage());
        } catch (Exception e) {
            savedBooking.setStatus(Booking.Status.CANCELLED);
            bookingRepository.save(savedBooking);

            try {
                releaseSlotWithRetry(request.getRoomId(), requestId);
            } catch (Exception compensationException) {
                log.error("Compensation failed for booking {}: {}", savedBooking.getId(), compensationException.getMessage());
            }
            log.error("Booking {} failed: {}", savedBooking.getId(), e.getMessage());
        }
        return bookingDtoMapper.convertToDto(savedBooking);
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private boolean confirmAvailabilityWithRetry(Long roomId, String requestId) {
        return hotelServiceClient.confirmAvailability(roomId, requestId);
    }

    @Recover
    private boolean confirmAvailabilityRecover(Exception ex, Long roomId, String requestId) {
        throw new RetryExhaustedException("Failed to confirm availability after all retry attempts", ex);
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void incrementTimesBookedWithRetry(Long roomId, String requestId) {
        hotelServiceClient.incrementTimesBooked(roomId, requestId);
    }

    @Recover
    private void incrementTimesBookedRecover(Exception ex, Long roomId, String requestId) {
        throw new RetryExhaustedException("Failed to increment times booked after all retry attempts", ex);
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void releaseSlotWithRetry(Long roomId, String requestId) {
        hotelServiceClient.releaseSlot(roomId, requestId);
    }

    @Recover
    private void releaseSlotRecover(Exception ex, Long roomId, String requestId) {
        log.error("All retry attempts exhausted for releaseSlot room {} with requestId: {}", roomId, requestId, ex);
    }

    public BookingDto getBookingById(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to booking: " + id);
        }
        return bookingDtoMapper.convertToDto(booking);
    }

    public List<BookingDto> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(bookingDtoMapper::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public void cancelBooking(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to booking: " + id);
        }

        if (booking.getStatus() == Booking.Status.CONFIRMED) {
            booking.setStatus(Booking.Status.CANCELLED);
            bookingRepository.save(booking);
        } else {
            log.warn("Booking {} cannot be cancelled - status: {}", id, booking.getStatus());
        }
    }
}
