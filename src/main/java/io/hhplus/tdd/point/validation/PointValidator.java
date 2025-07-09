package io.hhplus.tdd.point.validation;

import org.springframework.stereotype.Component;

import static io.hhplus.tdd.common.PointConstraints.*;

@Component
public class PointValidator {

    // 충전 검증
    public void validateChargeAmount(long amount, long newAmount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("0보다 큰 금액을 입력해야 합니다.");
        }
        if (amount > MAX_POINT_PER_CHARGE) {
            throw new IllegalArgumentException("1회 충전 한도(" + MAX_POINT_PER_CHARGE + "포인트)를 초과할 수 없습니다.");
        }
        if (newAmount > MAX_POINT) {
            throw new IllegalArgumentException("최대 보유 가능 포인트(" + MAX_POINT + "포인트)를 초과할 수 없습니다.");
        }
    }

    // 사용 검증 (1회 사용 한도 무제한, 단 음수만 방지)
    public void validateUseAmount(long amount, long newAmount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("0보다 큰 금액을 입력해야 합니다.");
        }
        if (newAmount < 0) {
            throw new IllegalArgumentException("보유 포인트(" + (amount + newAmount) + "포인트)보다 많은 금액을 사용할 수 없습니다.");
        }
    }
}
