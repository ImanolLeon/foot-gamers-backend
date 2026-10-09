package org.example.footgamers.repository;

import org.example.footgamers.entities.Bando;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BandoRepository extends JpaRepository<Bando, Long> {
}