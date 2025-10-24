# Smart Cinema Ticketing & Experience Management System
### Enterprise Software Analysis & Design (SE5060)
**Group ID:** ESAD-03  

---

## Project Overview
The **Smart Cinema Ticketing & Experience Management System** is a full-stack enterprise web application developed for cinema operations automation.  
It enables customers to browse movies, book tickets, make payments, and receive digital QR-based tickets, while administrators manage movies, schedules, pricing, and revenue reports from a central portal.

The system was implemented as part of the **Enterprise Software Analysis & Design (SE5060)** module at SLIIT, transforming the approved UML design into a fully functional application.

---

## System Architecture
The system follows a **multi-tier (MVC)** architecture built on **Java Spring Boot 3**, **MySQL**, and **Thymeleaf**.

**Layers:**
- **Presentation Layer:** Thymeleaf + Tailwind CSS templates for public, admin, and gatekeeper portals.
- **Business Logic Layer:** Spring Services handling core workflows (Booking, Movies, Payments, Reports).
- **Data Access Layer:** Spring Data JPA repositories with Hibernate ORM.
- **Security Layer:** Spring Security for role-based authentication and password encryption.

---

## Technology Stack
| Category | Technology |
|-----------|-------------|
| Language | Java 17 |
| Framework | Spring Boot 3 (MVC, Security, JPA) |
| Database | MySQL 8.x |
| View Engine | Thymeleaf |
| CSS Framework | Tailwind CSS |
| Build Tool | Maven |
| Version Control | Git |
| Mail Service | Gmail SMTP (TLS) |
| QR Code Generation | ZXing Library |

---

## Core Modules
| Module | Description |
|---------|-------------|
| **Authentication** | Secure login/register with Spring Security and BCrypt encryption. |
| **Movie Management** | CRUD operations for movies, posters, and showtimes. |
| **Ticket Booking** | Real-time seat booking, price calculation, and confirmation. |
| **Payment Processing** | Simulated online and cash payment options. |
| **QR Validation** | Gatekeeper module validates tickets through QR scanning. |
| **Revenue Reporting** | Admin dashboard with revenue analytics and CSV export. |
| **Pricing Management** | Admin configuration of seat-type and time-based pricing. |

---

## Security Implementation
- Role-based access control (`ROLE_ADMIN`, `ROLE_USER`)
- Encrypted passwords via **BCryptPasswordEncoder**
- Secure HTTPS communication (recommended for deployment)
- CSRF protection for form submissions
- Login auditing through `LoginAuditSuccessHandler`
- Input validation and sanitized templates

## Setup & Execution

### **1. Prerequisites**
- Java JDK 17+
- Maven 3.9+
- MySQL Server 8+
- Internet connection for Tailwind CDN and TMDB API access


### Update environment variables or .env file:
``
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/cinema_management?useSSL=true
SPRING_DATASOURCE_USERNAME=cinema_app
SPRING_DATASOURCE_PASSWORD=your_password
MAIL_USERNAME=your_gmail@example.com
MAIL_PASSWORD=your_app_password
``
### Build & Run 
``
mvn clean install
mvn spring-boot:run
 ``

### Access the application:

Public Site: http://localhost:8080
Admin Portal: http://localhost:8080/admin
