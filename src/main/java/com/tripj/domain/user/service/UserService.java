package com.tripj.domain.user.service;

import com.tripj.domain.checklist.repository.CheckListRepository;
import com.tripj.domain.inquiry.repository.InquiryRepository;
import com.tripj.domain.item.repository.ItemRepository;
import com.tripj.domain.trip.repository.TripRepository;
import com.tripj.domain.user.model.dto.request.UpdateNicknameRequest;
import com.tripj.domain.user.model.dto.response.DeleteUserResponse;
import com.tripj.domain.user.model.dto.response.GetNicknameResponse;
import com.tripj.domain.user.model.dto.response.LogoutResponse;
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
    private final ItemRepository itemRepository;
    private final TripRepository tripRepository;
    private final InquiryRepository inquiryRepository;
    private final CheckListRepository checkListRepository;
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

        User user = userRepository.findNicknameAndProfileById(userId)
            .orElseThrow(() -> new NotFoundException(E404_NOT_EXISTS_USER));

        return GetNicknameResponse.of(user);
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
            .orElseThrow(() -> new AuthenticationException(E403_NOT_FOUND_REFRESH_TOKEN));

        LocalDateTime tokenExpireTime = user.getTokenExpirationTime();
        if (tokenExpireTime.isBefore(LocalDateTime.now())) {
            throw new AuthenticationException(E403_REFRESH_TOKEN_EXPIRED);
        }

        return user;
    }

    /**
     * 로그아웃
     */
    public LogoutResponse logout(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(E404_NOT_EXISTS_USER));

        LocalDateTime now = LocalDateTime.now();

        user.updateRefreshTokenNow(now);

        return LogoutResponse.of(user);
    }

    /**
     * 회원탈퇴
     */
    public DeleteUserResponse withdraw(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(E404_NOT_EXISTS_USER));

        checkListRepository.deleteByUserId(user.getId());
        itemRepository.deleteByUserId(user.getId());
        tripRepository.deleteByUserId(user.getId());
        inquiryRepository.deleteByUserId(user.getId());
        userRepository.deleteById(user.getId());

        return DeleteUserResponse.of(user.getId());
    }
}
