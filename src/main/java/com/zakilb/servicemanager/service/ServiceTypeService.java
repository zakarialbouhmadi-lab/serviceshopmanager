package com.zakilb.servicemanager.service;

import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.repository.ServiceTypeRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for ServiceType business logic
 */
public class ServiceTypeService {
    
    private final ServiceTypeRepository serviceTypeRepository;
    
    public ServiceTypeService() {
        this.serviceTypeRepository = new ServiceTypeRepository();
    }
    
    public ServiceTypeService(ServiceTypeRepository serviceTypeRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
    }
    
    /**
     * Get all service types
     */
    public List<ServiceType> getAllServiceTypes() throws SQLException {
        return serviceTypeRepository.findAll();
    }
    
    /**
     * Search service types by code, name, or description
     */
    public List<ServiceType> searchServiceTypes(String searchText) throws SQLException {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllServiceTypes();
        }
        return serviceTypeRepository.search(searchText.trim());
    }
    

    /**
     * Create or update service type
     */
    public ServiceType saveServiceType(ServiceType serviceType) throws SQLException {
        // Validate
        validateServiceType(serviceType);
        
        // Check for duplicate code
        if (serviceType.getId() == null) {
            // Creating new - check if code exists
            if (serviceTypeRepository.codeExists(serviceType.getCode(), null)) {
                throw new IllegalArgumentException(
                    "Service type with code '" + serviceType.getCode() + "' already exists"
                );
            }
            serviceTypeRepository.insert(serviceType);
        } else {
            // Updating existing - check if code conflicts with other service types
            if (serviceTypeRepository.codeExists(serviceType.getCode(), serviceType.getId())) {
                throw new IllegalArgumentException(
                    "Service type with code '" + serviceType.getCode() + "' already exists"
                );
            }
            serviceTypeRepository.update(serviceType);
        }
        
        return serviceType;
    }
    
    /**
     * Delete service type
     * Note: Will fail if service type is referenced by work order items (foreign key constraint)
     */
    public void deleteServiceType(int id) throws SQLException {
        ServiceType serviceType = serviceTypeRepository.findById(id);
        if (serviceType == null) {
            throw new IllegalArgumentException("Service type not found with ID: " + id);
        }
        
        try {
            serviceTypeRepository.delete(id);
        } catch (SQLException e) {
            // Check if it's a foreign key constraint error
            if (e.getMessage().contains("FOREIGN KEY constraint failed")) {
                throw new IllegalStateException(
                    "Cannot delete service type '" + serviceType.getName() + 
                    "' because it is used in existing work orders"
                );
            }
            throw e;
        }
    }
    
    /**
     * Check if service type code is available
     */
    public boolean isCodeAvailable(String code, Integer excludeId) throws SQLException {
        return !serviceTypeRepository.codeExists(code, excludeId);
    }
    
    /**
     * Validate service type data
     */
    private void validateServiceType(ServiceType serviceType) {
        if (serviceType == null) {
            throw new IllegalArgumentException("Service type cannot be null");
        }
        
        if (serviceType.getCode() == null || serviceType.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Service type code is required");
        }
        
        if (serviceType.getName() == null || serviceType.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service type name is required");
        }
        
        if (serviceType.getPriceCents() < 0) {
            throw new IllegalArgumentException("Service type price cannot be negative");
        }
        
        // Validate code format (alphanumeric, no spaces)
        if (!serviceType.getCode().matches("^[A-Za-z0-9-_]+$")) {
            throw new IllegalArgumentException(
                "Service type code must contain only letters, numbers, hyphens, and underscores"
            );
        }
        
        // Code length check
        if (serviceType.getCode().length() > 20) {
            throw new IllegalArgumentException("Service type code must be 20 characters or less");
        }
    }
}
