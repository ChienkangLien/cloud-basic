package org.tutorial.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tutorial.entities.Account;

import jakarta.transaction.Transactional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

	@Transactional
	@Modifying
	@Query("update Account a set a.used = a.used + :money, a.residue = a.residue - :money where a.userId = :userId")
	void decrease(@Param("userId") Long userId, @Param("money") Long money);
}
