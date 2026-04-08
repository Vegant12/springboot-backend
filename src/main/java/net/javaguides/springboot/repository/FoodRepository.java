package net.javaguides.springboot.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.javaguides.springboot.model.Food;

@Repository
public interface  FoodRepository extends JpaRepository<Food, Long>{
    
}
