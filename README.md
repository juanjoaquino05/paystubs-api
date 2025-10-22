# Paystubs API

A Spring Boot REST API for processing payroll CSV files, generating PDF paystubs, and sending them via email to employees.

## Features

- 📄 CSV payroll file parsing
- 📊 PDF paystub generation with company logos
- 📧 Automated email delivery with PDF attachments
- 🌍 Internationalization support (English/Spanish)
- 🔒 Custom parameter-based authentication
- 🏢 Multi-country support (Dominican Republic, USA)

## Prerequisites

- **Java 25** or higher
- **Maven 3.9+** (included via Maven Wrapper)
- **Docker** (optional, for local email testing with Mailpit)

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd paystubs-api
```

### 2. Configure Environment Variables

Create a `.env` file or set the following environment variables:

```bash
# Authentication credentials
export APP_USER=your_username
export APP_PASSWORD=your_password

# Email/SMTP Configuration
export MAIL_HOST=localhost
export MAIL_PORT=1025
export MAIL_USER=noreply@example.com
export MAIL_PASSWORD=
export MAIL_AUTH=false
export MAIL_STARTTLS=false
```

### 3. Start Local Email Server (Optional)

For local development and testing, use Mailpit with Docker Compose:

```bash
docker-compose up -d
```

Access Mailpit web interface at: http://localhost:8025

### 4. Add Company Logos

Place company logo images in the `logos/` directory:

```
logos/
├── YourCompanyName.png
├── AnotherCompany.png
└── default.png (fallback logo)
```

- Logo files should be named `{company}.png` (e.g., `Acme.png`)
- If a specific logo is not found, `default.png` will be used
- Recommended size: 140x60 pixels or similar aspect ratio

### 5. Run the Application

Using Maven Wrapper:

```bash
./mvnw spring-boot:run
```

The API will start on port **8085** by default.

## API Usage

### Process Payroll Endpoint

**POST** `/process`

Process a CSV file containing payroll data, generate PDF paystubs, and send them via email.

#### Request

- **Content-Type**: `multipart/form-data`
- **Parameters**:
  - `file` (required): CSV file with payroll data
  - `country` (optional, default: "do"): Country code ("do" or "USA")
  - `credentials` (required): Authentication credentials in format `username+password`
  - `company` (required): Company name

#### CSV File Format

```csv
full_name,email,position,health_discount_amount,social_discount_amount,taxes_discount_amount,other_discount_amount,gross_salary,gross_payment,net_payment,period
John Doe,john.doe@example.com,Software Engineer,150.00,250.00,450.00,50.00,6000.00,5500.00,4600.00,2024-01
Jane Smith,jane.smith@example.com,Senior Manager,200.00,350.00,750.00,100.00,8500.00,8000.00,6600.00,2024-01
```

**Required CSV Headers:**
- `full_name` - Employee full name
- `email` - Employee email address
- `position` - Job position/title
- `health_discount_amount` - Health insurance discount
- `social_discount_amount` - Social security discount
- `taxes_discount_amount` - Tax withholding
- `other_discount_amount` - Other deductions
- `gross_salary` - Gross salary amount
- `gross_payment` - Gross payment amount
- `net_payment` - Net payment (take-home)
- `period` - Payment period (e.g., "2024-01")

#### Example Request

Using cURL:

```bash
curl -X POST http://localhost:8085/process \
  -F "file=@payroll.csv" \
  -F "country=USA" \
  -F "credentials=myuser+mypassword" \
  -F "company=AcmeCorp"
```

Using HTTPie:

```bash
http --form POST http://localhost:8085/process \
  file@payroll.csv \
  country=USA \
  credentials=myuser+mypassword \
  company=AcmeCorp
