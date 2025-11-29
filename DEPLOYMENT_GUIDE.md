# 🚀 FarmChainX Deployment Guide

Complete deployment guide for FarmChainX with Frontend on Netlify and Backend on Render.

## 📋 Prerequisites

- GitHub account
- Netlify account (free tier available)
- Render account (free tier available)  
- Cloudinary account (for image storage)

## 🌐 Frontend Deployment (Netlify)

### Step 1: Prepare Frontend for Production

1. **Update Angular Configuration**
   ```bash
   # In frontend directory
   cd frontend
   ```

2. **Build for Production**
   ```bash
   npm run build:prod
   ```

3. **Test Production Build Locally**
   ```bash
   npm install -g http-server
   http-server dist/farmchainx-frontend -p 4200
   ```

### Step 2: Deploy to Netlify

1. **Connect to Netlify**
   - Go to [netlify.com](https://netlify.com)
   - Sign up/Sign in with GitHub
   - Click "New site from Git"
   - Choose GitHub and authorize Netlify
   - Select your `farmchainx` repository

2. **Configure Build Settings**
   - **Base directory**: `frontend`
   - **Build command**: `npm install && npx ng build --configuration production`
   - **Publish directory**: `dist/farmchainx-frontend`
   - **Environment**: Node.js 18.x

3. **Advanced Build Settings**
   Netlify will automatically use `netlify.toml` for configuration including:
   - SPA redirects for Angular routing
   - Security headers
   - Caching rules
   - Environment variables

4. **Environment Variables**
   Add these in Netlify Site Settings → Environment Variables:
   ```
   NODE_ENV=production
   ANGULAR_ENV=production
   ```

5. **Deploy**
   - Click "Deploy site"
   - Wait for build to complete
   - Note your Netlify URL (e.g., `https://amazing-site-name.netlify.app`)
   - Optionally set up custom domain in Site Settings

## 🖥️ Backend Deployment (Render)

### Step 1: Prepare Backend for Production

1. **Update Application Properties**
   - Ensure `application-production.properties` is configured
   - PostgreSQL dependency is added to `pom.xml`

2. **Test Build Locally**
   ```bash
   cd backend
   ./mvnw clean package -DskipTests
   ```

### Step 2: Deploy to Render

1. **Connect to Render**
   - Go to [render.com](https://render.com)
   - Sign up/Sign in with GitHub
   - Click "New +" → "Web Service"
   - Connect your `farmchainx` repository

2. **Configure Web Service**
   ```
   Name: farmchainx-backend
   Environment: Java
   Region: Choose closest to your users
   Branch: master
   Root Directory: backend
   ```

3. **Build & Deploy Settings**
   ```bash
   Build Command: ./mvnw clean package -DskipTests
   Start Command: java -Dserver.port=$PORT -Dspring.profiles.active=production -jar target/farmchainx-1.0.0.jar
   ```

4. **Environment Variables**
   Add these in Render dashboard:
   ```
   SPRING_PROFILES_ACTIVE=production
   JAVA_TOOL_OPTIONS=-Xmx512m
   CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
   CLOUDINARY_API_KEY=your_cloudinary_api_key  
   CLOUDINARY_API_SECRET=your_cloudinary_api_secret
   FRONTEND_URL=https://your-netlify-site.netlify.app
   ```

### Step 3: Setup Database

1. **Create PostgreSQL Database**
   - In Render dashboard: "New +" → "PostgreSQL"
   - Database Name: `farmchainx-db`
   - User: `farmchainx_user`
   - Region: Same as your web service

2. **Connect Database to Web Service**
   - Go to your web service settings
   - Add environment variables:
   ```
   DATABASE_URL=[Copy from PostgreSQL dashboard]
   DB_USERNAME=[Copy from PostgreSQL dashboard]
   DB_PASSWORD=[Copy from PostgreSQL dashboard]
   ```

3. **Deploy**
   - Click "Deploy Latest Commit"
   - Wait for build and deployment
   - Note your Render URL (e.g., `https://farmchainx-backend.onrender.com`)

## 🔗 Connect Frontend and Backend

### Step 1: Update Frontend Environment

1. **Update Production Environment**
   Edit `frontend/src/environments/environment.prod.ts`:
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://your-render-backend.onrender.com',
     name: 'production'
   };
   ```

2. **Update Angular Services**
   Make sure all HTTP calls use `environment.apiUrl`:
   ```typescript
   // In your services
   import { environment } from '../environments/environment';
   
   private apiUrl = environment.apiUrl;
   ```

### Step 2: Update Backend CORS

1. **Update Production Properties**
   In `application-production.properties`:
   ```properties
   frontend.url=https://your-netlify-site.netlify.app
   ```

2. **Redeploy Both Services**
   - Trigger new build on Netlify (push to GitHub)
   - Redeploy Render service

## 🛡️ Security Configuration

### Step 1: Domain Security

1. **HTTPS Only**
   - Both Vercel and Render provide HTTPS automatically
   - Update any HTTP references to HTTPS

2. **Environment Variables**
   - Never commit sensitive data
   - Use environment variables for all secrets

### Step 2: CORS Configuration

Update your Spring Boot CORS configuration:
```java
@CrossOrigin(origins = {"https://your-netlify-site.netlify.app"})
```

## 🧪 Testing Deployment

### Step 1: Test Frontend

1. **Access Netlify URL**
   ```
   https://your-netlify-site.netlify.app
   ```

2. **Check Console**
   - Open browser developer tools
   - Check for any errors
   - Verify API calls are working

### Step 2: Test Backend

1. **Access Render API**
   ```
   https://your-render-backend.onrender.com/api/health
   ```

2. **Test Database Connection**
   - Try user registration
   - Test product upload
   - Verify QR code generation

### Step 3: End-to-End Testing

1. **Complete User Journey**
   - Register new account
   - Login with credentials
   - Upload product with image
   - Generate QR code
   - Verify product via QR code

## 🔧 Troubleshooting

### Common Issues

1. **Build Failures**
   ```bash
   # Check logs in Render/Netlify dashboard
   # Verify dependencies in package.json/pom.xml
   ```

2. **Database Connection Issues**
   ```bash
   # Verify DATABASE_URL is correct
   # Check database is running
   # Verify credentials
   ```

3. **CORS Errors**
   ```bash
   # Update CORS configuration
   # Verify frontend URL in backend
   # Check HTTPS vs HTTP
   ```

4. **Environment Variables**
   ```bash
   # Verify all required env vars are set
   # Check for typos in variable names
   # Ensure proper values
   ```

## 📊 Performance Optimization

### Frontend (Netlify)

1. **Build Optimization**
   ```json
   // angular.json - production configuration
   "optimization": true,
   "outputHashing": "all",
   "sourceMap": false,
   "namedChunks": false,
   "aot": true,
   "extractLicenses": true,
   "vendorChunk": false,
   "buildOptimizer": true
   ```

2. **Lazy Loading**
   - Implement lazy loading for routes
   - Split bundles for better performance
   - Netlify provides automatic CDN optimization

### Backend (Render)

1. **JVM Optimization**
   ```bash
   JAVA_TOOL_OPTIONS=-Xmx512m -Xms256m -XX:MaxMetaspaceSize=128m
   ```

2. **Database Connection Pooling**
   ```properties
   spring.datasource.hikari.maximum-pool-size=5
   spring.datasource.hikari.minimum-idle=1
   ```

## 🎯 Production URLs

After successful deployment:

- **Frontend**: `https://your-app-name.netlify.app`
- **Backend**: `https://your-service-name.onrender.com`
- **Database**: Managed by Render PostgreSQL

## 📈 Monitoring

1. **Netlify Analytics**
   - Enable analytics in Netlify dashboard
   - Monitor page load times and visitor data

2. **Render Metrics**
   - Monitor CPU and memory usage
   - Check response times

3. **Application Logs**
   - Check Render logs for errors
   - Monitor database performance

## 💰 Cost Estimation

### Free Tier Limits

**Netlify (Free)**
- 300 build minutes per month
- 100GB bandwidth per month
- Unlimited personal sites
- Custom domains
- Forms (100 submissions/month)

**Render (Free)**
- 750 hours per month
- 512MB RAM
- Sleeps after 15 minutes of inactivity

### Paid Plans
- **Netlify Pro**: $19/month per member
- **Render Starter**: $7/month per service

---

## ✅ Deployment Checklist

- [ ] Frontend builds successfully locally
- [ ] Backend builds and runs locally
- [ ] Environment variables configured
- [ ] Database credentials set up
- [ ] CORS configuration updated
- [ ] Cloudinary credentials added
- [ ] Both services deployed
- [ ] End-to-end testing completed
- [ ] Custom domains configured (optional)
- [ ] Monitoring set up

Your FarmChainX application is now live and production-ready! 🎉
