package com.cbs.customer_service.controller;

import com.cbs.customer_service.model.Customer;
import com.cbs.customer_service.model.KycStatus;
import com.cbs.customer_service.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;
    public CustomerController(CustomerService customerService){
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> fetchCustomers() {
        return customerService.getAllCustomers();
    }

    @PostMapping
    public Customer createCustomer(@Valid @RequestBody Customer customer) {
        return customerService.createCustomer(customer);
    }

    @GetMapping("/{id}")
    public Customer getById(@PathVariable Long id){
        return customerService.getCustomerById(id);
    }

    @PatchMapping("/{id}/kyc")
    public Customer updateKYC(@PathVariable Long id, @RequestParam KycStatus status){
        return customerService.updateKycStatus(id,status);
    }
}
