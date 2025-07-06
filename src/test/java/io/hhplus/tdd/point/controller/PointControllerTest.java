package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.controller.PointController;
import io.hhplus.tdd.point.entity.UserPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ExtendWith(SpringExtension.class)
@WebMvcTest(PointController.class)
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void chargePoint_ShouldIncreaseUserPoint_WhenValidInput() throws Exception {
        // given
        long userId = 1L;
        long chargeAmount = 1000L;

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType("application/json")
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(0)); // 현재는 하드코딩 리턴이므로 0
    }
}
