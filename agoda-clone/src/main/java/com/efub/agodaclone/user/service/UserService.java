package com.efub.agodaclone.user.service;

import com.efub.agodaclone.user.domain.CustomUserDetails;
import com.efub.agodaclone.user.dto.KakaoUserResponseDto;
import com.efub.agodaclone.user.domain.User;
import com.efub.agodaclone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.efub.agodaclone.global.exception.ClientExceptionCode;


import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User registerOrLogin(KakaoUserResponseDto kakaoUserResponseDto) {
        String kakaoId = String.valueOf(kakaoUserResponseDto.getId());
        Optional<User> userOptional = userRepository.findByKakaoId(kakaoId);

        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            User newUser = User.builder()
                    .kakaoId(kakaoId)
                    .email(kakaoUserResponseDto.getKakaoAccount().getEmail())
                    .name(kakaoUserResponseDto.getKakaoAccount().getProfile().getNickname())
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        }
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException(ClientExceptionCode.UNAUTHORIZED.name());
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new RuntimeException(ClientExceptionCode.UNAUTH_ERROR.name());
        }

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException(ClientExceptionCode.RESOURCE_NOT_FOUND.name()));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        userRepository.delete(user);
    }


}
