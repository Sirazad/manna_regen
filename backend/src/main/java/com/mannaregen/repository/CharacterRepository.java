package com.mannaregen.repository;

import com.mannaregen.entity.CharacterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<CharacterEntity, Long> {
    List<CharacterEntity> findByDeletedFalseOrderByNameAsc();
    List<CharacterEntity> findByDeletedTrueOrderByNameAsc();
    Optional<CharacterEntity> findByNameAndDeletedFalse(String name);
}
