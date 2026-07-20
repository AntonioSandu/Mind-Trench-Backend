//package com.antoniosandu.mindtrench.controller;
//
//import com.antoniosandu.mindtrench.dto.request.CreateGameRequest;
//import com.antoniosandu.mindtrench.dto.response.GameResponse;
//import com.antoniosandu.mindtrench.entity.enums.GameMode;
//import com.antoniosandu.mindtrench.service.GameService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(GameController.class)
//class GameControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private GameService gameService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void createGame_ShouldReturn201() throws Exception {
//
//        CreateGameRequest request = new CreateGameRequest();
//        request.setUserId(1L);
//        request.setMode(GameMode.ENDLESS);
//
//        GameResponse response = new GameResponse();
//        response.setId(10L);
//        response.setMode(GameMode.ENDLESS);
//        response.setTurnNumber(0);
//
//        when(gameService.createGame(any()))
//                .thenReturn(response);
//
//        mockMvc.perform(post("/api/game")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(10L))
//                .andExpect(jsonPath("$.mode").value("ENDLESS"));
//    }
//
//    @Test
//    void getGamesByUser_ShouldReturn200() throws Exception {
//
//        GameResponse g1 = new GameResponse();
//        g1.setId(1L);
//
//        GameResponse g2 = new GameResponse();
//        g2.setId(2L);
//
//        when(gameService.getGamesByUser(1L))
//                .thenReturn(List.of(g1, g2));
//
//        mockMvc.perform(get("/api/game/user/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2));
//    }
//
//    @Test
//    void getGameById_ShouldReturn200() throws Exception {
//
//        GameResponse response = new GameResponse();
//        response.setId(10L);
//
//        when(gameService.getGameById(1L, 10L))
//                .thenReturn(response);
//
//        mockMvc.perform(get("/api/game/10")
//                        .param("userId", "1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(10L));
//    }
//
//    @Test
//    void deleteGame_ShouldReturn200() throws Exception {
//
//        mockMvc.perform(delete("/api/game/10")
//                        .param("userId", "1"))
//                .andExpect(status().isOk());
//
//        verify(gameService).deleteGame(1L, 10L);
//    }
//
//}
