package com.antoniosandu.mindtrench.controller;

import com.antoniosandu.mindtrench.dto.request.CreateGameRequest;
import com.antoniosandu.mindtrench.dto.request.TurnRequest;
import com.antoniosandu.mindtrench.dto.request.UseItemRequest;
import com.antoniosandu.mindtrench.dto.response.GameResponse;
import com.antoniosandu.mindtrench.dto.response.GameStateResponse;
import com.antoniosandu.mindtrench.dto.response.MessageResponse;
import com.antoniosandu.mindtrench.service.GameEngineService;
import com.antoniosandu.mindtrench.service.GameService;
import com.antoniosandu.mindtrench.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final ItemService itemService;
    private final GameEngineService gameEngineService;

    public GameController(GameService gameService, ItemService itemService, GameEngineService gameEngineService) {
        this.gameService = gameService;
        this.itemService = itemService;
        this.gameEngineService = gameEngineService;
    }

    @PostMapping
    public ResponseEntity<GameResponse> createGame(
            @Valid @RequestBody CreateGameRequest request) {

        GameResponse response =
                gameService.createGame(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GameResponse>> getGamesByUser(
            @PathVariable Long userId) {

        List<GameResponse> games =
                gameService.getGamesByUser(userId);

        return ResponseEntity.ok(games);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGameById(
            @PathVariable Long gameId,
            @RequestParam Long userId) {

        GameStateResponse response =
                gameService.getGameById(userId, gameId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<MessageResponse> deleteGame(
            @PathVariable Long gameId,
            @RequestParam Long userId) {

        gameService.deleteGame(userId, gameId);

        return ResponseEntity.ok(
                new MessageResponse(
                        "Game eliminated successfully"));
    }

    @PatchMapping("/{gameId}/item")
    public ResponseEntity<GameStateResponse> useItem(
            @PathVariable Long gameId,
            @RequestParam Long userId,
            @Valid @RequestBody UseItemRequest request
    ) {

        GameStateResponse response = itemService.useItem(
                gameId,
                userId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{gameId}/forget")
    public ResponseEntity<GameStateResponse> forgetItem(
            @PathVariable Long gameId,
            @RequestParam Long userId,
            @Valid @RequestBody UseItemRequest request
    ){
        GameStateResponse response = itemService.forgetItem(
                gameId,
                userId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{gameId}/turn")
    public ResponseEntity<GameStateResponse> executeTurn(
            @PathVariable Long gameId,
            @RequestParam Long userId,
            @Valid @RequestBody TurnRequest request
    ){
        GameStateResponse response =
                gameEngineService.executeTurn(
                        gameId,
                        userId,
                        request
                );

        return ResponseEntity.ok(response);
    }

}
