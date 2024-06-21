package com.tripj.domain.trip.service;

import com.tripj.batch.trip.TripBatchScheduler;
import com.tripj.batch.trip.TripDailyJob;
import com.tripj.domain.country.model.entity.Country;
import com.tripj.domain.country.repository.CountryRepository;
import com.tripj.domain.trip.model.dto.request.CreateTripRequest;
import com.tripj.domain.trip.model.dto.request.UpdateTripRequest;
import com.tripj.domain.trip.model.dto.response.CreateTripResponse;
import com.tripj.domain.trip.model.dto.response.GetTripResponse;
import com.tripj.domain.trip.model.dto.response.UpdateTripResponse;
import com.tripj.domain.trip.model.entity.Trip;
import com.tripj.domain.trip.repository.TripRepository;
import com.tripj.domain.user.constant.Role;
import com.tripj.domain.user.constant.UserType;
import com.tripj.domain.user.model.entity.User;
import com.tripj.domain.user.repository.UserRepository;
import com.tripj.global.error.exception.BusinessException;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TripServiceTest {
    
    @Autowired
    private TripService tripService;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CountryRepository countryRepository;

    @AfterEach
    void tearDown() {
        tripRepository.deleteAllInBatch();
        countryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("여행 계획을 최초 등록한다.")
    void createTripWhenBeforeExistingTrip() {
        // given
        Country country = createCountry();
        countryRepository.save(country);

        User user = createUser();
        userRepository.save(user);

        CreateTripRequest createTripRequest =
                createTripRequest(
                        "NOW",
                        country.getId(),
                        LocalDate.of(2022, 10, 1),
                        LocalDate.of(2022, 10, 10));

        // when
        CreateTripResponse trip = tripService.createTrip(createTripRequest, user.getId());

        // then
        assertNotNull(trip);
        assertEquals("즐거운 오사카 여행", trip.getTripName());
        assertEquals("여행", trip.getPurpose());
        assertEquals("NOW", trip.getPrevious());
        assertEquals(LocalDate.of(2022, 10, 1), trip.getStartDate());
        assertEquals(LocalDate.of(2022, 10, 10), trip.getEndDate());
    }

    @Test
    void 여행_계획은_EndDate가_끝나기_전까지_한개밖에_생성하지_못한다() {
        // given
        Country country = createCountry();
        countryRepository.save(country);

        User user = createUser();
        userRepository.save(user);

        CreateTripRequest createTripRequest =
                createTripRequest(
                        "NOW",
                        country.getId(),
                        LocalDate.of(2022, 10, 1),
                        LocalDate.of(2022, 10, 10));
        tripService.createTrip(createTripRequest, user.getId());

        // when // then
        assertThatThrownBy(() -> tripService.createTrip(createTripRequest, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 생성한 여행이 있습니다.");
    }

    @Test
    void EndDate가_지나면_여행_계획의_Previous는_B01_로_업데이트_된다() {
        // given
        Country country = createCountry();
        countryRepository.save(country);

        User user = createUser();
        userRepository.save(user);

        CreateTripRequest createTripRequest =
                createTripRequest(
                        "NOW",
                        country.getId(),
                        LocalDate.of(2022, 10, 1),
                        LocalDate.now());
        CreateTripResponse trip = tripService.createTrip(createTripRequest, user.getId());

        // when
        tripService.changeTripPrevious();

        // then
        Optional<Trip> updatedTrip = tripRepository.findById(trip.getTripId());
        assertNotNull(updatedTrip);
        assertEquals("즐거운 오사카 여행", updatedTrip.get().getTripName());
        assertEquals("여행", updatedTrip.get().getPurpose());
        assertEquals("B01", updatedTrip.get().getPrevious());
    }

    @Test
    void EndDate가_지나면_여행_계획의_Previous는_B01에서_순차적으로_증가한다() {
        // given
        Country country = createCountry();
        countryRepository.save(country);

        User user = createUser();
        userRepository.save(user);

        CreateTripRequest createTripRequest =
                createTripRequest(
                        "NOW",
                        country.getId(),
                        LocalDate.of(2022, 10, 1),
                        LocalDate.now());
        CreateTripResponse trip = tripService.createTrip(createTripRequest, user.getId());

        tripService.changeTripPrevious();

        CreateTripRequest createTripRequest2 =
                createTripRequest(
                        "NOW",
                        country.getId(),
                        LocalDate.of(2022, 10, 1),
                        LocalDate.now().plusDays(1));

        // when
        CreateTripResponse trip2 = tripService.createTrip(createTripRequest2, user.getId());

        // then
        Optional<Trip> updatedTrip = tripRepository.findById(trip2.getTripId());
        assertNotNull(updatedTrip);
        assertEquals("즐거운 오사카 여행", updatedTrip.get().getTripName());
        assertEquals("여행", updatedTrip.get().getPurpose());
        assertEquals("B02", updatedTrip.get().getPrevious());
    }

    @Test
    void 여행_계획을_수정한다() {
        //given
        Country country = createCountry();
        Country country2 = createCountry("홍콩", "HK");
        countryRepository.saveAll(List.of(country, country2));

        User user = createUser();
        userRepository.save(user);

        CreateTripRequest createTripRequest =
                createTripRequest(
                        "NOW",
                        country.getId(),
                        LocalDate.of(2022, 10, 1),
                        LocalDate.now());
        CreateTripResponse trip = tripService.createTrip(createTripRequest, user.getId());

        //when
        UpdateTripResponse updateTripResponse = tripService.updateTrip(
                UpdateTripRequest.builder()
                        .tripName("즐거운 홍콩 여행")
                        .purpose("사업")
                        .startDate(LocalDate.of(2022, 10, 1))
                        .endDate(LocalDate.now().plusDays(1))
                        .countryId(country2.getId())
                        .build(),
                trip.getTripId(),
                user.getId());

        //then
        assertEquals(updateTripResponse.getTripName(), "즐거운 홍콩 여행");
        assertEquals(updateTripResponse.getPurpose(), "사업");
        assertEquals(updateTripResponse.getCountryId(), country2.getId());
    }

    @Test
    void 여행_계획을_조회한다 ()  {
        //given
        Country country = createCountry();
        countryRepository.save(country);

        User user = createUser();
        userRepository.save(user);

        CreateTripRequest createTripRequest =
                createTripRequest(
                        "NOW",
                        country.getId(),
                        LocalDate.of(2022, 10, 1),
                        LocalDate.of(2025, 01, 1));
        tripService.createTrip(createTripRequest, user.getId());

        //when
        GetTripResponse trip = tripService.getTrip(user.getId());

        //then
        assertThat(trip.getTripName()).isEqualTo("즐거운 오사카 여행");
        assertThat(trip.getPurpose()).isEqualTo("여행");
        assertThat(trip.getCountryName()).isEqualTo("일본");
        assertThat(trip.getStartDate()).isEqualTo(LocalDate.of(2022, 10, 1));
        assertThat(trip.getEndDate()).isEqualTo(LocalDate.of(2025, 01, 1));
    }

    @Test
    void 지난_여행_계획을_조회한다 ()  {
        //given
        Country country = createCountry();
        countryRepository.save(country);

        User user = createUser();
        userRepository.save(user);

        CreateTripRequest createTripRequest =
                createTripRequest(
                        "B01",
                        country.getId(),
                        LocalDate.of(2022, 10, 1),
                        LocalDate.of(2022, 10, 2));
        tripService.createTrip(createTripRequest, user.getId());

        //when
        List<GetTripResponse> trip = tripService.getPastTrip(user.getId());

        //then
    }







    private CreateTripRequest createTripRequest(String previous, Long countryId, LocalDate startDate, LocalDate endDate) {
        return CreateTripRequest.builder()
                .tripName("즐거운 오사카 여행")
                .purpose("여행")
                .previous(previous)
                .startDate(startDate)
                .endDate(endDate)
                .countryId(countryId)
                .build();
    }

    private User createUser() {
        return User.builder()
                .userType(UserType.KAKAO)
                .email("asdf@naver.com")
                .nickname("다람지기엽지")
                .userName("홍길동")
                .role(Role.ROLE_USER)
                .build();
    }

    private Country createCountry() {
        return Country.builder()
                .name("일본")
                .code("JP")
                .build();
    }

    private Country createCountry(String name, String code) {
        return Country.builder()
                .name(name)
                .code(code)
                .build();
    }

}