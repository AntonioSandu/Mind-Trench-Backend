//package com.antoniosandu.mindtrench.service;
//
//import com.antoniosandu.mindtrench.dto.request.CreateGameRequest;
//import com.antoniosandu.mindtrench.dto.response.GameResponse;
//import com.antoniosandu.mindtrench.entity.Game;
//import com.antoniosandu.mindtrench.entity.User;
//import com.antoniosandu.mindtrench.entity.enums.GameMode;
//import com.antoniosandu.mindtrench.exception.GameNotFoundException;
//import com.antoniosandu.mindtrench.exception.MaxGamesReachedException;
//import com.antoniosandu.mindtrench.exception.UserNotFoundException;
//import com.antoniosandu.mindtrench.repository.GameRepository;
//import com.antoniosandu.mindtrench.repository.UserRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class GameServiceTest {
//
//    @Mock
//    private GameRepository gameRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private GameService gameService;
//
//    @Test
//    void createGame_ShouldReturnGameResponse_WhenValidRequest() {
//
//        CreateGameRequest request = new CreateGameRequest();
//        request.setUserId(1L);
//        request.setMode(GameMode.ENDLESS);
//
//        User user = new User("antonio", "encoded");
//        user.setBestEndlessScore(0);
//
//        when(userRepository.findById(1L))
//                .thenReturn(Optional.of(user));
//
//        when(gameRepository.countByUserIdAndMode(1L, GameMode.ENDLESS))
//                .thenReturn(1L);
//
//        Game savedGame = new Game(user, GameMode.ENDLESS);
//
//        when(gameRepository.save(any(Game.class)))
//                .thenReturn(savedGame);
//
//        GameResponse response =
//                gameService.createGame(request);
//
//        assertNotNull(response);
//        assertEquals(GameMode.ENDLESS, response.getMode());
//    }
//
//    @Test
//    void createGame_ShouldThrowException_WhenUserNotFound() {
//
//        CreateGameRequest request = new CreateGameRequest();
//        request.setUserId(1L);
//        request.setMode(GameMode.ENDLESS);
//
//        when(userRepository.findById(1L))
//                .thenReturn(Optional.empty());
//
//        assertThrows(UserNotFoundException.class,
//                () -> gameService.createGame(request));
//    }
//
//    @Test
//    void createGame_ShouldThrowException_WhenMaxGamesReached() {
//
//        CreateGameRequest request = new CreateGameRequest();
//        request.setUserId(1L);
//        request.setMode(GameMode.ENDLESS);
//
//        User user = new User("antonio", "encoded");
//
//        when(userRepository.findById(1L))
//                .thenReturn(Optional.of(user));
//
//        when(gameRepository.countByUserIdAndMode(1L, GameMode.ENDLESS))
//                .thenReturn(3L);
//
//        assertThrows(MaxGamesReachedException.class,
//                () -> gameService.createGame(request));
//    }
//
//    @Test
//    void getGamesByUser_ShouldReturnList() {
//
//        User user = new User("antonio", "encoded");
//
//        when(userRepository.existsById(1L))
//                .thenReturn(true);
//
//        List<Game> games = List.of(
//                new Game(user, GameMode.ENDLESS),
//                new Game(user, GameMode.NORMAL)
//        );
//
//        when(gameRepository.findByUserIdOrderByCreatedAtDesc(1L))
//                .thenReturn(games);
//
//        List<GameResponse> result =
//                gameService.getGamesByUser(1L);
//
//        assertEquals(2, result.size());
//    }
//
//    @Test
//    void getGamesByUser_ShouldThrow_WhenUserNotFound() {
//
//        when(userRepository.existsById(1L))
//                .thenReturn(false);
//
//        assertThrows(UserNotFoundException.class,
//                () -> gameService.getGamesByUser(1L));
//    }
//
//    @Test
//    void getGameById_ShouldReturnGame() {
//
//        User user = new User("antonio", "encoded");
//
//        Game game = new Game(user, GameMode.ENDLESS);
//
//        when(gameRepository.findByIdAndUserId(1L, 10L))
//                .thenReturn(Optional.of(game));
//
//        GameResponse response =
//                gameService.getGameById(1L, 10L);
//
//        assertEquals(GameMode.ENDLESS, response.getMode());
//    }
//
//    @Test
//    void getGameById_ShouldThrow_WhenNotFound() {
//
//        when(gameRepository.findByIdAndUserId(1L, 10L))
//                .thenReturn(Optional.empty());
//
//        assertThrows(GameNotFoundException.class,
//                () -> gameService.getGameById(1L, 10L));
//    }
//
//    @Test
//    void deleteGame_ShouldDelete_WhenExists() {
//
//        User user = new User("antonio", "encoded");
//
//        Game game = new Game(user, GameMode.ENDLESS);
//
//        when(gameRepository.findByIdAndUserId(1L, 10L))
//                .thenReturn(Optional.of(game));
//
//        gameService.deleteGame(1L, 10L);
//
//        verify(gameRepository).delete(game);
//    }
//
//
//}
