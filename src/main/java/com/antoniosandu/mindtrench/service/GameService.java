package com.antoniosandu.mindtrench.service;

import com.antoniosandu.mindtrench.dto.request.CreateGameRequest;
import com.antoniosandu.mindtrench.dto.response.GameResponse;
import com.antoniosandu.mindtrench.dto.response.GameStateResponse;
import com.antoniosandu.mindtrench.entity.CharacterState;
import com.antoniosandu.mindtrench.entity.Game;
import com.antoniosandu.mindtrench.entity.User;
import com.antoniosandu.mindtrench.exception.GameNotFoundException;
import com.antoniosandu.mindtrench.exception.MaxGamesReachedException;
import com.antoniosandu.mindtrench.exception.UserNotFoundException;
import com.antoniosandu.mindtrench.game.map.MapDefinition;
import com.antoniosandu.mindtrench.mapper.GameMapper;
import com.antoniosandu.mindtrench.repository.GameRepository;
import com.antoniosandu.mindtrench.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public GameService(GameRepository gameRepository,
                       UserRepository userRepository) {

        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    private static final int MAX_GAMES_PER_MODE = 3;

    private CharacterState createCharacter() {
        return new CharacterState(
                3,
                MapDefinition.getRandomNode()
        );
    }

    public GameResponse createGame(CreateGameRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        if (gameRepository.countByUserIdAndMode(
                user.getId(),
                request.getMode()) >= MAX_GAMES_PER_MODE) {

            throw new MaxGamesReachedException(
                    "Reached maximum number of games you can create for this game mode!");
        }

        Game game = new Game(
                user,
                request.getMode());

        game.setPlayer(
                createCharacter()
        );

        game.setBoss(
                createCharacter()
        );

        Game saved = gameRepository.save(game);

        return GameMapper.toResponse(saved);
    }

    public GameStateResponse getGameById(
            Long userId,
            Long gameId) {

        Game game = gameRepository
                .findByIdAndUserId(gameId, userId)
                .orElseThrow(() ->
                        new GameNotFoundException(
                                "Game not found"));

        GameStateResponse response = GameMapper.toStateResponse(game);
        response.setGameOver(false);
        response.setResult(null);
        return response;
    }

    public List<GameResponse> getGamesByUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    "User not found");
        }

        List<Game> games =
                gameRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return games.stream()
                .map(GameMapper::toResponse)
                .toList();
    }

    public void deleteGame(
            Long userId,
            Long gameId) {

        Game game = gameRepository
                .findByIdAndUserId(gameId, userId)
                .orElseThrow(() ->
                        new GameNotFoundException(
                                "Game not found"));

        gameRepository.delete(game);
    }

}
