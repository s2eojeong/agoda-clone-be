package com.efub.agodaclone.review.service;

import com.efub.agodaclone.global.exception.AgodaException;
import com.efub.agodaclone.global.exception.ExceptionCode;
import com.efub.agodaclone.reservation.domain.Reservation;
import com.efub.agodaclone.reservation.service.ReservationService;
import com.efub.agodaclone.review.domain.Review;
import com.efub.agodaclone.review.dto.request.ReviewCreateRequest;
import com.efub.agodaclone.review.repository.ReviewRepository;
import com.efub.agodaclone.user.domain.User;
import com.efub.agodaclone.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReservationService reservationService;
    private final UserService userService;

    @Transactional
    public Long addReview(ReviewCreateRequest reviewCreateRequest){
        User user = userService.getCurrentUser();
        Long reservationId = reviewCreateRequest.getReservationId();
        Reservation reservation = reservationService.findReservationById(reservationId);
        authorizeWriteUser(user, reservation);
        Review newReview = Review.create(reservation, reviewCreateRequest);
        reviewRepository.save(newReview);
        return newReview.getReviewId();
    }

    private void authorizeWriteUser(User user, Reservation reservation){
        if(user != reservation.getUser()){
            throw new AgodaException(ExceptionCode.UNAUTHORIZED);
        }
    }
}
