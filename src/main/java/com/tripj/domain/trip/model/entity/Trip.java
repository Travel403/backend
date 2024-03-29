package com.tripj.domain.trip.model.entity;

import com.tripj.domain.common.entity.BaseTimeEntity;
import com.tripj.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Trip extends BaseTimeEntity {

    @Id
    @Column(name = "trip_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

//    @Embedded
//    private Period TripPeriod;

    private LocalDate startDate;

    private LocalDate endDate;

    private String tripName;

    private String purpose;

    private String previous;

    public static Trip newTrip(String tripName, String purpose, String previous,
                               LocalDate startDate, LocalDate endDate) {
        return Trip.builder()
                .tripName(tripName)
                .purpose(purpose)
                .previous("NOW")
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
