# Repository Layer

This directory contains all Data Access Object (DAO) classes that handle database operations.

## ✅ Implemented Repositories

### CustomerRepository
Handles all customer-related database operations including search functionality and order count tracking.

**Key Methods:**
- CRUD operations (findAll, findById, insert, update, delete)
- searchByName(String) - partial name matching
- getOrderCount(int customerId) - loyalty information

### ServiceTypeRepository
Manages service type definitions with unique code validation.

**Key Methods:**
- CRUD operations
- findByCode(String) - lookup by unique code
- codeExists(String, Integer) - validation helper

### WorkOrderRepository
Handles work order management with extensive filtering capabilities.

**Key Methods:**
- CRUD operations
- findByOrderNumber(String) - order lookup
- findByCustomerId(int) - customer history
- findByStatus(String) - status filtering
- findByDateRange(LocalDate, LocalDate) - date filtering
- updateStatus(int, String) - quick status update

### WorkOrderItemRepository
Manages individual items within work orders, including barcode lookups.

**Key Methods:**
- CRUD operations
- findByWorkOrderId(int) - get all items for an order
- findByBarcode(String) - barcode scanning support
- insertBatch(List) - efficient multi-item insertion
- deleteByWorkOrderId(int) - cascade helper
- getItemCount(int) - item counting

### SettingsRepository
Key-value configuration store with type conversion helpers.

**Key Methods:**
- get(String) / get(String, String) - retrieve settings
- set(String, String) - store settings
- getInt/getBoolean - typed getters with defaults
- setMultiple(Map) - batch updates
- getAll() - retrieve all settings

### BackupLogRepository
Tracks backup history with time-based filtering.

**Key Methods:**
- addLog(BackupLog) - log new backup
- findRecent(int days) - recent backups
- findByMethod(String) - filter by auto/manual
- deleteOlderThan(int days) - cleanup old logs
- count/countByMethod - statistics

## Design Patterns

### Repository Pattern
- Encapsulates data access logic
- Provides clean abstraction over database operations
- Returns domain model objects (not ResultSets)
- Handles SQL and JDBC details internally

### Error Handling
- SQLException propagated to service layer
- Try-with-resources for automatic cleanup
- Transaction support for batch operations with rollback

### Key Features
- PreparedStatements prevent SQL injection
- Auto-generated key retrieval after INSERT
- Consistent naming conventions
- Full null safety handling
- Performance optimized with indexes

## Usage Example

```java
// Create repository instance
CustomerRepository customerRepo = new CustomerRepository();

// Find all customers
List<Customer> customers = customerRepo.findAll();

// Search by name
List<Customer> results = customerRepo.searchByName("Smith");

// Insert new customer
Customer newCustomer = new Customer();
newCustomer.setName("John Doe");
newCustomer.setPhone("+48 123 456 789");
customerRepo.insert(newCustomer); // ID is auto-set

// Update customer
customer.setEmail("john@example.com");
customerRepo.update(customer);

// Delete customer
customerRepo.delete(customer.getId());
```

## Database Connection

All repositories use `DatabaseManager.getInstance().getConnection()` to obtain the SQLite database connection.

The DatabaseManager:
- Implements singleton pattern
- Auto-initializes schema on first run
- Enables foreign keys
- Manages connection lifecycle

## Transaction Support

Some repositories provide batch operations with transaction support:

```java
WorkOrderItemRepository itemRepo = new WorkOrderItemRepository();

List<WorkOrderItem> items = Arrays.asList(item1, item2, item3);
itemRepo.insertBatch(items); // Atomic operation with rollback on failure
```

## Thread Safety

⚠️ Current implementation uses a singleton DatabaseManager with a single connection. For multi-threaded usage, consider implementing connection pooling.

## Future Enhancements

Potential improvements for production use:
- Connection pooling for concurrent access
- Query result caching
- Soft delete support
- Audit logging (created_by, updated_by fields)
- Optimistic locking for concurrent updates
- Database migration versioning
