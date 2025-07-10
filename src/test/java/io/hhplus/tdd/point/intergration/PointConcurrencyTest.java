package io.hhplus.tdd.point.intergration;

import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PointConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("충전 100번, 사용 100번을 동시에 실행해도 최종 포인트가 정확해야 한다. (동시성 처리 테스트)")
    void shouldBeConsistent_WhenChargeAndUseExecutedConcurrently() throws InterruptedException {
        // given
        long userId = 1L;
        long initialAmount = 100_000L;
        long eachAmount = 100L;
        int repeat = 100;

        pointService.chargePoint(userId, initialAmount);

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(repeat * 2);

        // when
        for (int i = 0; i < repeat; i++) {
            executor.execute(() -> {
                try {
                    mockMvc.perform(patch("/point/charge/{id}", userId)
                                    .contentType("application/json")
                                    .content(String.valueOf(eachAmount)))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e); // 예외 전파 (테스트 실패 유도)
                } finally {
                    latch.countDown();
                }
            });
        }

        for (int i = 0; i < repeat; i++) {
            executor.execute(() -> {
                try {
                    mockMvc.perform(patch("/point/use/{id}", userId)
                                    .contentType("application/json")
                                    .content(String.valueOf(eachAmount)))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        UserPoint result = pointService.selectUserPoint(userId);
        assertThat(result.point()).isEqualTo(initialAmount);

        // verify

        /*  mock이 아닌 실제 객체는 verify가 불가능함. 우회 방법이 있으나 History 기능이 있으니 History로 검증하기로 결정
        verify(pointHistoryTable, times(200)).insert(anyLong(), anyLong(), any(), anyLong());
        verify(userPointTable, times(200)).insertOrUpdate(eq(userId), anyLong());
        */

        List<PointHistory> histories = pointService.selectUserHistories(userId);

        assertThat(histories).hasSize(201);         // 초기 충전 +1회

        long chargeCount = histories.stream().filter(h -> h.type() == TransactionType.CHARGE).count();
        long useCount = histories.stream().filter(h -> h.type() == TransactionType.USE).count();

        assertThat(chargeCount).isEqualTo(101);     // 초기 충전 +1회
        assertThat(useCount).isEqualTo(100);
    }
}
