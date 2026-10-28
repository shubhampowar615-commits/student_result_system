# Student Result Management System (College Mini-Project)

A complete, fully functional, standalone Java desktop application built for college mini-projects. Developed with **Java 21**, **Java Swing**, **SQLite + JDBC**, and built/managed via **Maven**.

---

## 🎨 Design & User Interface

The application features a traditional desktop Java Swing interface using a curated **navy blue, white, and light grey** palette:
- **Navy Blue Primary (`#1B365D`) & Dark Navy (`#0F213C`)**: Top branding header, sidebar navigation, table headers, and primary action buttons.
- **Light Grey (`#F5F7FA`) & Crisp Border Grey (`#DCE2EB`)**: Clean backgrounds, subtle outlines, and structured layout panels.
- **Pure White (`#FFFFFF`)**: High-contrast card surfaces, form dialogs, and report cards.
- **Status Badges**:
  - `PASS`: Deep Emerald Green (`#166534` on `#DCFCE7`)
  - `FAIL`: Deep Crimson Red (`#991B1B` on `#FEE2E2`)
  - `INCOMPLETE` / `ABSENT`: Deep Amber (`#B45309` on `#FEF3C7`)

---

## 🚀 Key Features

1. **Secure Administrator Authentication**:
   - SHA-256 password hashing with unique 16-byte random salts.
   - Initial database auto-seeding with default administrator account:
     - **Username**: `admin`
     - **Password**: `admin123`
2. **Interactive Dashboard**:
   - Real-time metric cards: Total Students, Total Subjects, Total Evaluations, Passed Count, Failed Count, Incomplete Count.
   - Quick Action navigation buttons and recent results overview.
3. **Student Management**:
   - Complete CRUD (Add, View, Edit, Delete).
   - Real-time search by Student Name or Roll Number.
   - Semester filtering.
   - **Enforced duplicate roll number prevention** across academic records.
4. **Subject Management & Curriculum Configuration**:
   - Fixed semester subjects, subject codes, maximum marks, passing marks, and credits.
   - **Editable Sample Defaults**: Pre-configured with typical engineering curriculum for Semesters 1 to 4 with a one-click "Reset Sample Defaults" button.
5. **Semester Marks Entry**:
   - Select semester and student; subjects for that semester load dynamically into an editable Swing table.
   - Client-side validation: prevents negative marks or marks exceeding maximum score.
   - Attendance tracking (`Absent` checkbox).
   - Pre-fills previously saved marks for easy corrections.
6. **Automatic Result Calculation & Strict Status Differentiation**:
   - Auto-computes Total Obtained, Total Maximum, Percentage, Letter Grade, and Status.
   - **Treats Incomplete Results Separately from Failed Results**:
     - If any subject is marked `Absent` or unentered, the overall status is **`INCOMPLETE`** (Grade: **`I`**), rather than `FAIL`.
     - Only when all subjects are attempted and one or more fall below the passing mark does the status become **`FAIL`** (Grade: **`F`**).
     - When all subjects meet passing thresholds, status is **`PASS`** with letter grade (`O`, `A+`, `A`, `B+`, `B`, `C`).
7. **Results Search & Filter**:
   - Search by student name or roll number.
   - Filter by semester (Sem 1 to 8) and status (`PASS`, `FAIL`, `INCOMPLETE`).
   - One-click button to recalculate all results if subjects or grading criteria change.
8. **Printable Report Card**:
   - Official institutional statement of marks and grade sheet directly inside Swing.
   - Displays college header, student details, course-wise marks, pass/fail status, percentage, final grade, and signature blocks.
   - **Native Desktop Printing**: Uses Java standard `java.awt.print.PrinterJob` to bring up the Windows Print Dialog directly (supports physical printers and Microsoft Print to PDF without any external PDF library).
9. **SQLite Data Persistence**:
   - Stores all data in a local SQLite file (`student_results.db`).
   - Preserves all records after application restart.
   - Enforces foreign keys (`ON DELETE CASCADE`) and prepared statements to prevent SQL injection.

---

## 📁 Project Structure

```
student-result-management-system/
├── pom.xml                                   # Maven configuration (Java 21, SQLite JDBC, JUnit 5)
├── mvnw.cmd                                  # Windows Maven Wrapper
├── README.md                                 # Project documentation
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── studentresult/
│   │               ├── Main.java             # Main application launcher
│   │               ├── model/                # Data models and DTOs
│   │               │   ├── GradeScale.java
│   │               │   ├── MarkEntry.java
│   │               │   ├── Result.java
│   │               │   ├── ResultSummary.java
│   │               │   ├── Student.java
│   │               │   ├── Subject.java
│   │               │   └── User.java
│   │               ├── dao/                  # Data Access Objects (Prepared SQL Statements)
│   │               │   ├── DatabaseManager.java
│   │               │   ├── MarkDao.java
│   │               │   ├── ResultDao.java
│   │               │   ├── StudentDao.java
│   │               │   ├── SubjectDao.java
│   │               │   └── UserDao.java
│   │               ├── service/              # Business Logic & Calculation Layer
│   │               │   ├── AuthService.java
│   │               │   ├── CalculationService.java
│   │               │   ├── ResultService.java
│   │               │   ├── StudentService.java
│   │               │   └── SubjectService.java
│   │               ├── ui/                   # Java Swing Desktop Interface
│   │               │   ├── DashboardPanel.java
│   │               │   ├── LoginFrame.java
│   │               │   ├── MainFrame.java
│   │               │   ├── MarksEntryPanel.java
│   │               │   ├── ReportCardPanel.java
│   │               │   ├── ResultsPanel.java
│   │               │   ├── StudentPanel.java
│   │               │   ├── SubjectPanel.java
│   │               │   └── UITheme.java
│   │               └── util/                 # Utilities
│   │                   ├── PasswordHasher.java
│   │                   ├── PrintUtil.java
│   │                   └── ValidationUtil.java
│   └── test/
│       └── java/
│           └── com/
│               └── studentresult/
│                   ├── dao/
│                   │   └── DatabaseIntegrationTest.java
│                   ├── service/
│                   │   └── CalculationServiceTest.java
│                   └── util/
│                       ├── PasswordHasherTest.java
│                       └── ValidationUtilTest.java
```

---

## 💻 Exact Windows Terminal Commands to Build, Test, and Run

Open PowerShell or Command Prompt in the project folder (`c:\Users\rajes\Downloads\Vibe Coder`):

### 1. Compile the Code
```powershell
.\mvnw.cmd clean compile
```
*(Or `mvn clean compile` if Maven is directly in your PATH)*

### 2. Run All JUnit 5 Tests
```powershell
.\mvnw.cmd clean test
```

### 3. Package into an Executable Fat JAR
```powershell
.\mvnw.cmd clean package
```
This produces an executable JAR with all dependencies included at:
`target\student-result-system.jar`

### 4. Launch the Application
Run the packaged JAR:
```powershell
java -jar target\student-result-system.jar
```
Or execute directly using Maven:
```powershell
.\mvnw.cmd exec:java -Dexec.mainClass="com.studentresult.Main"
```

---

## 🔑 Default Administrator Credentials
- **Username**: `admin`
- **Password**: `admin123`
*(Pre-seeded with demo students and sample curriculum for immediate demonstration)*

