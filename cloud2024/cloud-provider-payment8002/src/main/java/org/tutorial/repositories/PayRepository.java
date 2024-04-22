package org.tutorial.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tutorial.entities.Pay;

@Repository
public interface PayRepository extends JpaRepository<Pay, Integer>{

}
