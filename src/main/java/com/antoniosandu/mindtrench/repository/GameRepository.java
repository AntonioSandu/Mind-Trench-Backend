package com.antoniosandu.mindtrench.repository;

import com.antoniosandu.mindtrench.entity.Game;
import com.antoniosandu.mindtrench.entity.enums.GameMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByUserId(Long userId);

    long countByUserIdAndMode(
            Long userId,
            GameMode mode);

    Optional<Game> findByIdAndUserId(
            Long gameId,
            Long userId);

    List<Game> findByUserIdOrderByCreatedAtDesc(Long userId);

}

