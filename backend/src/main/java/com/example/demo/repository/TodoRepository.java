package com.example.demo.repository;

import com.example.demo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findAllByOrderByCreatedAtDesc();

    @Query("SELECT t FROM Todo t WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY t.createdAt DESC")
    Page<Todo> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByCompletedFalse();

    long countByCompletedTrue();

    long countByPriorityAndCompletedFalse(int priority);
}
