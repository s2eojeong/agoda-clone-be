package com.efub.agodaclone.reservation.service;

import com.efub.agodaclone.accomodation.domain.Accommodation;
import com.efub.agodaclone.accomodation.service.AccommodationService;
import com.efub.agodaclone.global.exception.AgodaException;
import com.efub.agodaclone.global.exception.ExceptionCode;
import com.efub.agodaclone.reservation.domain.Reservation;
import com.efub.agodaclone.reservation.dto.ReservationConfirmationResponseDto;
import com.efub.agodaclone.reservation.dto.ReservationListResponseDto;
import com.efub.agodaclone.reservation.dto.ReservationRequestDto;
import com.efub.agodaclone.reservation.repository.ReservationRepository;
import com.efub.agodaclone.room.domain.Room;
import com.efub.agodaclone.room.service.RoomService;
import com.efub.agodaclone.user.domain.User;
import com.efub.agodaclone.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

    private final AccommodationService accommodationService;
    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final RoomService roomService;

    // 예약하기
    public ReservationConfirmationResponseDto addReservation(Long accommodationId, Long roomId, ReservationRequestDto requestDto) {
        User findUser = userService.getCurrentUser(); // 현재 로그인된 유저 찾아오기
        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new AgodaException(ExceptionCode.INVALID_DATE_RANGE);
        }
        Accommodation findAccommodation = accommodationService.findAccommodationById(accommodationId);
        Room findRoom = roomService.findByRoomId(roomId);
        Reservation reservation = requestDto.toEntity(findUser, findAccommodation, findRoom);
        reservationRepository.save(reservation);

        return ReservationConfirmationResponseDto.of(reservation);
    }

    // 나의 예약 전체 조회
    @Transactional(readOnly = true)
    public ReservationListResponseDto searchUpcomingReservations() {
        User findUser = userService.getCurrentUser(); // 현재 로그인된 유저 찾아오기
        List<Reservation> reservations = reservationRepository.findAllByUserOrderByStartDateAsc(findUser);
        // 오늘 날짜 가져오기
        LocalDate today = LocalDate.now();

        Reservation upcoming = reservations.stream()
                .filter(r -> !r.getStartDate().isBefore(today))  // startDate >= today 들 중 임박한 1개
                .min(Comparator.comparing(Reservation::getStartDate))
                .orElse(null);

        List<Reservation> completed = reservations.stream()
                .filter(r -> r.getStartDate().isBefore(today)) // startDate < today
                .collect(Collectors.toList());

        return ReservationListResponseDto.of(upcoming, completed, today, this::calculateStatus);
    }

    // 예약 상태 가져오는 함수
    private String calculateStatus(Reservation reservation, LocalDate today) {
        if (reservation.getEndDate().isBefore(today)) {
            return "체크아웃 완료";
        } else return "체크인 완료";
    }

    public Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(()->new AgodaException(ExceptionCode.RESOURCE_NOT_FOUND));
    }
}
