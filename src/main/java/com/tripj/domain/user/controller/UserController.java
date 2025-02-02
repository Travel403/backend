package com.tripj.domain.user.controller;

import com.tripj.domain.user.model.dto.request.UpdateNicknameRequest;
import com.tripj.domain.user.model.dto.response.DeleteUserResponse;
import com.tripj.domain.user.model.dto.response.GetNicknameResponse;
import com.tripj.domain.user.model.dto.response.LogoutResponse;
import com.tripj.domain.user.model.dto.response.UpdateNicknameResponse;
import com.tripj.domain.user.service.UserService;
import com.tripj.global.model.RestApiResponse;
import com.tripj.resolver.userinfo.UserInfo;
import com.tripj.resolver.userinfo.UserInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "user", description = "사용자 API")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "사용자 닉네임, 프로필 사진 조회 API",
            description = "사용자 닉네임, 프로필 사진을 조회합니다."
    )
    @GetMapping("")
    public RestApiResponse<GetNicknameResponse> getNickname(
            @UserInfo UserInfoDto userInfo) {

        return RestApiResponse.success(
                userService.getNickname(userInfo.getUserId()));
    }

    @Operation(
            summary = "사용자 닉네임 수정 API",
            description = "사용자 닉네임을 수정합니다."
    )
    @PostMapping("")
    public RestApiResponse<UpdateNicknameResponse> updateNickName(
            @RequestBody @Valid UpdateNicknameRequest request,
            @UserInfo UserInfoDto userInfo) {

        return RestApiResponse.success(
                userService.updateNickname(request, userInfo.getUserId()));
    }

    @Operation(
            summary = "사용자 로그아웃 API",
            description = "리프레시 토큰을 만료시켜 로그아웃합니다."
    )
    @PostMapping("/logout")
    public RestApiResponse<LogoutResponse> logout(
            @UserInfo UserInfoDto userInfo) {

        return RestApiResponse.success(
                userService.logout(userInfo.getUserId()));
    }

    @Operation(
            summary = "사용자 회원탈퇴 API",
            description = "회원 탈퇴를 합니다. 사용자의 데이터를 모두 삭제합니다." +
                    " 데이터 보존 기간은 없습니다."
    )
    @PostMapping("/withdraw")
    public RestApiResponse<DeleteUserResponse> withdraw(
            @UserInfo UserInfoDto userInfo) {

        return RestApiResponse.success(
                userService.withdraw(userInfo.getUserId()));
    }

}
