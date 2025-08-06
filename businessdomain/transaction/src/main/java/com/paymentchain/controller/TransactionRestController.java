package com.paymentchain.controller;

import com.paymentchain.entities.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import com.paymentchain.repository.TransactionRepository;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionRestController {

    @Autowired
    private TransactionRepository transactionRepository;

    // Create - POST
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        Transaction savedTransaction = transactionRepository.save(transaction);
        return ResponseEntity.ok(savedTransaction);
    }

    // Read All - GET
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    // Read One - GET
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable long id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        return transaction.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update - PUT
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable long id, @RequestBody Transaction transactionDetails) {
        return transactionRepository.findById(id)
                .map(existingTransaction -> {
                    existingTransaction.setReference(transactionDetails.getReference());
                    existingTransaction.setIbanAccount(transactionDetails.getIbanAccount());
                    existingTransaction.setDate(transactionDetails.getDate());
                    existingTransaction.setAmount(transactionDetails.getAmount());
                    existingTransaction.setFee(transactionDetails.getFee());
                    existingTransaction.setDescription(transactionDetails.getDescription());
                    //existingTransaction.setStatus(transactionDetails.getStatus());
                    //existingTransaction.setChannel(transactionDetails.getChannel());
                    Transaction updatedTransaction = transactionRepository.save(existingTransaction);
                    return ResponseEntity.ok(updatedTransaction);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete - DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable long id) {
        return transactionRepository.findById(id)
                .map(transaction -> {
                    transactionRepository.delete(transaction);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    
    // Endpoint adicional para buscar transacciones por IBAN
//    @GetMapping("/by-account/{iban}")
//    public ResponseEntity<List<Transaction>> getTransactionsByAccountIban(@PathVariable String iban) {
//        List<Transaction> transactions = transactionRepository.findByAccountIban(iban);
//        return ResponseEntity.ok(transactions);
//    }
//
//    // Endpoint adicional para buscar transacciones por estado
//    @GetMapping("/by-status/{status}")
//    public ResponseEntity<List<Transaction>> getTransactionsByStatus(@PathVariable Status status) {
//        List<Transaction> transactions = transactionRepository.findByStatus(status);
//        return ResponseEntity.ok(transactions);
//    }
}
