package com.paymentchain.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.entities.Customer;
import com.paymentchain.entities.CustomerProduct;
import com.paymentchain.repository.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerRestController {

    @Autowired
    private CustomerRepository customerRepository;
    
    private final WebClient.Builder webClientBuilder;
    
    public CustomerRestController (WebClient.Builder webClientBuilder){
        this.webClientBuilder = webClientBuilder;
    }
    
    HttpClient client = HttpClient.create()
            // Connection Timeout: is a period within which a connection between a client and a server must be stablished
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            // Response Timeout: The maximum time we wait to receive a response after sending a request
            .responseTimeout(Duration.ofSeconds(1))
            // Read and Write Timeout: A read timeout occurs when no data was read within a certain
            //period of time, while the write timeout when a write operation cannot finish at an specific time
            .doOnConnected(connection -> {
              connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
              connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

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
                    existingCustomer.setTransactions(customerDetails.getTransactions());
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
    
    @GetMapping("/full")
    public Customer getByCode(@RequestParam("code") String code) {
        Customer customer = customerRepository.findByCode(code);
        List<CustomerProduct> products = customer.getProducts();
        products.forEach(x -> {
            String productName = getProductName(x.getId());
            x.setProductName(productName);
        });
        return customer;
                
    }
    
    private String getProductName (long id){
        WebClient build =webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8082/api/v1/products")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8082/api/v1/products"))
                .build();
        
        JsonNode block = build.method(HttpMethod.GET).uri("/" + id)
                .retrieve().bodyToMono(JsonNode.class).block();
        
        String name = block.get("name").asText();
        return name;
    }
}
