# Model Layer (Domain Entities)

This directory contains all domain model classes representing the core business entities of the Service Manager application.

## ✅ Implemented Models

### Customer.java
Represents a customer in the system.

**Fields:**
- `Integer id` - Primary key (auto-generated)
- `String name` - Customer name (required)
- `String phone` - Contact phone number
- `String email` - Email address
- `String notes` - Additional notes about customer
- `LocalDateTime createdAt` - Registration timestamp

**Features:**
- Full getters/setters
- toString() for debugging
- Default constructor sets createdAt to now

**Business Rules:**
- Name is required
- Can track loyalty via order count (via repository)

---

### ServiceType.java
Represents a type of service offered (e.g., "Bike brake repair", "Ski waxing").

**Fields:**
- `Integer id` - Primary key (auto-generated)
- `String code` - Unique short code (e.g., "BBR")
- `String name` - Service name (required)
- `int priceCents` - Price stored as integer cents/grosze
- `String description` - Optional service description
- `LocalDateTime createdAt` - Creation timestamp

**Helper Methods:**
- `getPriceInPLN()` - Returns price as double in złoty
- `setPriceFromPLN(double)` - Sets price from złoty value

**Business Rules:**
- Code must be unique
- Price stored as integer to avoid floating-point issues
- 1 PLN = 100 grosze

---

### WorkOrder.java
Represents a customer's service order containing one or more items.

**Fields:**
- `Integer id` - Primary key (auto-generated)
- `String orderNumber` - Unique order number (format: YYYY-NNNNN)
- `Integer customerId` - Foreign key to Customer
- `LocalDate dueDate` - Expected completion date
- `String status` - Current order status
- `String notes` - Order notes
- `LocalDateTime createdAt` - Order creation timestamp

**Status Constants:**
- `STATUS_RECEIVED = "received"` - Initial state
- `STATUS_IN_PROGRESS = "in_progress"` - Work started
- `STATUS_READY = "ready"` - Ready for pickup
- `STATUS_PICKED_UP = "picked_up"` - Completed

**Business Rules:**
- Order number generated automatically (see OrderNumberGenerator)
- Default status is RECEIVED
- Each order must have at least 1 item

---

### WorkOrderItem.java
Represents an individual item within a work order (e.g., one bike, one snowboard).

**Fields:**
- `Integer id` - Primary key (auto-generated)
- `Integer workOrderId` - Foreign key to WorkOrder
- `Integer serviceTypeId` - Foreign key to ServiceType
- `int itemNumber` - Item sequence (1, 2, 3...)
- `String barcode` - Unique barcode (format: orderNumber-itemNumber)
- `String notes` - Item-specific notes

**Business Rules:**
- Barcode format: `{orderNumber}-{itemNumber:02d}`
- Example: `2025-00042-01`, `2025-00042-02`, `2025-00042-03`
- Barcode must be unique across all items
- Used for printing labels and scanning

---

### Settings.java
Simple key-value pair for application configuration.

**Fields:**
- `String key` - Setting key (primary key)
- `String value` - Setting value

**Common Settings:**
- `order_counter_YYYY` - Order counter for current year
- `google_drive_sync_enabled` - Google Drive sync toggle
- `last_backup_date` - Last automatic backup date
- `shop_name` - Shop name for printing
- `shop_address` - Shop address for documents
- `shop_phone` - Shop phone number

**Usage:**
Accessed via SettingsRepository with type conversion helpers (getInt, getBoolean).

---

### BackupLog.java
Tracks backup history for audit and cleanup purposes.

**Fields:**
- `Integer id` - Primary key (auto-generated)
- `String filename` - Backup file name
- `LocalDateTime createdAt` - Backup timestamp
- `String method` - Backup method (auto/manual)

**Method Constants:**
- `METHOD_AUTO = "auto"` - Daily automatic backup
- `METHOD_MANUAL = "manual"` - User-initiated backup

**Business Rules:**
- Used to track backup history
- Automatic cleanup of logs older than retention period
- Helps identify last successful backup

---

## Design Principles

### POJO Pattern
All models are Plain Old Java Objects with:
- Private fields
- Public getters/setters
- No business logic (handled in services)
- No database code (handled in repositories)

### Immutability
- IDs are set by repository after INSERT
- Timestamps set on object creation
- Status/method constants prevent typos

### Type Safety
- LocalDateTime for timestamps (ISO 8601 compatible)
- LocalDate for due dates (no time component)
- Integer for nullable IDs (null = not yet persisted)
- int for prices (avoid floating-point errors)

### Database Mapping
Models map directly to database tables:
- Field names match column names (snake_case → camelCase)
- Null handling for optional fields
- Auto-generated IDs after INSERT

---

## Usage Example

```java
// Create new customer
Customer customer = new Customer();
customer.setName("Jan Kowalski");
customer.setPhone("+48 123 456 789");
customer.setEmail("jan@example.com");
// createdAt is auto-set in constructor

// Create service type
ServiceType service = new ServiceType();
service.setCode("BBR");
service.setName("Bike Brake Repair");
service.setPriceFromPLN(50.00); // Stores as 5000 cents
// Gets: service.getPriceInPLN() → 50.0

// Create work order
WorkOrder order = new WorkOrder();
order.setOrderNumber("2025-00042");
order.setCustomerId(customer.getId());
order.setDueDate(LocalDate.now().plusDays(7));
order.setStatus(WorkOrder.STATUS_RECEIVED);
// status defaults to RECEIVED if not set

// Create order item
WorkOrderItem item = new WorkOrderItem();
item.setWorkOrderId(order.getId());
item.setServiceTypeId(service.getId());
item.setItemNumber(1);
item.setBarcode("2025-00042-01");
item.setNotes("Front brakes need replacement");
```

---

## Relationship Diagram

```
Customer (1) ───< has many >───> (N) WorkOrder
                                      │
                                      │ contains
                                      ↓
                                (1-N) WorkOrderItem
                                      │
                                      │ uses
                                      ↓
                                  (1) ServiceType

Settings - standalone key-value store
BackupLog - standalone audit log
```

---

## Data Validation

⚠️ **Important:** Models perform minimal validation. Business rule validation should be handled in the Service layer.

Models only ensure:
- Type safety (via Java types)
- Default values (createdAt, status)
- Basic constraints (required fields via @NonNull annotations could be added)

The Service layer should validate:
- Business rules (e.g., unique codes, price ranges)
- Foreign key validity
- Status transitions
- Complex validations

---

## Future Enhancements

Potential improvements:
- Bean Validation annotations (@NotNull, @Size, @Email)
- Builder pattern for complex object creation
- Equals/hashCode implementations
- Copy constructors for defensive copying
- Immutable variants for thread safety
- Audit fields (createdBy, updatedBy, updatedAt)
