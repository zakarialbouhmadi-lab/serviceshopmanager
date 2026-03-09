# Service Shop Manager

**Service Shop Manager** is a comprehensive desktop application built with JavaFX and SQLite, designed specifically to manage repair and maintenance service centers. Originally developed for a ski and bike service shop (kraftsport.pl), its highly adaptable architecture makes it suitable for any item-based service business (electronics, tailoring, mechanic shops, etc.).

It handles the complete lifecycle of a service order: from customer registration and item intake to barcode label printing, status tracking, and final PDF receipt generation.

## 🌟 Key Features

### 📦 Order & Item Management

* **Complete Order Lifecycle:** Track orders from "Received" -> "In Progress" -> "Ready" -> "Picked Up" or "Canceled".
* **Item-Level Tracking:** Add multiple items per order (e.g., two bikes in one order).
* **Dynamic Pricing & Discounts:** Apply percentage discounts per item and track the total vs. paid amounts.
* **Status History:** View a detailed timeline of status changes and notes for every order.

### 🖨️ Professional Printing & Barcodes

* **Thermal Label Support:** Natively generates and prints Code-128 barcode stickers for DYMO, Brady, and Brother label printers (supports custom sizes and ZPL).
* **PDF Receipts:** Generates professional A4 PDF order summaries for customers, complete with your shop's logo, contact info, itemized costs, and a scannable barcode footer.
* **Barcode Scanning:** Easily search and update items by scanning their printed barcode labels.

### 👥 Customer & Service Management

* **Customer Database:** Manage customer contact info, notes, and automatically track loyalty (number of historical orders).
* **Custom Service Types:** Define your own services (e.g., "Full Ski Wax", "Bike Brake Bleed") with unique codes and preset pricing.

### ⚙️ System Features

* **Automated Backups:** Built-in SQLite database backup system with manual and automated daily backup options, plus safe restore functionality.
* **Bilingual Interface:** Fully internationalized (i18n) with out-of-the-box support for English and Polish.
* **Customizable Settings:** Upload your shop logo, configure taxes/currency, and set specific printers for A4 documents vs. label stickers.

---


### Main Dashboard (Orders View)


*The main dashboard where you can filter orders by status, date, and search by barcode.*

### Creating a New Order


*Adding a customer, selecting service types, and applying discounts.*

### Customer Management


*Managing the customer database and tracking order history.*

### Sticker and PDF Generation


*Preview of the auto-generated thermal stickers and A4 PDF receipts.*

### Application Settings


*Configuring shop details, logos, and specific hardware printers.*

---

### 📸 Application Gallery

<img width="1920" height="1080" alt="Screenshot_20260309_234231" src="https://github.com/user-attachments/assets/9e43681d-f4ce-4311-845d-3d9dbb456093" />
<img width="751" height="689" alt="Screenshot_20260309_234406" src="https://github.com/user-attachments/assets/df97c37e-bf79-43d5-991b-48b2db8e2ef2" />
<img width="751" height="689" alt="Screenshot_20260309_234424" src="https://github.com/user-attachments/assets/115f77c7-caa8-4cb5-91b3-5f78320fcd31" />
<img width="615" height="535" alt="Screenshot_20260309_234639" src="https://github.com/user-attachments/assets/092fef39-9186-4409-a136-3913c7357588" />
<img width="615" height="625" alt="Screenshot_20260309_234717" src="https://github.com/user-attachments/assets/11883243-dfdd-4940-abf0-961e746bcc18" />
<img width="725" height="638" alt="Screenshot_20260309_234813" src="https://github.com/user-attachments/assets/eb7723cb-fd00-4059-8081-c1494bbf7817" />
<img width="775" height="674" alt="Screenshot_20260309_234841" src="https://github.com/user-attachments/assets/4a97eb64-e189-4bf3-ab20-879dd52ee629" />
<img width="1917" height="1053" alt="Screenshot_20260309_234918" src="https://github.com/user-attachments/assets/e9a4ef06-b4e6-418c-a60e-765cb1aa5ab0" />
<img width="1917" height="1053" alt="Screenshot_20260309_234948" src="https://github.com/user-attachments/assets/dbdc261a-6a68-4ec0-af8d-4dd314505746" />
<img width="1917" height="1053" alt="Screenshot_20260309_235006" src="https://github.com/user-attachments/assets/5bd780d8-e7d0-4719-b888-95992554b50a" />
<img width="1917" height="1053" alt="Screenshot_20260309_235015" src="https://github.com/user-attachments/assets/e962e55b-3331-4725-a346-5cead55ab61c" />
<img width="1917" height="1053" alt="Screenshot_20260309_235025" src="https://github.com/user-attachments/assets/5c925a78-89aa-4379-8dba-12b6a0deba80" />


## 🏗️ Architecture & Tech Stack

The application is built using a clean, layered **MVC (Model-View-Controller)** architecture to ensure maintainability and separation of concerns.

* **Language:** Java 17+ (Modularized via `module-info.java`)
* **UI Framework:** JavaFX with FXML and custom CSS styling
* **Database:** SQLite (Local, zero-configuration database)
* **Key Libraries:**
* `ZXing` - For generating high-quality Code-128 barcodes.
* `OpenPDF` / `PDFBox` - For A4 document generation and printing.
* `SLF4J` - For standardized application logging.



### Directory Structure Highlights

* `model/` - Domain entities (`WorkOrder`, `Customer`, `ServiceType`, etc.) mapping to the DB.
* `repository/` - Data Access Objects (DAOs) handling pure SQLite SQL queries and transactions.
* `service/` - Core business logic, validation, and complex workflows (e.g., `PrintingService`, `BackupService`).
* `ui/` - JavaFX Controllers, ViewModels (for reactive data binding), and FXML views.
* `util/` - Helpers for PDF layout (`PdfGenerator`), sticker drawing (`StickerTemplate`), and database initialization.

---

## 🚀 Getting Started

### Prerequisites

* **JDK 17** or higher.
* **Maven** or **Gradle** (Depending on your build setup).
* *Optional:* A thermal label printer (e.g., DYMO LabelWriter or Brady) to test sticker printing.

### Running the Application

1. **Clone the repository:**
```bash
git clone https://github.com/yourusername/ServiceShopManager.git
cd ServiceShopManager

```


2. **Build and Run:**
If using Maven:
```bash
mvn clean javafx:run

```


*(Alternatively, run the `Launcher.java` class directly from your IDE).*
3. **First Run Initialization:**
* On the first launch, the app automatically creates the SQLite database in your OS's native AppData folder (e.g., `%LOCALAPPDATA%\ServiceShopManager` on Windows or `~/.local/share/ServiceShopManager` on Linux).
* Sample data can be loaded via the `SampleDataLoader` utility for testing.



---

## 🛠️ Configuration & Setup

1. Navigate to the **Settings** tab in the application.
2. Upload your shop's **Logo** (recommended size: 200x58px).
3. Fill in your shop's contact details (these appear on the printed PDFs).
4. Select your default **A4 Printer** and **Label Printer** from the dropdowns. Configure the sticker dimensions based on your label rolls.
5. Select your preferred UI Language (Polish or English).

---

## 📄 License

All Rights Reserved. This repository is available for portfolio and educational viewing purposes only. Commercial or non-commercial use, distribution, or modification is strictly prohibited without explicit written permission.