```

#### Response

**Success (200 OK):**

```json
{
  "company": "AcmeCorp",
  "country": "US",
  "sent": [
    {
      "email": "john.doe@example.com",
      "full_name": "John Doe",
      "sent_at": "2024-01-15T10:30:45.123Z"
    },
    {
      "email": "jane.smith@example.com",
      "full_name": "Jane Smith",
      "sent_at": "2024-01-15T10:30:46.456Z"
    }
  ]
}
```

**Error (400 Bad Request):**

```json
{
  "error": "Invalid country option."
}
```

**Error (403 Forbidden):**

Invalid or missing credentials.

## Configuration

### Application Properties

Edit `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: "Paystubs API"

  mail:
    host: ${MAIL_HOST:localhost}
    port: ${MAIL_PORT:1025}
    username: ${MAIL_USER:no-reply@example.com}
    password: ${MAIL_PASSWORD:}
    properties:
      mail.smtp.auth: ${MAIL_AUTH:false}
      mail.smtp.starttls.enable: ${MAIL_STARTTLS:false}

server:
  port: 8085

app:
  auth:
    password: ${APP_PASSWORD:pwd}
    user: ${APP_USER:user}
```

### Supported Countries

- **`do`** - Dominican Republic (Spanish)
- **`USA`** - United States (English)

The country parameter determines the language used in email subjects and PDF labels.

## Running Tests

The project includes comprehensive test coverage with 97 tests covering unit, integration, and security testing.

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=CsvServiceTest
```

### Test Coverage

- **Unit Tests**: Service layer, utilities, and domain logic
- **Integration Tests**: Full request/response cycle with security
- **Security Tests**: Authentication and authorization

### Test Reports

Test reports are generated in `target/surefire-reports/`

## Technologies Used

- **Spring Boot 3.5.6** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Mail** - Email sending
- **Apache Commons CSV** - CSV parsing
- **OpenPDF** - PDF generation
- **Lombok** - Boilerplate code reduction
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions

## Development

### Building the Project

```bash
./mvnw clean install
```

### Running in Development Mode

```bash
./mvnw spring-boot:run
```

The application will run with Spring Boot DevTools for hot reload.

### Packaging for Production

```bash
./mvnw clean package -DskipTests
```

The executable JAR will be generated in `target/paystubs-api-0.0.1-SNAPSHOT.jar`

### Running the JAR

```bash
java -jar target/paystubs-api-0.0.1-SNAPSHOT.jar
```

## Security

The API uses a custom parameter-based authentication system:

- Credentials must be passed as a request parameter in format: `username+password`
- The credentials are validated against configured `app.auth.user` and `app.auth.password`
- All endpoints (except health check) require authentication
- Failed authentication returns HTTP 403 Forbidden

**Note**: This authentication mechanism is designed for internal use. For production environments with external access, consider implementing OAuth2, JWT, or other industry-standard authentication mechanisms.

## Troubleshooting

### Common Issues

**Issue**: Tests failing with email errors
- **Solution**: Ensure Mailpit is running or mock the EmailService in tests

**Issue**: Logo not appearing in PDF
- **Solution**: Check that the logo file exists in `logos/{company}.png` or `logos/default.png`

**Issue**: CSV parsing errors
- **Solution**: Verify CSV headers match the expected format exactly (case-insensitive but must be present)

**Issue**: Port 8085 already in use
- **Solution**: Change `server.port` in `application.yml` or set `SERVER_PORT` environment variable

**Issue**: Authentication failing
- **Solution**: Ensure credentials parameter format is correct: `username+password` (with plus sign, not colon)

## Email Testing

### Using Mailpit (Recommended for Development)

Mailpit is already configured in `compose.yaml`:

```bash
docker-compose up -d
```

- **SMTP Server**: localhost:1025
- **Web Interface**: http://localhost:8025

All emails sent by the application will be captured by Mailpit and viewable in the web interface.

### Using Real SMTP Server

Update environment variables to point to your SMTP server:

```bash
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USER=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export MAIL_AUTH=true
export MAIL_STARTTLS=true
```

## Disclaimers
* This is an example and intended to demonstrate to app providers a sample of how to approach an implementation. There are potentially other ways to approach it and alternatives could be considered.
* Its possible that the repo is not actively maintained.

## License
MIT

The code in this repository is covered by the included license.

## Support

For issues, questions, or contributions, please [create an issue](link-to-issues) or contact the development team.
