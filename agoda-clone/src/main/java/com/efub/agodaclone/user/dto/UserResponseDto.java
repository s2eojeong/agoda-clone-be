package com.efub.agodaclone.user.dto;

import com.efub.agodaclone.user.domain.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}