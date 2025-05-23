package com.efub.agodaclone.user.repository;

import com.efub.agodaclone.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //카카오 ID로 사용자 조회 (로그인/가입 시 사용)
    Optional<User> findByKakaoId(String kakaoId);
}
