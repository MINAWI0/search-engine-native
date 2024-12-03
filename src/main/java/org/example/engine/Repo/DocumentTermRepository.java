package org.example.engine.Repo;

import org.example.engine.models.DocumentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTermRepository extends JpaRepository<DocumentTerm, Long> {

}