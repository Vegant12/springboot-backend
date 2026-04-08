package net.javaguides.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.javaguides.springboot.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
