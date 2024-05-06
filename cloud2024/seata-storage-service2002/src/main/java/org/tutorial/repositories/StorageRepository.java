package org.tutorial.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tutorial.entities.Storage;

import jakarta.transaction.Transactional;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Integer> {
	
	@Transactional
	@Modifying
	@Query("update Storage s set s.used = s.used + :count, s.residue = s.residue - :count where s.productId = :productId")
	void decrease(@Param("productId") Long productId, @Param("count") Integer count);
}
