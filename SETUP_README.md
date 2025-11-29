# FarmChainX Setup Instructions

## Project Overview
FarmChainX is a farm supply chain management application with Spring Boot backend and Angular frontend.

## Updated Configurations

### Backend Configuration

#### 1. MySQL Database
- **Host**: localhost:3306
- **Database**: farmchainx
- **Username**: root
- **Password**: Qwerty@123

#### 2. Cloudinary Configuration
- **Cloud Name**: dorboched
- **API Key**: 285348916953255
- **API Secret**: idvxx9y_brsTxuEdLeHYfZ_76UI

### Setup Steps

#### Prerequisites
- Java 21
- MySQL 8.0+
- Node.js 18+
- Angular CLI

#### Backend Setup
1. Navigate to backend directory:
   ```bash
   cd backend
   ```

2. Set up MySQL database:
   - Create database named `farmchainx`
   - Ensure MySQL is running on localhost:3306

3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```

#### Frontend Setup
1. Navigate to frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the Angular development server:
   ```bash
   ng serve
   ```

#### Access the Application
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080

### Configuration Files Modified

1. **backend/src/main/resources/application.properties**
   - Updated MySQL password
   - Added Cloudinary configuration

2. **backend/src/main/java/com/farmchainx/farmchainx/configuration/CloudinaryConfig.java**
   - Created new configuration class for Cloudinary

3. **backend/src/main/java/com/farmchainx/farmchainx/controller/ProductController.java**
   - Updated to use dependency injection for Cloudinary
   - Removed hardcoded Cloudinary credentials

### Important Notes

1. **Security**: All Cloudinary credentials are now configured in application.properties.

2. **Database**: Make sure MySQL is running and the `farmchainx` database exists before starting the application.

4. **File Uploads**: The application uses Cloudinary for image storage, so make sure your Cloudinary account is properly configured.

### Troubleshooting

1. **MySQL Connection Issues**: 
   - Verify MySQL is running
   - Check username/password in application.properties
   - Ensure database `farmchainx` exists

2. **Cloudinary Upload Issues**:
   - All credentials are configured
   - Check Cloudinary account quotas if uploads fail

3. **CORS Issues**:
   - Check CorsConfig.java if frontend can't connect to backend

### Environment Variables Template

All credentials are now configured in application.properties. No additional environment variables needed.
