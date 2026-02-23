package com.mannaregen.repository;

import com.mannaregen.entity.GameSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSessionEntity, Long> {

    List<GameSessionEntity> findByCharacterNameOrderBySavedAtDesc(String characterName);

    List<GameSessionEntity> findAllByOrderByCharacterNameAscSavedAtDesc();
}
