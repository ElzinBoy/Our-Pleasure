package me.sha425.ourpleasure.db.repository;

import me.sha425.ourpleasure.dto.HentaiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HentaiRepository extends JpaRepository<HentaiEntity, Integer> {}
