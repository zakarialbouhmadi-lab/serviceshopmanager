# Service Manager

A desktop application for managing bike, ski, and snowboard service orders.

## 🎯 Overview

This application helps service shop cashiers manage:
- Customer information
- Service types and pricing
- Work orders with multiple items
- **Per-item discounts (0-100%)**
- Barcode generation and printing
- A4 document generation
- Manual backups with optional Google Drive sync

## 🛠 Technology Stack

- **Language**: Java 17+
- **UI Framework**: JavaFX 21
- **Database**: SQLite
- **Barcode Generation**: ZXing (Code-128)
- **PDF Generation**: OpenPDF
- **Cloud Sync**: Google Drive API
- **Build Tool**: Maven
- **Logging**: SLF4J

## 📋 Features

### Customer Management
- Add, edit, delete customers
- Search by name
- Track order history
- Import/Export CSV

### Service Types
- Manage service catalog
- Set prices (in cents/grosze)
- Assign unique codes
- Descriptions

### Work Orders
- Create orders with multiple items
- **Apply per-item discounts (percentage-based)**
- Generate unique order numbers (YYYY-NNNNN format)
- Track order status (received, in_progress, ready, picked_up)
- Print barcode stickers for each item
- Generate A4 customer documents with pricing

### Discounts
- Percentage-based discounts (0-100%)
- Applied per individual item
- Automatic final price calculation
- Display original and discounted prices
- Track savings per order

### Printing
- Code-128 barcodes
- Item stickers with customer info
- A4 customer receipts with pricing
- Support for ZPL printers (Zebra/Brother)

### Backup & Sync
- Manual backup creation on-demand
- All backups retained permanently (no automatic deletion)
- Backup files automatically detected after database restore
- Optional Google Drive sync
- Backup history logging

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Building
```bash
mvn clean install
```

### Running
```bash
mvn javafx:run
```

Or run the main class:
```bash
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -jar target/ServiceManager-1.0-SNAPSHOT.jar
```

## 📁 Project Structure

```
src/main/java/com/zakilb/servicemanager/
├── model/              # Domain entities
├── repository/         # Data access layer (DAOs)
├── service/           # Business logic
├── ui/
│   ├── controllers/   # JavaFX controllers
│   └── viewmodels/    # Observable view models
└── util/              # Utility classes

src/main/resources/com/zakilb/servicemanager/
├── fxml/              # UI layout files
├── css/               # Stylesheets
└── db/                # Database schema

data/                  # Application data (database, backups)
```

## 📝 Development Status

- ✅ **Phase 1**: Project Setup - COMPLETED
- ✅ **Phase 2**: Database Layer - COMPLETED
- ✅ **Phase 3**: Model Layer - COMPLETED
- ✅ **Phase 4**: Service Layer - COMPLETED
- ✅ **Phase 5**: UI Layer - COMPLETED
- ✅ **Phase 6**: Backup UI - COMPLETED
- ✅ **Phase 7**: Settings UI - COMPLETED
- ✅ **Discount Feature**: Backend - COMPLETED, UI - PENDING

See `docs/tasks_checklist.md` for detailed task breakdown.
See `DISCOUNT_FEATURE.md` for discount implementation details.

## 📄 License

Proprietary - All rights reserved

## 👤 Author

Built with ❤️ for service shops
