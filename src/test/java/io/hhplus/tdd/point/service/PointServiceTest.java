package io.hhplus.tdd.point.service;

import io.hhplus.tdd.common.exception.UserNotFoundException;
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

import static io.hhplus.tdd.common.PointConstraints.MAX_POINT;
import static io.hhplus.tdd.common.PointConstraints.MAX_POINT_PER_CHARGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 해당 테스트 클래스는 PointService의 비즈니스 로직을 검증한다.
 */
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
        UserPoint actual = pointService.selectUserPoint(userId);

        // then
        assertNotNull(actual);
        assertThat(actual).isEqualTo(expected);

        //verify
        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    @DisplayName("사용자 ID와 금액을 입력받으면 포인트를 충전")
    void chargePoint_ShouldIncreaseUserPoint_WhenValidInput() {
        // given
        long userId = 1L;
        long beforeAmount = 1500L;
        long chargeAmount = 500L;
        long expectedAmount = beforeAmount + chargeAmount;

        UserPoint beforePoint = new UserPoint(userId, beforeAmount, System.currentTimeMillis());
        UserPoint expectedPoint = new UserPoint(userId, expectedAmount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(beforePoint);
        when(userPointTable.insertOrUpdate(userId, expectedAmount)).thenReturn(expectedPoint);
        when(pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE, expectedPoint.updateMillis()))
                .thenReturn(new PointHistory(1L, userId, chargeAmount, TransactionType.CHARGE, expectedPoint.updateMillis()));

        // when
        UserPoint actual = pointService.chargePoint(userId, chargeAmount);

        // then
        assertThat(actual).isEqualTo(expectedPoint);

        //verify
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, times(1)).insertOrUpdate(userId, expectedAmount);
        verify(pointHistoryTable, times(1)).insert(userId, chargeAmount, TransactionType.CHARGE, expectedPoint.updateMillis());
    }

    @Test
    @DisplayName("사용자 ID와 금액을 입력받으면 포인트를 사용")
    void usePoint_ShouldDecreasePoint_WhenValidInput() {
        // given
        long userId = 1L;
        long beforeAmount = 1500L;
        long useAmount = 500L;
        long expectedAmount = beforeAmount - useAmount;

        UserPoint beforePoint = new UserPoint(userId, beforeAmount, System.currentTimeMillis());
        UserPoint expectedPoint = new UserPoint(userId, expectedAmount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(beforePoint);
        when(userPointTable.insertOrUpdate(userId, expectedAmount)).thenReturn(expectedPoint);
        when(pointHistoryTable.insert(userId, useAmount, TransactionType.USE, expectedPoint.updateMillis()))
                .thenReturn(new PointHistory(1L, userId, useAmount, TransactionType.USE, expectedPoint.updateMillis()));

        // when
        UserPoint actual = pointService.usePoint(userId, useAmount);

        // then
        assertThat(actual).isEqualTo(expectedPoint);

        // verify
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, times(1)).insertOrUpdate(userId, expectedAmount);
        verify(pointHistoryTable, times(1)).insert(userId, useAmount, TransactionType.USE, expectedPoint.updateMillis());
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
        List<PointHistory> actual = pointService.selectUserHistories(userId);

        // then
        assertThat(actual).isEqualTo(expected);

        // verify
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    @Test
    @DisplayName("사용자의 포인트 잔량이 부족할 때 예외를 발생시킨다.")
    void usePoint_ShouldThrowException_WhenInsufficientPoint(){
        // given
        long userId = 1L;
        long beforeAmount = 2000L;
        long useAmount = 2001L;

        UserPoint beforePoint = new UserPoint(userId, beforeAmount, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(beforePoint);

        // when & then
        assertThatThrownBy(() -> pointService.usePoint(userId, useAmount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("보유 포인트(" + beforeAmount + "포인트)보다 많은 금액을 사용할 수 없습니다.");

        // verify
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("사용자가 1회 한도 이상으로 충전을 요청할 때 예외를 발생시킨다.")
    void chargePoint_ShouldThrowException_WhenExceedsChargeLimit(){
        // given
        long userId = 1L;
        long chargeAmount = 100_001L;

        when(userPointTable.selectById(userId)).thenReturn(
                new UserPoint(userId, 100_000L, System.currentTimeMillis())
        );

        // when & then
        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("1회 충전 한도(" + MAX_POINT_PER_CHARGE + "포인트)를 초과할 수 없습니다.");

        // verify
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("사용자가 충전 요청 시 충전 후 잔량이 최대 보유량을 초과할 때 예외를 발생시킨다.")
    void chargePoint_ShouldThrowException_WhenExceedsMaxTotalPoint() {
        // given
        long userId = 1L;
        long beforeAmount = 950_000L;
        long chargeAmount = 50_001L;

        UserPoint beforePoint = new UserPoint(userId, beforeAmount, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(beforePoint);

        // when & then
        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 보유 가능 포인트(" + MAX_POINT + "포인트)를 초과할 수 없습니다.");

        // verify
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());

    }

    @Test
    @DisplayName("충전 후 잔량이 최대 보유량과 정확히 같을 경우 성공한다. (1회 최대 충전량과 최대 보유량의 Edge Test)")
    void chargePoint_ShouldSucceed_WhenTotalPointEqualsMax() {
        // given
        long userId = 1L;
        long beforeAmount = 900_000L;
        long chargeAmount = 100_000L;
        long expectedAmount = 1_000_000L;

        UserPoint beforePoint = new UserPoint(userId, beforeAmount, System.currentTimeMillis());
        UserPoint afterPoint = new UserPoint(userId, expectedAmount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(beforePoint);
        when(userPointTable.insertOrUpdate(userId, expectedAmount)).thenReturn(afterPoint);
        when(pointHistoryTable.insert(anyLong(), anyLong(), any(), anyLong()))
                .thenReturn(new PointHistory(1L, userId, chargeAmount, TransactionType.CHARGE, afterPoint.updateMillis()));

        // when
        UserPoint actual = pointService.chargePoint(userId, chargeAmount);

        // then
        assertThat(actual).isEqualTo(afterPoint);
    }

    @Test
    @DisplayName("유저가 존재하지 않으면 UserNotFoundException을 던진다")
    void selectUserPoint_ShouldThrow_WhenUserNotFound() {
        // given
        long invalidUserId = 9999L;
        when(userPointTable.selectById(invalidUserId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> pointService.selectUserPoint(invalidUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("해당 사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("충전량이 0일 때 예외를 발생시킨다.")
    void chargePoint_ShouldThrow_WhenPointZero() {
        // given
        long userId = 1L;
        long chargeAmount = 0L;
        long existingPoint = 5000L;

        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, existingPoint, System.currentTimeMillis()));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargePoint(userId, chargeAmount);
        });

        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("0보다 큰 금액을 입력해야 합니다.");
    }




}
