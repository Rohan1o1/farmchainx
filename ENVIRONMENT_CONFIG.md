# 🌍 Environment Configuration Guide

## Local Development Environment Variables

### Backend (.env)
```env
# Database Configuration
DB_USERNAME=root
DB_PASSWORD=your_local_mysql_password

# Cloudinary Configuration  
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret

# Frontend URL
FRONTEND_URL=http://localhost:4200
```

## Production Environment Variables

### Vercel (Frontend)
```env
NODE_ENV=production
```

### Render (Backend)
```env
# Spring Configuration
SPRING_PROFILES_ACTIVE=production
JAVA_TOOL_OPTIONS=-Xmx512m

# Database (Auto-populated by Render PostgreSQL)
DATABASE_URL=postgresql://username:password@hostname:port/database
DB_USERNAME=your_db_username  
DB_PASSWORD=your_db_password

# Cloudinary (Same as local)
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret

# CORS Configuration
FRONTEND_URL=https://your-vercel-app.vercel.app
```

## 🔑 How to Get Required Credentials

### 1. Cloudinary Setup
1. Go to [cloudinary.com](https://cloudinary.com)
2. Sign up for free account
3. Dashboard → Account Details
4. Copy:
   - Cloud Name
   - API Key  
   - API Secret

### 2. Database Setup (Render)
1. Render Dashboard → New PostgreSQL
2. Database Name: `farmchainx-db`
3. After creation, copy connection details
4. Auto-populate DATABASE_URL, DB_USERNAME, DB_PASSWORD

### 3. Domain Configuration
1. After Vercel deployment, copy your app URL
2. Update FRONTEND_URL in Render backend
3. Update apiUrl in Angular environment files

## 🚀 Quick Setup Commands

### Frontend Environment Setup
```bash
cd frontend
cp src/environments/environment.ts src/environments/environment.prod.ts
# Edit environment.prod.ts with production API URL
```

### Backend Environment Setup  
```bash
cd backend
cp src/main/resources/application.properties.template src/main/resources/application-production.properties
# Edit with production database and Cloudinary settings
```

## 📝 Environment Variable Checklist

### Before Deployment
- [ ] Cloudinary account created and credentials obtained
- [ ] Local development environment working
- [ ] Production environment files created
- [ ] All sensitive data in environment variables
- [ ] No hardcoded credentials in source code

### After Deployment
- [ ] Vercel environment variables set
- [ ] Render environment variables set  
- [ ] Database connection working
- [ ] CORS configured properly
- [ ] Frontend can connect to backend
- [ ] Image upload working (Cloudinary)
- [ ] All features tested in production
