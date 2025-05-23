package com.efub.agodaclone.user.controller;

import com.efub.agodaclone.global.exception.AgodaException;
import com.efub.agodaclone.global.exception.ExceptionCode;
import com.efub.agodaclone.user.dto.KakaoUserResponseDto;
import com.efub.agodaclone.user.domain.User;
import com.efub.agodaclone.user.jwt.JwtProvider;
import com.efub.agodaclone.user.service.KakaoService;
import com.efub.agodaclone.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final KakaoService kakaoService;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @GetMapping("/oauth/login")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {
        String accessToken = kakaoService.getAccessToken(code);
        KakaoUserResponseDto kakaoUserResponseDto = kakaoService.getUserInfo(accessToken);
        User user = userService.registerOrLogin(kakaoUserResponseDto);

        String jwt = jwtProvider.generateToken(user.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        // 쿠키로 access_token 설정
        ResponseCookie accessCookie = ResponseCookie.from("access_token", jwt)
                .path("/")
                .httpOnly(true) // JS에서 접근 불가능하게
                .secure(false)   // 지금은 테스트용이라 false로 해둠. 나중에 https에서는 true로!!
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .domain("localhost") //테스트용. 실제는 배포 url로 바꿔야 됨!
                .build();

        // refresh_token 쿠키 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .path("/oauth/reissue")
                .httpOnly(true)
                .secure(true)
                .maxAge(14 * 24 * 60 * 60) // 예: 14일
                .sameSite("Lax")
                .domain("localhost")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/oauth/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new AgodaException(ExceptionCode.REFRESH_TOKEN_EMPTY);
        }

        Long userId = jwtProvider.validateAndGetUserId(refreshToken);
        String newAccessToken = jwtProvider.generateToken(userId);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", newAccessToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(60 * 60) // 1시간
                .sameSite("Lax")
                .domain("localhost") // 실제 배포 시 도메인으로 변경
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        return ResponseEntity.ok().build();

    }



    @DeleteMapping("/oauth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        Long userId = (Long) request.getAttribute("userId");
        userService.deleteUser(userId);

        // access_token 쿠키 제거
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("Lax")
                .build();

        // refresh_token 쿠키 제거
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .path("/auth/reissue")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.noContent().build(); // 204 No Content
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refresh_token")) {
                return cookie.getValue();
            }
        }
        return null;
    }

    //////user service test용. 사용 후 삭제
    @GetMapping("/user/me")
    public ResponseEntity<?> getMyInfo() {
        User user = userService.getCurrentUser();

        return ResponseEntity.ok()
                .body("현재 유저: " + user.getName() + " (id: " + user.getUserId() + ")");
    }

}
