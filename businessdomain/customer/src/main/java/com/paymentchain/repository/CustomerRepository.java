package com.paymentchain.repository;

import com.paymentchain.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE c.code = ?1")
    public Customer findByCode (String code);
    
    @Query("SELECT c FROM Customer c WHERE c.iban = ?1")
    public Customer findByAccount (String iban);
}
