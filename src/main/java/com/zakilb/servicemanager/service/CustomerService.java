package com.zakilb.servicemanager.service;

import com.zakilb.servicemanager.model.Customer;
import com.zakilb.servicemanager.repository.CustomerRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for Customer business logic
 */
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    public CustomerService() {
        this.customerRepository = new CustomerRepository();
    }
    
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    /**
     * Get all customers
     */
    public List<Customer> getAllCustomers() throws SQLException {
        return customerRepository.findAll();
    }
    
    /**
     * Get customer by ID
     */
    public Customer getCustomerById(int id) throws SQLException {
        return customerRepository.findById(id);
    }
    
    /**
     * Search customers by name (partial match)
     */
    public List<Customer> searchCustomers(String searchText) throws SQLException {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerRepository.searchByName(searchText.trim());
    }
    
    /**
     * Create or update customer
     * If customer has no ID, creates new customer
     * If customer has ID, updates existing customer
     */
    public Customer saveCustomer(Customer customer) throws SQLException {
        // Validate
        validateCustomer(customer);
        
        if (customer.getId() == null) {
            // Create new
            customerRepository.insert(customer);
        } else {
            // Update existing
            customerRepository.update(customer);
        }
        
        return customer;
    }
    
    /**
     * Delete customer
     */
    public void deleteCustomer(int id) throws SQLException {
        // Check if customer has orders
        int orderCount = customerRepository.getOrderCount(id);
        if (orderCount > 0) {
            throw new IllegalStateException(
                "Cannot delete customer with existing orders. Customer has " + orderCount + " orders."
            );
        }
        
        customerRepository.delete(id);
    }
    
    /**
     * Get number of orders for a customer (loyalty information)
     */
    public int getCustomerOrderCount(int customerId) throws SQLException {
        return customerRepository.getOrderCount(customerId);
    }
    

    /**
     * Validate customer data
     */
    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        
        // Optional: Validate email format
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (!isValidEmail(customer.getEmail())) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
    }
    
    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
