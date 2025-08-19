package com.example.forum.repository;

import com.example.forum.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
}
