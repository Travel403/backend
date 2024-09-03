package com.tripj.domain.user.service;

import com.tripj.domain.user.model.dto.request.UpdateNicknameRequest;
import com.tripj.domain.user.model.dto.response.GetNicknameResponse;
import com.tripj.domain.user.model.dto.response.UpdateNicknameResponse;
import com.tripj.domain.user.model.entity.User;
import com.tripj.domain.user.repository.UserRepository;
import com.tripj.domain.user.repository.nickname.GenerateRandomNicknameRepository;
import com.tripj.global.error.exception.AuthenticationException;
import com.tripj.global.error.exception.BusinessException;
import com.tripj.global.error.exception.ForbiddenException;
import com.tripj.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.tripj.global.code.ErrorCode.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GenerateRandomNicknameRepository nicknameRepository;

    /**
     * 회원가입
     */
    public User registerUser(User user) {
        validateDuplicateUser(user);
        return userRepository.save(user);
    }

    /**
     * 회원 중복 검사
     */
    @Transactional(readOnly = true)
    public void validateDuplicateUser(User user) {
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());

        if (optionalUser.isPresent()) {
            throw new BusinessException(ALREADY_REGISTERED_USER);
        }
    }

    /**
     * 이메일로 회원 찾기
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 랜덤 닉네임 생성
     */
    public String generateRandomNickname() {
        while (true) {
            String nickname = nicknameRepository.generate();
            if (!userRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }
    }

    /**
     * 사용자 닉네임 조회
     */
    @Transactional(readOnly = true)
    public GetNicknameResponse getNickname(Long userId) {

        User user = userRepository.findNicknameById(userId)
            .orElseThrow(() -> new NotFoundException(E404_NOT_EXISTS_USER));

        return GetNicknameResponse.of(user.getNickname());
    }

    /**
     * 사용자 닉네임 수정
     */
    public UpdateNicknameResponse updateNickname(UpdateNicknameRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(E404_NOT_EXISTS_USER));

        if (!user.getId().equals(userId)) {
            throw new ForbiddenException(E403_NOT_MY_NICKNAME);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ALREADY_EXISTED_NICKNAME);
        }

        user.updateNickname(request.getNickname());

        return UpdateNicknameResponse.of(user.getNickname());
    }

    /**
     * accessToken 재발급
     */
    @Transactional(readOnly = true)
    public User findUserByRefreshToken(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new AuthenticationException(REFRESH_TOKEN_NOT_FOUND));

        LocalDateTime tokenExpireTime = user.getTokenExpirationTime();
        if (tokenExpireTime.isBefore(LocalDateTime.now())) {
            throw new AuthenticationException(FORBIDDEN_REFRESH_TOKEN_EXPIRED);
        }

        return user;
    }
}
