package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 해당 클래스는 비즈니스 로직을 처리한다.
 */
@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * 포인트 조회
     * @param userId 사용자 ID
     * @return 유저의 포인트 잔량
     */
    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    /**
     * 포인트 히스토리 조회
     * @param userId 사용자 ID
     * @return 유저의 포인트 히스토리 리스트
     */
    public List<PointHistory> getUserHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 포인트 충전
     * @param userId 사용자 ID
     * @param amount 사용할 포인트
     * @return 충전 후 포인트 잔량
     */
    public UserPoint chargePoint(long userId, long amount) {
        // 충전 전 포인트
        UserPoint beforePoint = userPointTable.selectById(userId);

        // Logic
        long newAmount = beforePoint.point() + amount;
        UserPoint afterPoint = userPointTable.insertOrUpdate(userId, newAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, afterPoint.updateMillis());

        return afterPoint;
    }

    /**
     * 포인트 사용
     * @param userId 사용자 ID
     * @param amount 사용할 포인트
     * @return 사용 후 포인트 잔량
     */
    public UserPoint usePoint(long userId, long amount) {
        // 사용 전 포인트
        UserPoint beforePoint = userPointTable.selectById(userId);

        // Logic
        long newAmount = beforePoint.point() - amount;
        UserPoint afterPoint = userPointTable.insertOrUpdate(userId, newAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.USE, afterPoint.updateMillis());

        return afterPoint;
    }
}
