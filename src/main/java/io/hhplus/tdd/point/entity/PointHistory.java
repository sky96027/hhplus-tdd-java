package io.hhplus.tdd.point.entity;

import io.hhplus.tdd.point.domain.TransactionType;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
}


