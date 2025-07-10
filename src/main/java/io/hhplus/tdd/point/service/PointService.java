package io.hhplus.tdd.point.service;

import io.hhplus.tdd.common.exception.UserNotFoundException;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.validation.PointValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 해당 클래스는 비즈니스 로직을 처리한다.
 */
@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    // 동시성 처리를 위한 락 생성
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    // 생성자에 포함시키면 테스트할 수 있다는 장점이 있으나 단순 유틸이기에 직접 생성
    private final PointValidator pointValidator = new PointValidator();

    /**
     * 포인트 조회
     * @param userId 사용자 ID
     * @return 유저의 포인트 잔량
     */
    public UserPoint selectUserPoint(long userId) {
        UserPoint userPoint = userPointTable.selectById(userId);
        if(userPoint == null) {
            throw new UserNotFoundException();
        }

        return userPoint;
    }

    /**
     * 포인트 히스토리 조회
     * @param userId 사용자 ID
     * @return 유저의 포인트 히스토리 리스트
     */
    public List<PointHistory> selectUserHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 포인트 충전
     * @param userId 사용자 ID
     * @param amount 사용할 포인트
     * @return 충전 후 포인트 잔량
     */
    public UserPoint chargePoint(long userId, long amount) {
        ReentrantLock lock = lockMap.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();

        try {
            // 충전 전 포인트
            UserPoint beforePoint = userPointTable.selectById(userId);

            // Logic
            long newAmount = beforePoint.point() + amount;

            // 예외 처리
            pointValidator.validateChargeAmount(amount, newAmount);

            UserPoint afterPoint = userPointTable.insertOrUpdate(userId, newAmount);
            pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, afterPoint.updateMillis());

            return afterPoint;
        } finally {
            lock.unlock();
        }
    }


    /**
     * 포인트 사용
     * @param userId 사용자 ID
     * @param amount 사용할 포인트
     * @return 사용 후 포인트 잔량
     */
    public UserPoint usePoint(long userId, long amount) {
        ReentrantLock lock = lockMap.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();

        try {
            // 사용 전 포인트
            UserPoint beforePoint = userPointTable.selectById(userId);

            // Logic
            long newAmount = beforePoint.point() - amount;

            // 예외 처리
            pointValidator.validateUseAmount(amount, newAmount);

            UserPoint afterPoint = userPointTable.insertOrUpdate(userId, newAmount);
            pointHistoryTable.insert(userId, amount, TransactionType.USE, afterPoint.updateMillis());

            return afterPoint;
        } finally {
            lock.unlock();
        }
    }
}
