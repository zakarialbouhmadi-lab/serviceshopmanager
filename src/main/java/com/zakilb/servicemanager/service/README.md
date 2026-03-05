# Service Layer

This directory contains all business logic service classes that orchestrate operations between repositories and the UI layer.

## ✅ Implemented Services

### CustomerService
Manages customer business logic with validation and safety checks.

**Key Features:**
- CRUD operations with validation
- Email format validation
- Search functionality
- Loyalty tracking (order count)
- Prevent deletion of customers with orders

**Usage:**
```java
CustomerService service = new CustomerService();
Customer customer = service.createCustomer(
    "Jan Kowalski",
    "+48 123 456 789",
    "jan@example.com",
    "VIP customer"
);
```

---

### ServiceTypeService
Manages service type definitions with code uniqueness validation.

**Key Features:**
- Unique code validation
- Price validation (non-negative)
- Code format validation (alphanumeric)
- Foreign key constraint handling

**Usage:**
```java
ServiceTypeService service = new ServiceTypeService();
ServiceType serviceType = service.createServiceType(
    "BBR",
    "Bike Brake Repair",
    50.00,
    "Complete brake system service"
);
```

---

### WorkOrderService
Complete order lifecycle management with automatic barcode generation.

**Key Features:**
- Multi-item order creation
- Automatic order number generation (YYYY-NNNNN)
- Automatic barcode generation per item
- Status management workflow
- Advanced filtering (by customer, status, date)
- Barcode scanning support
- Order with details (includes customer and items)

**Usage:**
```java
WorkOrderService service = new WorkOrderService();
WorkOrder order = service.createOrder(
    customer,
    Arrays.asList(serviceTypeId1, serviceTypeId2),
    Arrays.asList("Front brake", "Rear brake"),
    LocalDate.now().plusDays(7),
    "Customer prefers quick turnaround"
);
// Order number and barcodes auto-generated
```

---

### BarcodeService
Code-128 barcode generation using ZXing library.

**Key Features:**
- Code-128 format (industry standard)
- Multiple output formats (BufferedImage, PNG bytes)
- Custom dimensions support
- Preset dimensions for stickers and documents
- ASCII content validation

**Usage:**
```java
BarcodeService service = new BarcodeService();
BufferedImage barcode = service.generateBarcode("2025-00042-01");
byte[] pngData = service.generateBarcodePNG("2025-00042-01");
```

---

### PrintingService
Printing stickers and A4 documents with barcode integration.

**Key Features:**
- Sticker printing (label format)
- A4 document printing (customer receipt)
- ZPL code generation for Zebra printers
- Java Print Service integration
- Shop details from settings
- Automatic layout and formatting

**Sticker Content:**
- Order number (large)
- Item number (e.g., "Item 01 of 03")
- Customer name
- Service type
- Due date
- Code-128 barcode

**A4 Document Content:**
- Shop header
- Order and customer details
- Items table with prices
- Total calculation
- Notes section
- Signature line

**Usage:**
```java
PrintingService service = new PrintingService();

// Print stickers for all items
service.printStickers(items, order);

// Print A4 customer document
service.printA4Document(order, items, customer);

// Generate ZPL for label printer
String zpl = service.generateStickerZPL(item, order, serviceType, customer);
```

---

### BackupService
Database backup management with automatic daily backups.

**Key Features:**
- Daily automatic backups (once per day)
- Manual backup on demand
- 14-day retention policy with auto-cleanup
- Backup restore with safety backup
- Backup file management
- Storage monitoring

**Backup Location:** `data/backups/`
**Filename Format:** `service_shop_backup_YYYYMMDD_HHMMSS.db`

**Usage:**
```java
BackupService service = new BackupService();

// Check if daily backup needed
if (service.shouldRunDailyBackup()) {
    String filename = service.createDailyBackup();
}

// Manual backup
String filename = service.createManualBackup();

// List recent backups
List<BackupLog> backups = service.getRecentBackups();

// Restore from backup
service.restoreFromBackup("service_shop_backup_20251115_143022.db");
```

---

### GoogleDriveSyncService
Google Drive integration for remote backup storage.

**Key Features:**
- OAuth2 authentication
- Automatic backup folder creation
- Upload backups to Google Drive
- List remote backups
- Download backups from Drive
- Delete remote backups
- Enable/disable toggle
- Connection testing

**Setup Required:**
1. Create Google Cloud project
2. Enable Google Drive API
3. Create OAuth 2.0 credentials
4. Download credentials.json to project root
5. User authenticates on first use

