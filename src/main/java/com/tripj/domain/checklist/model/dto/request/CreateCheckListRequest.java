package com.tripj.domain.checklist.model.dto.request;

import com.tripj.domain.checklist.model.entity.CheckList;
import com.tripj.domain.item.constant.ItemType;
import com.tripj.domain.item.model.entity.FixedItem;
import com.tripj.domain.item.model.entity.Item;
import com.tripj.domain.trip.model.entity.Trip;
import com.tripj.domain.user.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCheckListRequest {

    @Schema(description = "아이템 Id", example = "1")
    private Long itemId;

    @Schema(description = "여행 Id", example = "1")
    private Long tripId;

    @Schema(description = "아이템 타입", example = "FIXED")
    private ItemType itemType;

    public CheckList toEntity(Item item, User user, Trip trip, ItemType itemType) {
        return CheckList.newCheckList(item, user, trip, itemType);
    }

    public CheckList toEntity(FixedItem item, User user, Trip trip, ItemType itemType) {
        return CheckList.newCheckList(item, user, trip, itemType);
    }


}
