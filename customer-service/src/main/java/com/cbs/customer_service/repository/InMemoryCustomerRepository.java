package com.cbs.customer_service.repository;

import com.cbs.customer_service.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<Long,Customer> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator  = new AtomicLong(0);

    @Override
    public Customer save(Customer customer) {
        if (customer.getId() == null){
            customer.setId(idGenerator.incrementAndGet());
        }
        store.put(customer.getId(),customer);
        return customer;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        for (Customer c : store.values()) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(store.values());
    }
}
