package com.efub.agodaclone.review.domain;

import com.efub.agodaclone.global.entity.BaseEntity;
import com.efub.agodaclone.reservation.domain.Reservation;
import com.efub.agodaclone.review.dto.request.ReviewCreateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(length = 1000)
    private String content;

    @Column(name = "cleanliness_score", nullable = false)
    private int cleanlinessScore;

    @Column(name = "service_score", nullable = false)
    private int serviceScore;

    @Column(name = "location_score", nullable = false)
    private int locationScore;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> reviewImages = new ArrayList<>();

    @Builder
    public Review(String content, int cleanlinessScore, int serviceScore, int locationScore, Reservation reservation) {
        this.content = content;
        this.cleanlinessScore = cleanlinessScore;
        this.serviceScore = serviceScore;
        this.locationScore = locationScore;
        this.reservation = reservation;
        this.reviewImages = reviewImages;
    }

    public static Review create(Reservation reservation, ReviewCreateRequest request){
        Review review = Review.builder()
                .reservation(reservation)
                .locationScore(request.getLocationScore())
                .serviceScore(request.getServiceScore())
                .cleanlinessScore(request.getCleanScore())
                .content(request.getReviewText())
                .build();
        request.getReviewImages().forEach(url ->{
            ReviewImage img = ReviewImage.builder()
                    .reviewImage(url)
                    .build();
            review.addReviewImage(img);
        });
        return review;
    }

    // 리뷰 이미지 추가
    public void addReviewImage(ReviewImage reviewImage) {
        this.reviewImages.add(reviewImage);
        reviewImage.setReview(this);
    }

    // 리뷰 이미지 삭제
    public void removeReviewImage(ReviewImage reviewImage) {
        this.reviewImages.remove(reviewImage);
    }

    // 리뷰 내용 수정
    public void updateReviewContent(String content) {
        this.content = content;
    }

    public void updateCleanlinessScore(int cleanlinessScore) {
        this.cleanlinessScore = cleanlinessScore;
    }

    public void updateServiceScore(int serviceScore) {
        this.serviceScore = serviceScore;
    }

    public void updateLocationScore(int locationScore) {
        this.locationScore = locationScore;
    }

}
