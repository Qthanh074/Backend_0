# Smart Retail Backend

A comprehensive backend system for Smart Retail with AI capabilities, built with Spring Boot.

## Features

- **Product Management**: Complete product catalog with variants, categories, and inventory
- **Order Management**: Order processing, payment tracking, and delivery management
- **Customer Management**: Customer profiles, loyalty points, and purchase history
- **Store Management**: Multi-store support with regional management
- **AI Integration**: AI-powered recommendations and sales forecasting
- **User Management**: Employee roles and permissions
- **Reporting**: Sales analytics and business intelligence
- **Security**: JWT-based authentication and authorization

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Database**: MySQL/PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven
- **Java Version**: 17

## Database Schema

The system includes the following main entities:

### Core Entities
- `KhuVuc` - Regions/Areas
- `CuaHang` - Stores
- `DanhMuc` - Product Categories (hierarchical)
- `DonVi` - Units of Measurement
- `SanPham` - Products
- `BienTheSanPham` - Product Variants
- `HinhAnhSanPham` - Product Images

### Customer & Loyalty
- `KhachHang` - Customers
- `HangKhachHang` - Customer Ranks
- `TheTichDiem` - Loyalty Cards
- `LichSuDiem` - Point Transaction History

### Orders & Payments
- `HoaDon` - Invoices/Orders
- `ChiTietHoaDon` - Order Details
- `HinhThucThanhToan` - Payment Methods
- `GiaoDichThanhToan` - Payment Transactions
- `GiaoHang` - Shipping/Delivery
- `Shipper` - Delivery Personnel

### Procurement
- `NhaCungCap` - Suppliers
- `PhieuNhapHang` - Purchase Orders
- `ChiTietPhieuNhap` - Purchase Order Details

### AI Features
- `AiAgent` - AI Agents/Models
- `AiHoiThoai` - AI Conversations
- `AiTinNhan` - AI Messages
- `AiGoiY` - AI Recommendations
- `AiDuDoan` - AI Predictions/Forecasts

### User Management
- `NhanVien` - Employees
- `VaiTro` - Roles
- `NhanVienVaiTro` - Employee-Role Relationships

### Additional
- `SoQuy` - Cash Book/Ledger
- `DanhGiaSanPham` - Product Reviews

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL/PostgreSQL database

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd smart-retail-backend
   ```

2. **Configure Database**

   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/smart_retail?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Documentation

### Swagger UI
Access the API documentation at: `http://localhost:8080/swagger-ui.html`

### Base URL
```
http://localhost:8080/api/v1
```

### Key Endpoints

#### Products
- `GET /san-pham` - Get all products
- `GET /san-pham/{id}` - Get product by ID
- `POST /san-pham` - Create new product
- `PUT /san-pham/{id}` - Update product
- `DELETE /san-pham/{id}` - Delete product

#### Product Variants
- `GET /bien-the-san-pham` - Get all variants
- `GET /bien-the-san-pham/san-pham/{sanPhamId}` - Get variants by product

#### Orders
- `GET /hoa-don` - Get all orders
- `GET /hoa-don/{id}` - Get order by ID
- `POST /hoa-don` - Create new order
- `PUT /hoa-don/{id}/trang-thai` - Update order status

#### AI Features
- `GET /ai-goi-y/khach-hang/{khachHangId}/top-recommendations` - Get AI recommendations
- `GET /ai-du-doan/cua-hang/{cuaHangId}/loai/{loaiDuDoan}` - Get AI predictions

#### Authentication
- `POST /auth/login` - User login
- `POST /auth/register` - User registration

## Security

The application uses JWT (JSON Web Tokens) for authentication:

1. **Login** to receive a JWT token
2. **Include token** in Authorization header: `Bearer <token>`
3. **Token expiration**: 24 hours (configurable)

## Configuration

Key configuration properties in `application.properties`:

```properties
# Server
server.port=8080
server.servlet.context-path=/api/v1

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/smart_retail
spring.datasource.username=root
spring.datasource.password=password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=mySecretKey
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=10MB
```

## Development

### Project Structure
```
src/main/java/com/smartretail/backend/
├── controller/          # REST controllers
├── entity/             # JPA entities
├── repository/         # Data repositories
├── service/            # Business logic
├── dto/                # Data transfer objects
├── security/           # Security configuration
├── exception/          # Exception handling
└── SmartRetailBackendApplication.java
```

### Adding New Features

1. **Create Entity** in `entity/` package
2. **Create Repository** interface extending `JpaRepository`
3. **Create Service** class with business logic
4. **Create Controller** with REST endpoints
5. **Add DTOs** if needed for request/response
6. **Update Security** configuration if required

### Testing

Run tests with:
```bash
mvn test
```

## Deployment

### Build for Production
```bash
mvn clean package -DskipTests
```

### Docker Support
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/smart-retail-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For support and questions, please contact the development team.