package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.PointService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("핵심 기능 테스트")
public class PointServiceTest {

    @Mock
    private UserPointTable uSerPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    private PointService pointService;


    @BeforeEach
    void setUp() {
        pointService = new PointService(uSerPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("사용자 ID를 입력받으면 해당 사용자의 포인트를 조회")
    void getUserPoint_ReturnPoint_WhenUserIdIsValid() {
        //given
        long userId = 1L;
        UserPoint expected = new UserPoint(userId, 1000L, System.currentTimeMillis());
        when(uSerPointTable.selectById(userId)).thenReturn(expected);

        //when
        UserPoint actual = pointService.getUserPoint(userId);

        //then
        assertNotNull(actual);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("사용자 ID와 금액을 입력받으면 포인트를 충전")
    void chargePoint_ShouldIncreaseUserPoint_WhenValidInput() {
        //given
        long userId = 1L;
        long amount = 1000L;
        UserPoint expected = new UserPoint(userId, 1000, System.currentTimeMillis());
        when(uSerPointTable.insertOrUpdate(userId, amount)).thenReturn(expected);

        //when
        UserPoint actual = pointService.chargePoint(userId, amount);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("사용자 ID와 금액을 입력받으면 포인트를 사용")
    void usePoint_ShouldDecreasePoint_WhenValidInput() {
        //given
        long userId = 1L;
        long amount = 1000L;
        UserPoint expected = new UserPoint(userId, -1000, System.currentTimeMillis());
        when(uSerPointTable.insertOrUpdate(userId, amount)).thenReturn(expected);

        //when
        UserPoint actual = pointService.usePoint(userId, amount);

        //then
        assertThat(actual).isEqualTo(expected);
    }
}
