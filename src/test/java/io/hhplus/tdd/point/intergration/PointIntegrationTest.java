package io.hhplus.tdd.point.intergration;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("사용자의 ID와 충전량을 입력 받으면 포인트 잔량이 증가, 이후 DB에서 조회")
    void chargePoint_ShouldIncreaseUserPoint_WhenValidInput() throws Exception {
        // given
        long userId = 1L;
        long initialAmount = 1500L;
        long chargeAmount = 500L;
        long expectedAmount = 2000L;

        pointService.chargePoint(userId, initialAmount);

        // when & then
        mockMvc.perform(patch("/point/charge/{id}", userId)
                        .contentType("application/json")
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(expectedAmount));

        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(expectedAmount));
    }

    @Test
    @DisplayName("사용자의 ID와 사용량을 입력 받으면 포인트 잔량이 감소, 이후 DB에서 조회")
    void usePoint_ShouldDecreaseUserPoint_WhenValidInput() throws Exception {
        // given
        long userId = 1L;
        long initialAmount = 1500L;
        long useAmount = 1000L;
        long expectedAmount = 500L;

        pointService.chargePoint(userId, initialAmount);

        // when & then
        mockMvc.perform(patch("/point/use/{id}", userId)
                        .contentType("application/json")
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(expectedAmount));

        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(expectedAmount));
    }


}