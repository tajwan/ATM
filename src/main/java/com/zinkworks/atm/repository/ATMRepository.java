package com.zinkworks.atm.repository;

import com.zinkworks.atm.entity.ATM;
import org.springframework.data.repository.CrudRepository;

public interface ATMRepository extends CrudRepository<ATM, Long> {
}
