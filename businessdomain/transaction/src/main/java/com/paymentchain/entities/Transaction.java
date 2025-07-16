package com.paymentchain.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Date;
import lombok.Data;


@Entity
@Data
public class Transaction {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;
    private String reference;
    private String accountIban;
    private Date date;
    private double amount;
    private double fee;
    private String description;
    //private Status status;
    //private Channel channel;
               
}
