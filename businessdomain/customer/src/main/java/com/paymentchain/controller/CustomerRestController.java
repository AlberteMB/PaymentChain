package com.paymentchain.controller;

import com.paymentchain.entities.Customer;
import com.paymentchain.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerRestController {

    @Autowired
    private CustomerRepository customerRepository;

    // Create - POST
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        // Validación para evitar NullPointerException
        if (customer.getProducts() != null) {
            // Asegura que cada producto tenga este Customer como padre
            customer.getProducts().forEach(x -> {
                if (x.getCustomer() != null && !x.getCustomer().equals(customer)) {
                    throw new IllegalStateException("El producto ya pertenece a otro cliente");
                }
                x.setCustomer(customer);
            });
        }

        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(savedCustomer);
    }          

    // Read All - GET
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return ResponseEntity.ok(customers);
    }

    // Read One - GET
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update - PUT
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable long id, @RequestBody Customer customerDetails) {
        return customerRepository.findById(id)
                .map(existingCustomer -> {
                    existingCustomer.setCode(customerDetails.getCode());
                    existingCustomer.setIban(customerDetails.getIban());
                    existingCustomer.setName(customerDetails.getName());
                    existingCustomer.setSurname(customerDetails.getSurname());
                    existingCustomer.setPhone(customerDetails.getPhone());
                    existingCustomer.setAddress(customerDetails.getAddress());
                    existingCustomer.setProducts(customerDetails.getProducts());
                    existingCustomer.setTransations(customerDetails.getTransations());
                    Customer updatedCustomer = customerRepository.save(existingCustomer);
                    return ResponseEntity.ok(updatedCustomer);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete - DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable long id) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customerRepository.delete(customer);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