**Usage:**
```java
GoogleDriveSyncService service = new GoogleDriveSyncService();

// Enable sync
service.enable();

// Initialize (OAuth2 flow)
service.initialize();

// Upload backup
String fileId = service.uploadBackup("service_shop_backup_20251115_143022.db");

// List remote backups
List<File> backups = service.listBackupsInDrive();

// Test connection
boolean connected = service.testConnection();
```

---

## 🏗️ Architecture

### Layered Design
```
UI Layer (JavaFX Controllers)
        ↓
Service Layer (Business Logic)
        ↓
Repository Layer (Data Access)
        ↓
Database (SQLite)
```

### Service Layer Responsibilities
- Business logic and validation
- Transaction coordination
- Data transformation
- Workflow orchestration
- Error handling and messaging
- Integration between repositories

### Design Principles
- Single Responsibility: Each service handles one domain
- Dependency Injection: Services can be injected for testing
- Validation: Business rules enforced at service level
- Error Handling: Meaningful exceptions with context
- Thread Safety: Synchronized where needed (OrderNumberGenerator)

---

## 🔄 Service Integration Examples

### Complete Order Workflow
```java
// 1. Customer management
CustomerService customerService = new CustomerService();
Customer customer = customerService.createCustomer(...);

// 2. Service type setup
ServiceTypeService serviceTypeService = new ServiceTypeService();
ServiceType service = serviceTypeService.createServiceType(...);

// 3. Create order
WorkOrderService workOrderService = new WorkOrderService();
WorkOrder order = workOrderService.createOrder(
    customer,
    Arrays.asList(service.getId()),
    Arrays.asList("Item notes"),
    LocalDate.now().plusDays(7),
    "Order notes"
);

// 4. Print stickers and document
PrintingService printingService = new PrintingService();
List<WorkOrderItem> items = workOrderService.getOrderItems(order.getId());
printingService.printStickers(items, order);
printingService.printA4Document(order, items, customer);

// 5. Update status as work progresses
workOrderService.updateStatus(order.getId(), WorkOrder.STATUS_IN_PROGRESS);
workOrderService.updateStatus(order.getId(), WorkOrder.STATUS_READY);
workOrderService.updateStatus(order.getId(), WorkOrder.STATUS_PICKED_UP);
```

### Backup and Sync Workflow
```java
// 1. Create backup
BackupService backupService = new BackupService();
String filename = backupService.createManualBackup();

// 2. Upload to Google Drive (if enabled)
GoogleDriveSyncService driveService = new GoogleDriveSyncService();
if (driveService.isEnabled()) {
    driveService.uploadBackup(filename);
}

// 3. Cleanup old backups
backupService.pruneOldBackups();
```

### Barcode Scanning Workflow
```java
WorkOrderService workOrderService = new WorkOrderService();

// Scan barcode
String scannedBarcode = "2025-00042-01"; // From scanner

// Find item
WorkOrderItem item = workOrderService.findItemByBarcode(scannedBarcode);

if (item != null) {
    // Get order details
    WorkOrder order = workOrderService.getOrderById(item.getWorkOrderId());
    
    // Update status or perform action
    workOrderService.updateStatus(order.getId(), WorkOrder.STATUS_READY);
}
```

---

## 🧪 Testing Considerations

Services are designed for testability:

```java
// Mock repositories for unit testing
CustomerRepository mockRepo = mock(CustomerRepository.class);
CustomerService service = new CustomerService(mockRepo);

// Test business logic
when(mockRepo.findById(1)).thenReturn(testCustomer);
Customer result = service.getCustomerById(1);
assertNotNull(result);
```

---

## 📋 Error Handling

Services throw meaningful exceptions:

- `IllegalArgumentException` - Invalid input or business rule violation
- `IllegalStateException` - Invalid state transition
- `SQLException` - Database errors (propagated from repositories)
- `IOException` - File/network errors (printing, backup, Drive)
- `WriterException` - Barcode generation errors
- `PrinterException` - Printing errors

---

## ⚙️ Configuration

Services read configuration from SettingsRepository:

- `shop_name` - Shop name for printing
- `shop_address` - Shop address
- `shop_phone` - Shop phone
- `google_drive_sync_enabled` - Drive sync toggle
- `last_backup_date` - Last backup date
- `order_counter_YYYY` - Order counter per year

---

## 🚀 Ready for UI Integration

All business logic is complete and ready for JavaFX UI implementation in Phase 5!
