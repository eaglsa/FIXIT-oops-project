# FixIt – Digital Complaint & Maintenance Tracker

A command-line Java application for tracking hostel/campus maintenance complaints.
Built as a KTU 2024 OOP mini-project.

---

## Prerequisites

| Tool    | Minimum version |
|---------|----------------|
| JDK     | 17 (or 21 / 25) |
| Maven   | 3.6+           |
| MySQL   | 8.x            |

---

## Setup

### 1. Create the database

```bash
mysql -u root -p < sql/schema.sql
```

This creates the `fixit` database, all tables, and the four seed users:

| Username     | Role    | Password  |
|-------------|---------|-----------|
| warden      | STAFF   | staff123  |
| electrician | STAFF   | staff123  |
| student1    | STUDENT | pass123   |
| student2    | STUDENT | pass123   |

### 2. Configure the database connection

Copy the example properties file and fill in your MySQL password:

```bash
copy db.properties.example src\main\resources\db.properties
```

Open `src/main/resources/db.properties` and replace `YOUR_PASSWORD_HERE` with your actual MySQL root password:

```properties
db.url=jdbc:mysql://localhost:3306/fixit?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=yourActualPasswordHere
```

> **Note:** `db.properties` is listed in `.gitignore` and will never be committed to version control.

---

## Running the Application

```bash
mvn compile exec:java
```

The application starts in the terminal with a menu-driven interface.

---

## Project Structure

```
FixIt/
├── pom.xml                         Maven build file
├── sql/
│   └── schema.sql                  Database schema + seed data
├── db.properties.example           Template for DB credentials
├── src/main/
│   ├── resources/
│   │   └── db.properties           Your local DB credentials (not committed)
│   └── java/com/fixit/
│       ├── Main.java               Entry point
│       ├── model/                  Domain classes (Complaint, User, enums, interface)
│       ├── service/                Business logic (AuthService, ComplaintService, Factory)
│       ├── dao/                    SQL access (UserDAO, ComplaintDAO)
│       ├── db/                     DBConnector Singleton
│       ├── ui/                     CLIMenu (Scanner + System.out)
│       └── exception/              Four custom checked exceptions
```

---

## OOP Concepts Used

| Concept       | Where                                                              |
|---------------|--------------------------------------------------------------------|
| Abstraction   | `Complaint` (abstract class), `User` (abstract class)             |
| Inheritance   | `ElectricalComplaint`, `PlumbingComplaint`, `WifiComplaint`, `GenericComplaint`, `Student`, `Staff` |
| Polymorphism  | `getAssignedTeam()`, `getEstimatedResolutionHours()`, `getSafetyNote()`, `getRole()`, `getWelcomeMessage()` |
| Interface     | `Updatable` – implemented by `Complaint`                          |
| Encapsulation | Private fields in all model classes, accessed via getters only    |
| Singleton     | `DBConnector` – one shared DB connection                          |
| Factory       | `ComplaintFactory` – creates the right subclass from a type enum  |
| Custom Exceptions | `InvalidComplaintException`, `InvalidStatusTransitionException`, `UnauthorizedActionException`, `AuthenticationException` |
| Enum with behaviour | `Status.canTransitionTo()` – encodes workflow rules           |

---

## Demo Flow

1. Run the app: `mvn compile exec:java`
2. Log in as `student1 / pass123`
3. Raise a plumbing complaint → observe the preview (team, ETA, safety note)
4. Log out, log in as `warden / staff123`
5. View all complaints → update status (REPORTED → IN_PROGRESS → RESOLVED)
6. Log back in as `student1` → view complaint details & history

---

## Notes

- Passwords are stored in plain text (acceptable for a demo project).
- All SQL uses `PreparedStatement` to prevent SQL injection.
- The `status_log` table records every status change with a timestamp and the staff name.
