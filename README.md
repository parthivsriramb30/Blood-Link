# BloodLink — Blood Donor Registry and Search

A clean, responsive, and functional college-level blood donor registry and search platform built using **Spring Boot**, **Spring Data JPA**, **Hibernate**, **Thymeleaf**, and **MySQL**.

The purpose of **BloodLink** is to help hospitals, patients, and healthcare coordinators quickly find available blood donors filtered by blood group and city during emergency requirements.

---

## 🚀 Features

- **Dashboard**: Real-time statistics including total registered donors, active/available donors, and total donation records. Quick blood group lookup cards with direct search filters.
- **Donor Registration**: Form with client-side and server-side validation (10-digit mobile number, age 18–65, weight ≥ 50kg, email format).
  - Duplicate registration prevention (matching existing active donor details).
  - Medical eligibility checklist (no recent major surgery, tattoos in last 6 months, or chronic blood diseases).
- **Fast Search & Filter**: Search donors by blood group and city. Toggle between all donors and only currently eligible/available donors.
- **90-Day Cooldown Tracker**: Automatically calculates donor availability status based on their last donation date (eligible to donate only after 90 days).
- **Donation History Tracking**: Record new blood donations with donation date, units, hospital name, and remarks. The donor's status dynamically updates upon donation logging.
- **Donor Directory**: View donor profile details, contact information, eligibility badge, and complete past donation history.

---

## 🛠️ Technology Stack

- **Backend**: Java 21, Spring Boot 3.5.x
- **Frameworks**: Spring MVC, Spring Data JPA, Hibernate, Bean Validation (`jakarta.validation`)
- **Template Engine**: Thymeleaf
- **Database**: MySQL 8.0+
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Build Tool**: Maven

---

## 🏗️ Architecture

```
Thymeleaf (Views)
      ↓
Spring MVC Controllers (DonorController, HomeController, DonationController)
      ↓
Service Layer (DonorService, DonationService)
      ↓
Repository Layer (DonorRepository, BloodGroupRepository, DonationRecordRepository)
      ↓
Spring Data JPA / Hibernate
      ↓
MySQL Database (bloodlink_db)
```

---

## ⚙️ Prerequisites & Setup

### 1. Prerequisites
- **Java Development Kit (JDK 21)**
- **MySQL Server (8.0 or later)**
- **Git**

### 2. Database Setup
Create database (or let Spring Boot create it automatically):
```sql
CREATE DATABASE IF NOT EXISTS bloodlink_db;
```

Update your MySQL credentials in `src/main/resources/application.properties` if needed:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bloodlink_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=cse@29
```

### 3. Run the Application
Open a terminal in the project root directory:

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

Once started, access the application in your browser:
```
http://localhost:8080
```

---

## 📋 Database Schema

- **`blood_groups`**: Holds the 8 standard blood group types (`A+`, `A-`, `B+`, `B-`, `AB+`, `AB-`, `O+`, `O-`) seeded on first run.
- **`donors`**: Stores registered donor information (`full_name`, `age`, `gender`, `blood_group_id`, `mobile_number`, `email`, `city`, `address`, `weight_kg`, `availability_status`, `last_donation_date`).
- **`donation_records`**: Tracks donation logs (`donor_id`, `donation_date`, `units_donated`, `hospital_name`, `notes`).

---

## 🧪 Running Tests

Execute the unit tests using Maven:
```powershell
.\mvnw.cmd test
```

---

## 📄 License
This project is open-source for educational and academic purposes.
