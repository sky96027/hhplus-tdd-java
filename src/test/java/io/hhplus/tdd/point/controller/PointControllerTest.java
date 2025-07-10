package io.hhplus.tdd.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.common.GlobalExceptionHandler;
import io.hhplus.tdd.common.exception.UserNotFoundException;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * 해당 테스트 클래스는 PointController의 HTTP 입출력을 검증한다.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(PointController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("컨트롤러 레이어 테스트 HTTP 요청에 대해 테스트")
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PointService pointService;
    @Autowired
    ObjectMapper objectMapper;


    @Test
    @DisplayName("사용자의 ID가 담긴 HTTP 요청을 받으면 사용자의 포인트를 조회")
    void selectUserPoint_ShouldUserPoint_WhenValidInput() throws Exception {
        // given
        long userId = 1L;
        UserPoint expected = new UserPoint (userId, 1000L, System.currentTimeMillis());
        when(pointService.selectUserPoint(userId)).thenReturn(expected);

        // when & then
        String expectedJson = objectMapper.writeValueAsString(expected);

        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));


    }

    @Test
    @DisplayName("사용자의 ID가 담긴 HTTP 요청을 받으면 사용자의 포인트 이력을 조회한다.")
    void selectUserHistories_ShouldUserHistories_WhenValidInput() throws Exception {
        // given
        long userId = 1L;
        List<PointHistory> expected = List.of(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 2000L, TransactionType.CHARGE, System.currentTimeMillis())
        );

        when(pointService.selectUserHistories(userId)).thenReturn(expected);

        // when & then
        String expectedJson = objectMapper.writeValueAsString(expected);

        mockMvc.perform(get("/point/histories/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    @DisplayName("사용자의 ID와 충전량이 담긴 HTTP 요청을 받으면 사용자의 포인트에서 충전량을 추가한다.")
    void chargePoint_ShouldIncreaseUserPoint_WhenValidInput() throws Exception {
        // given
        long userId = 1L;
        long chargeAmount = 500L;
        long expectedAmount = 1000L;

        UserPoint expected = new UserPoint(userId, expectedAmount, System.currentTimeMillis());
        when(pointService.chargePoint(userId, chargeAmount)).thenReturn(expected);

        // when & then
        mockMvc.perform(patch("/point/charge/{id}", userId)
                        .contentType("application/json")
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(expectedAmount));
    }

    @Test
    @DisplayName("사용자의 ID와 사용량이 담긴 HTTP 요청을 받으면 사용자의 포인트에서 사용량을 차감한다.")
    void usePoint_ShouldDecreaseUserPoint_WhenValidInput() throws Exception {
        // given
        long userId = 1L;
        long useAmount = 1500L;
        long expectedAmount = 500L;

        UserPoint expected = new UserPoint(userId, expectedAmount, System.currentTimeMillis());
        when(pointService.usePoint(userId, useAmount)).thenReturn(expected);

        // when & then
        mockMvc.perform(patch("/point/use/{id}", userId)
                        .contentType("application/json")
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(expectedAmount));

    }

    // back-end에서 소수를 막는 것보다 front-end에서 정수 입력을 강제하는 게 효율적이라 판단.
    /*@Test
    @DisplayName("충전 혹은 사용 시 외부 입력으로 소수가 들어왔을 때 예외처리한다.")
    void useChargePoint_ShouldThrowException_WhenInsufficientPoint() throws Exception {
        // given
        long userId = 1L;
        String decimalAmount = "1000.5";

        // when & then
        mockMvc.perform(patch("/point/use/{id}", userId)
                        .contentType("application/json")
                        .content(decimalAmount))
                        .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/point/charge/{id}", userId)
                        .contentType("application/json")
                        .content(decimalAmount))
                .andExpect(status().isBadRequest());

    }*/

    @Test
    @DisplayName("존재하지 않는 유저 ID로 포인트 조회 시 404 반환")
    void selectUserPoint_ShouldReturn404_WhenUserNotFound() throws Exception {
        // given
        long invalidUserId = 9999L;
        when(pointService.selectUserPoint(invalidUserId)).thenThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(get("/point/{id}", invalidUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("404"))
                .andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("음수 금액 요청 시 400 반환")
    void chargePoint_ShouldReturn400_WhenAmountIsNegative() throws Exception {
        // given
        long userId = 1L;
        long negativeAmount = -1000L;

        when(pointService.chargePoint(userId, negativeAmount))
                .thenThrow(new IllegalArgumentException("0보다 큰 금액을 입력해야 합니다."));

        // when & then
        mockMvc.perform(patch("/point/charge/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(negativeAmount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("400"))
                .andExpect(jsonPath("$.message").value("0보다 큰 금액을 입력해야 합니다."));
    }

    @Test
    @DisplayName("포인트 충전 시 0 이하 금액이면 400 반환")
    void chargePoint_ShouldReturn400_WhenAmountIsZero() throws Exception {
        // given
        long userId = 1L;
        long[] invalidAmounts = {0L, -500L};

        // when & then
        for (long amount : invalidAmounts) {
            when(pointService.chargePoint(userId, amount))
                    .thenThrow(new IllegalArgumentException("0보다 큰 금액을 입력해야 합니다."));

            mockMvc.perform(patch("/point/charge/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.valueOf(amount)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("400"))
                    .andExpect(jsonPath("$.message").value("0보다 큰 금액을 입력해야 합니다."));
        }
    }

    @Test
    @DisplayName("포인트 사용 시 0 이하 금액이면 400 반환")
    void usePoint_ShouldReturn400_WhenAmountIsZeroOrNegative() throws Exception {
        //given
        long userId = 1L;
        long[] invalidAmounts = {0L, -500L};

        for (long amount : invalidAmounts) {
            when(pointService.usePoint(userId, amount))
                    .thenThrow(new IllegalArgumentException("0보다 큰 금액을 입력해야 합니다."));

            mockMvc.perform(patch("/point/use/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.valueOf(amount)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("400"))
                    .andExpect(jsonPath("$.message").value("0보다 큰 금액을 입력해야 합니다."));
        }
    }

    @Test
    @DisplayName("포인트 사용 시 잔액 부족하면 400 반환")
    void usePoint_ShouldReturn400_WhenInsufficientPoint() throws Exception {
        // given
        long userId = 1L;
        long useAmount = 10_000L;

        when(pointService.usePoint(userId, useAmount))
                .thenThrow(new IllegalArgumentException("보유 포인트(0포인트)보다 많은 금액을 사용할 수 없습니다."));

        // when & then
        mockMvc.perform(patch("/point/use/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("400"))
                .andExpect(jsonPath("$.message").value("보유 포인트(0포인트)보다 많은 금액을 사용할 수 없습니다."));
    }

}
