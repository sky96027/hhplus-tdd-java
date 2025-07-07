package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.UserPoint;

public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    public UserPoint chargePoint(long userId, long amount) {
        // 충전 전 포인트
        UserPoint beforePoint = userPointTable.selectById(userId);

        // Logic
        long newAmount = beforePoint.point() + amount;
        UserPoint afterPoint = userPointTable.insertOrUpdate(userId, newAmount);

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

        return afterPoint;
    }
}
