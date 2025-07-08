package io.hhplus.tdd.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * 해당 테스트 클래스는 PointController의 HTTP 입출력을 검증한다.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(PointController.class)
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PointService pointService;
    @Autowired
    ObjectMapper objectMapper;


    @Test
    @DisplayName("사용자의 ID가 담긴 HTTP 요청을 받으면 사용자의 포인트를 조회")
    void selectUserPoint_ShouldUserPoint_whenValidInput() throws Exception {
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
    void selectUserHistories_ShouldUserHistories_whenValidInput() throws Exception {
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
    void chargePoint_ShouldIncreaseUserPoint_whenValidInput() throws Exception {
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
    void usePoint_ShouldDecreaseUserPoint_whenValidInput() throws Exception {
        //given
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

}
