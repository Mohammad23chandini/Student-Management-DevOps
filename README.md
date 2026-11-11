# 🎓 Student Management Pro — DevOps & Modern Dashboard

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.4-blue.svg?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg?logo=docker&logoColor=white)](https://www.docker.com/)
[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-Orchestrated-blue?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-D24939.svg?logo=jenkins&logoColor=white)](https://www.jenkins.io/)
[![UI Design](https://img.shields.io/badge/UI%2FUX-macOS%20Inspired-000000.svg?logo=apple&logoColor=white)](https://developer.apple.com/design/human-interface-guidelines/)

A commercial-grade **Student Information System (SIS) & Management Dashboard** built with **Java 21**, **Spring Boot**, **MySQL 8.4**, and **DevOps automation** (Docker, Docker Compose, and Jenkins CI). 

The user interface follows the **Apple Human Interface Guidelines (macOS Sequoia / Sonoma)**, combining elegant glassmorphism, SF Pro typography, refined animations, and an authentic desktop experience with robust backend persistence, academic tracking, and financial tuition fee management.

---

## 🌟 Key Highlights & Features

### 🖥️ 1. macOS-Inspired Interface Design
- **macOS Window Frame & Traffic Lights**: Working red (close), amber (minimize), and green (zoom/fullscreen) window controls.
- **Translucent Frosted Glass Surfaces**: Backdrop blur effects (`backdrop-filter: blur(25px)`), subtle drop shadows, and clean borders.
- **System Typography & Apple Color Palette**: SF Pro font stack with Apple Blue (`#0071e3`), Emerald Green (`#34c759`), and Accent Purple (`#af52de`).
- **Dark / Light Mode**: Seamless theme switcher with live icon changes and `localStorage` state persistence.
- **Collapsible Sidebar**: Compact icon-only view or expanded full navigation menu with live database connection telemetry.

### 📚 2. Academic Performance Tracking (10.0 CGPA Scale)
- **Roll Number / Student ID**: Official institutional roll number (e.g., `23CSE045`, `23CSE015`).
- **Semester & Year Level**: Track student academic progression from `Semester 1` through `Semester 8`.
- **Cumulative CGPA & Academic Standing**: Live 10.0 CGPA scale tracking (0.00 – 10.00) with automatic Indian university honor classifications:
  - `🌟 Distinction` — CGPA >= 9.00
  - `🟢 First Class` — CGPA 7.50 – 8.99
  - `🟡 Second Class` — CGPA 6.00 – 7.49
  - `🔴 Academic Warning` — CGPA < 6.00
- **Attendance Monitoring**: Percentage tracker with color-coded alerts (`🟢 >= 85%`, `🟠 75%–84%`, `🔴 < 75% Low Attendance`).

### 💳 3. Tuition & Fee Management (Indian Rupees ₹)
- **Tuition Ledger in ₹ INR**: Track Total Tuition Invoiced (e.g. ₹85,000), Paid Amount, and Remaining Due Balances in Indian Rupees.
- **Dynamic Fee Status**:
  - `🟢 Paid` — Full tuition cleared (₹0 balance)
  - `🟠 Partial` — Partial payment credited with outstanding balance
  - `🔴 Due / Overdue` — Pending balance with deadline tracking
- **One-Click Payment Posting**: In-place modal to post tuition fee payments in ₹ with instant balance recalculation and persistent audit logging.
- **Financial Collection KPIs**: Real-time campus fee realization rate progress bar and outstanding revenue analytics in Indian Rupees.

### 📊 4. Dynamic Dashboard Overview
- **Real-Time Database Statistics**:
  - **Enrolled Students**: Live count from MySQL across academic departments.
  - **Campus Average CGPA**: Institutional average on the 10.0 scale (e.g., `8.50 / 10.0`) and campus attendance rate.
  - **Tuition Fees Collected (₹)**: Total payments received in INR and collection realization rate.
  - **Outstanding Tuition Dues (₹)**: Real-time pending balance indicator.
- **Department & Fee Breakdown Charts**: Interactive Chart.js Donut Charts with percentage distributions and automatic SVG fallback for offline environments.
- **Persistent Activity Audit Stream**: Real audit events recorded directly into MySQL (`activity_logs` table) upon student creation, update, payment posting, deletion, and CSV import.

### 🧑‍🎓 5. Comprehensive Student Directory & Table
- **Modern Data Table**:
  - Dynamic initials avatars styled with a curated Apple pastel color palette.
  - Roll number, full name, institutional email, department, semester.
  - Color-coded 10.0 CGPA, attendance, and ₹ INR fee status badges.
- **Instant Live Search**: Client-side filtering across student name, email, department, and roll number in real time.
- **Multi-Level Filters**: Filter by Department, Semester (Sem 1 to 8), and Fee Status (Paid, Partial, Due, Overdue).
- **Multi-Option Sorting**: Sort by Newest First, Oldest First, Name (A–Z), Highest CGPA, and Highest Due Fee.
- **Modal Sheets**:
  - **Enroll Student Modal**: Comprehensive 2-column modal for identity, 10.0 CGPA academic standing, and initial ₹ fee structure.
  - **Edit Student Modal**: In-place modal to update all academic and billing metrics.
  - **Student Profile Inspector**: Full dual-view card for academic standing (10.0 scale) and ₹ INR fee ledger.
  - **Record Payment Modal**: Quick sheet to post fee payments in ₹ with immediate receipt confirmation.
  - **Delete Confirmation Modal**: Custom destructive warning sheet with confirmation.
- **Multi-Select & Bulk Deletion**: Batch student row selection with confirmation-gated atomic deletion.

### 📁 6. CSV Import & Export Utilities
- **RFC 4180-Compliant CSV Export**: One-click download of the entire registry (ID, Roll Number, Name, Email, Department, Semester, GPA, Attendance, Total Fee, Paid Fee, Due Balance, Fee Status, Registration Date) with UTF-8 BOM encoding.
- **Batch CSV Import**: File dropzone for batch uploading students with duplicate checking and format validation.

---

## 🏛️ System Architecture

```mermaid
graph TD
    User([Browser / Client]) -->|Port 8081| App[Spring Boot 4.1.2 App Container]
    
    subgraph Docker Network
        App -->|Port 3306| MySQL[(MySQL 8.4 Database Container)]
    end
    
    subgraph Spring Boot Application Architecture
        App --> Controller[StudentController]
        Controller --> Service[StudentService]
        Service --> StudentRepo[StudentRepository]
        Service --> ActivityRepo[ActivityLogRepository]
        StudentRepo --> MySQL
        ActivityRepo --> MySQL
        Controller --> Thymeleaf[Thymeleaf Engine & macOS Templates]
    end

    subgraph CI/CD Pipeline
        Git[GitHub Repository] -->|Webhook / SCM| Jenkins[Jenkins CI Pipeline]
        Jenkins -->|Maven Build| JAR[app.jar]
        Jenkins -->|Docker Build| ContainerImage[Docker Image]
        ContainerImage -->|Compose Deploy| Production[Running Containers]
    end
```

---

## 💻 Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 4.1.2, Spring Data JPA, Hibernate ORM |
| **Database** | MySQL 8.4 Community Edition |
| **Frontend** | Thymeleaf 3.x, HTML5, Vanilla JavaScript (ES6+), CSS3 (macOS Design Tokens), Chart.js |
| **DevOps & Containers** | Docker, Docker Compose, Eclipse Temurin 21 JRE Base Image |
| **CI / CD** | Git, GitHub, Jenkins Pipeline |
| **Build Tool** | Apache Maven 3.9+ |

---

## 🚀 Getting Started

### Option 1: Running with Docker Compose (Recommended)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Mohammad23chandini/Student-Management-DevOps.git
   cd Student-Management-DevOps/demo
   ```

2. **Package the application JAR:**
   ```bash
   mvn package -DskipTests
   ```

3. **Start the containers via Docker Compose:**
   ```bash
   docker compose up -d --build
   ```

4. **Access the application at:** `http://localhost:8081`

---

### Option 2: Running Locally with Maven

1. **Start a local MySQL 8.4 instance** and verify `src/main/resources/application.properties` credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/student_management
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   server.port=8081
   ```

2. **Compile and run the Spring Boot application:**
   ```bash
   mvn clean spring-boot:run
   ```

3. **Access the application at:** `http://localhost:8081`

---

## 📡 REST API & Web Routes Reference

| HTTP Method | Route | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Dashboard Overview with Academic KPIs, Financial Metrics & Activity Feed |
| `GET` | `/students` | All Students Directory with Academic & Billing Table, Search & Filters |
| `POST` | `/students` | Enroll a new student record (with academic standing & fee structure) |
| `GET` | `/students/edit/{id}` | Standalone edit page for student academic and fee profile |
| `POST` | `/students/update/{id}` | Update existing student academic and financial details |
| `POST` | `/students/payment/{id}` | Record and post tuition fee payments |
| `GET` | `/students/delete/{id}` | Delete a student record by ID |
| `POST` | `/students/bulk-delete` | Batch delete selected student IDs |
| `GET` | `/students/export` | Download full registry CSV export (with GPA, Attendance, Fees) |
| `POST` | `/students/import` | Upload & import student records from CSV file |
| `GET` | `/analytics` | Academic department breakdown and tuition realization analytics |
| `GET` | `/reports` | CSV management and printable roster reports view |
| `GET` | `/settings` | System tech stack runtime metadata & theme config |
| `GET` | `/api/stats` | JSON endpoint returning live academic and financial metrics |
| `GET` | `/api/students/{id}` | JSON endpoint returning individual student data |
| `POST` | `/students/seed-demo` | Seeds realistic multi-department records with GPA and fee statuses |

---

## 🗄️ Database Schema Evolution

Hibernate automatically synchronizes the MySQL database schema using `spring.jpa.hibernate.ddl-auto=update`.

### `students` Table
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique student identifier |
| `roll_number` | `VARCHAR(255)` | NULL | Institutional Roll / ID number |
| `name` | `VARCHAR(255)` | NOT NULL | Legal student full name |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE | Campus or personal email |
| `course` | `VARCHAR(255)` | NOT NULL | Department / Academic program |
| `semester` | `VARCHAR(255)` | NULL | Current academic semester (`Semester 1`–`8`) |
| `gpa` | `DOUBLE` | NULL | Cumulative Grade Point Average on 10.0 scale (0.00 – 10.00) |
| `attendance` | `INTEGER` | NULL | Attendance percentage (0 – 100) |
| `academic_status` | `VARCHAR(255)` | NULL | Standing (`Distinction`, `First Class`, `Second Class`, `Academic Warning`) |
| `total_fee` | `DOUBLE` | NULL | Total tuition fee invoiced in Indian Rupees (₹) |
| `paid_fee` | `DOUBLE` | NULL | Total fee payments received in Indian Rupees (₹) |
| `fee_status` | `VARCHAR(255)` | NULL | Status (`PAID`, `PARTIAL`, `DUE`, `OVERDUE`) |
| `fee_due_date` | `DATE` | NULL | Tuition payment deadline |
| `registration_date` | `DATE` | NULL | Initial enrollment date |

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
