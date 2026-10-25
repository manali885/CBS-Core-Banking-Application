package com.cbs.customer_service.service;

import com.cbs.customer_service.exception.CustomerAlreadyExistsException;
import com.cbs.customer_service.exception.CustomerNotFoundException;
import com.cbs.customer_service.model.Customer;
import com.cbs.customer_service.model.KycStatus;
import com.cbs.customer_service.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository){
        this.customerRepository = customerRepository;
    }
    public Customer createCustomer(Customer customer){
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()){
            throw new CustomerAlreadyExistsException("Email already registered: " + customer.getEmail());
        }
        return customerRepository.save(customer);
    }

    public Customer getCustomerById(Long id){
        return customerRepository.findById(id).orElseThrow(() ->
                new CustomerNotFoundException("Customer not found: " + id));
    }


    public List<Customer> getAllCustomers(){
        return customerRepository.findAll();
    }

    public Customer updateKycStatus(Long id, KycStatus status){
        Customer c = getCustomerById(id);
        c.setKycStatus(status);
        return customerRepository.save(c);

    }
}
