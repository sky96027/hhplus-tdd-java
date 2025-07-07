package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service 행위 기반 테스트, 호출했는지만 검사")
public class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    private PointService pointService;


    @BeforeEach
    void setUp() {
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("사용자 ID를 입력받으면 해당 사용자의 포인트를 조회")
    void getUserPoint_ReturnPoint_WhenUserIdIsValid() {
        // given
        long userId = 1L;
        UserPoint expected = new UserPoint(userId, 1000L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(expected);

        // when
        UserPoint actual = pointService.getUserPoint(userId);

        // then
        assertNotNull(actual);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("사용자 ID와 금액을 입력받으면 포인트를 충전")
    void chargePoint_ShouldIncreaseUserPoint_WhenValidInput() {
        // given
        long userId = 1L;
        long beforeAmount = 2000L;
        long chargeAmount = 1000L;
        long afterAmount = beforeAmount + chargeAmount;

        UserPoint beforePoint = new UserPoint(userId, beforeAmount, System.currentTimeMillis());
        UserPoint afterPoint = new UserPoint(userId, afterAmount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(beforePoint);
        when(userPointTable.insertOrUpdate(userId, afterAmount)).thenReturn(afterPoint);

        // when
        UserPoint actual = pointService.chargePoint(userId, chargeAmount);

        // then
        assertThat(actual).isEqualTo(afterPoint);
    }

    @Test
    @DisplayName("사용자 ID와 금액을 입력받으면 포인트를 사용")
    void usePoint_ShouldDecreasePoint_WhenValidInput() {
        // given
        long userId = 1L;
        long beforeAmount = 2000L;
        long useAmount = 1000L;
        long afterAmount = beforeAmount - useAmount;

        UserPoint beforePoint = new UserPoint(userId, beforeAmount, System.currentTimeMillis());
        UserPoint afterPoint = new UserPoint(userId, afterAmount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(beforePoint);
        when(userPointTable.insertOrUpdate(userId, afterAmount)).thenReturn(afterPoint);
        when(pointHistoryTable.insert(userId, useAmount, TransactionType.USE, afterPoint.updateMillis()))
                .thenReturn(new PointHistory(1L, userId, useAmount, TransactionType.USE, afterPoint.updateMillis()));

        // when
        UserPoint actual = pointService.usePoint(userId, useAmount);

        // then
        assertThat(actual).isEqualTo(afterPoint);

        // verify
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, times(1)).insertOrUpdate(userId, afterAmount);
        verify(pointHistoryTable, times(1)).insert(userId, useAmount, TransactionType.USE, afterPoint.updateMillis());
    }

    @Test
    @DisplayName("사용자 ID를 입력받으면 사용자의 포인트 사용/충전 내역 조회")
     void getUserHistories_ReturnHistories_WhenUserIdIsValid(){
        // given
        long userId = 1L;
        List<PointHistory> expected = List.of(
            new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(2L, userId, 2000L, TransactionType.CHARGE, System.currentTimeMillis())
        );
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(expected);

        // when
        List<PointHistory> actual = pointService.getUserHistories(userId);

        // then
        assertThat(actual).isEqualTo(expected);

        // verify
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }


}
