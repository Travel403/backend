package com.tripj.domain.checklist.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tripj.domain.checklist.model.dto.response.GetCheckListResponse;
import com.tripj.domain.checklist.model.dto.response.GetMyCheckListResponse;
import com.tripj.domain.checklist.model.dto.response.QGetCheckListResponse;
import com.tripj.domain.checklist.model.dto.response.QGetMyCheckListResponse;
import com.tripj.domain.item.constant.ItemType;
import com.tripj.domain.item.model.entity.QFixedItem;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tripj.domain.checklist.model.entity.QCheckList.*;
import static com.tripj.domain.item.model.entity.QFixedItem.fixedItem;
import static com.tripj.domain.item.model.entity.QItem.item;
import static com.tripj.domain.itemcate.model.entity.QItemCate.itemCate;
import static com.tripj.domain.trip.model.entity.QTrip.*;

@Repository
public class CheckListRepositoryCustomImpl implements CheckListRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public CheckListRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<GetCheckListResponse> getCheckList(Long userId, Long tripId) {
        List<GetCheckListResponse> results = queryFactory
                .select(new QGetCheckListResponse(
                        checkList.id,
                        Expressions.cases()
                                .when(checkList.item.isNotNull()).then(item.itemName)
                                .otherwise(fixedItem.itemName),
                        Expressions.cases()
                                .when(checkList.item.isNotNull()).then(item.itemCate.id)
                                .otherwise(fixedItem.itemCate.id),
                        checkList.pack
                ))
                .from(checkList)
                .leftJoin(item).on(checkList.item.id.eq(item.id))
                .leftJoin(fixedItem).on(checkList.fixedItem.id.eq(fixedItem.id))
                .join(trip).on(checkList.trip.id.eq(trip.id))
                .where(
                        checkList.user.id.eq(userId),
                        trip.id.eq(tripId)
                )
                .fetch();

        return results;
    }

}


