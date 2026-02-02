# Rental Property Management System

A simple and elegant Spring Boot application for managing rental properties with a beautiful web UI and flat file storage.

## Features

- ✨ Add, edit, and delete rental properties
- 📊 Dashboard with real-time statistics
- 🔍 Filter properties by status and type
- 👥 Track tenant information for rented properties
- 💰 Monitor rental amounts
- 🎨 Modern, responsive UI
- 💾 **Flat file storage** - All data stored in JSON format (no database required!)

## Technologies Used

- **Backend:** Spring Boot 3.2.0
- **Storage:** JSON flat file (properties-data.json)
- **Frontend:** HTML, CSS, JavaScript
- **Template Engine:** Thymeleaf

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Setup Instructions

1. **Clone or extract the project**

2. **Navigate to the project directory**
   ```bash
   cd rental-management
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   
   Or run the JAR file:
   ```bash
   java -jar target/rental-management-1.0.0.jar
   ```

5. **Access the application**
   - Main Application: http://localhost:8080

## Data Storage

All property data is stored in a file called `properties-data.json` in the project root directory. This file is automatically created when you add your first property and is updated whenever you make changes.

**Benefits of flat file storage:**
- No database setup required
- Easy to backup (just copy the JSON file)
- Human-readable format
- Perfect for small-scale operations
- Portable across systems

**Data file location:** `properties-data.json` (created automatically in the application directory)

## Project Structure

```
rental-management/
├── src/
│   ├── main/
│   │   ├── java/com/rental/
│   │   │   ├── RentalManagementApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── PropertyController.java
│   │   │   │   └── ViewController.java
│   │   │   ├── model/
│   │   │   │   └── Property.java
│   │   │   └── service/
│   │   │       ├── FileStorageService.java
│   │   │       └── PropertyService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           └── index.html
├── pom.xml
└── properties-data.json (created automatically)
```

## API Endpoints

### Properties

- `GET /api/properties` - Get all properties
- `GET /api/properties/{id}` - Get property by ID
- `POST /api/properties` - Create new property
- `PUT /api/properties/{id}` - Update property
- `DELETE /api/properties/{id}` - Delete property
- `GET /api/properties/status/{status}` - Get properties by status
- `GET /api/properties/type/{type}` - Get properties by type

## Usage

### Adding a Property

1. Click the "Add New Property" button
2. Fill in the required information:
   - Address
   - Property Type (Apartment, House, Condo, Studio)
   - Status (Available, Rented, Maintenance)
   - Number of bedrooms and bathrooms
   - Rent amount
3. If status is "Rented", fill in tenant information
4. Click "Save Property"

### Editing a Property

1. Click the "Edit" button on any property card
2. Update the information
3. Click "Save Property"

### Deleting a Property

1. Click the "Delete" button on any property card
2. Confirm the deletion

### Filtering Properties

Use the dropdown filters to view:
- Properties by status (Available, Rented, Maintenance)
- Properties by type (Apartment, House, Condo, Studio)

## Backup and Restore

### Backing up your data
Simply copy the `properties-data.json` file to a safe location.

### Restoring your data
Replace the `properties-data.json` file with your backup copy and restart the application.

## Customization

### Changing the Port

Edit `application.properties`:
```properties
server.port=8081
```

### Changing the Data File Location

Edit `FileStorageService.java` and update the `DATA_FILE` constant:
```java
private static final String DATA_FILE = "/path/to/your/data/properties-data.json";
```

## Migrating to a Database (Optional)

If your needs grow and you want to migrate to a database:
1. Add JPA and database dependencies to `pom.xml`
2. Add JPA annotations back to `Property.java`
3. Create a `PropertyRepository` interface
4. Update `PropertyService` to use the repository instead of `FileStorageService`
5. Configure database properties in `application.properties`

## Future Enhancements

- User authentication and authorization
- Payment tracking and reminders
- Maintenance request management
- Document upload for leases
- Email notifications
- Reports and analytics
- Mobile app integration
- Data export to CSV/Excel

## License

This project is open source and available for educational purposes.

## Support

For issues or questions, please create an issue in the project repository.