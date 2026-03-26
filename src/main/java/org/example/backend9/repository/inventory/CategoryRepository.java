package org.example.backend9.repository.inventory;

import org.example.backend9.entity.inventory.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    @Query(value = """
        SELECT c.name, COUNT(p.id) 
        FROM categories c
        LEFT JOIN products p ON p.category_id = c.id
        GROUP BY c.id, c.name
    """, nativeQuery = true)
    List<Object[]> getCategoryRatioNative();
}