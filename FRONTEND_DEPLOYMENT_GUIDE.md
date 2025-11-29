# 🌐 **FarmChainX Frontend Deployment Guide (Netlify)**

Complete step-by-step guide to deploy your Angular frontend to Netlify.

---

## **📋 Prerequisites**

- ✅ GitHub account with FarmChainX repository
- ✅ Netlify account (sign up at [netlify.com](https://netlify.com))
- ✅ Backend API URL (deployed or local for testing)

---

## **🔧 Step 1: Configure Production Environment**

### **1.1 Update API URL**

Edit `frontend/src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'YOUR_BACKEND_URL_HERE',  // Replace with actual backend URL
  name: 'production'
};
```

**Examples of backend URLs:**
- **Render**: `https://farmchainx-backend.onrender.com`
- **Local Testing**: `http://localhost:8080`
- **Heroku**: `https://your-app.herokuapp.com`
- **Railway**: `https://your-app.up.railway.app`

### **1.2 Test Production Build Locally**

```bash
cd frontend
npm install
npm run build:prod
```

**Expected Output:**
```
Initial chunk files | Names     | Raw size
styles.css          | styles    | 156.45 kB
main.js            | main      | 109.65 kB (estimated transfer)
```

### **1.3 Test Production Build**

```bash
npm install -g http-server
http-server dist/farmchainx-frontend -p 4200 -o
```

---

## **🚀 Step 2: Deploy to Netlify**

### **2.1 Connect GitHub Repository**

1. **Go to Netlify**
   - Visit [netlify.com](https://netlify.com)
   - Click **"Log in"** or **"Sign up"**

2. **Import from Git**
   - Click **"New site from Git"**
   - Choose **"GitHub"**
   - Authorize Netlify to access your repositories

3. **Select Repository**
   - Find and select **"farmchainx"** repository
   - Click to proceed

### **2.2 Configure Build Settings**

**✅ Exact Settings to Use:**

```
Base directory: frontend
Build command: npm install && npx ng build --configuration production
Publish directory: dist/farmchainx-frontend
```

**⚡ Advanced Settings:**
- **Node.js Version**: 20.19.0 (automatically detected from .nvmrc)
- **Package directory**: (leave empty)
- **Functions directory**: (leave empty)

### **2.3 Environment Variables (Optional)**

Add these in **Site settings → Environment variables**:

```
NODE_ENV=production
ANGULAR_ENV=production
```

### **2.4 Deploy**

1. Click **"Deploy site"**
2. Wait for build to complete (2-5 minutes)
3. Get your site URL: `https://random-name-123456.netlify.app`

---

## **⚙️ Step 3: Configuration Files (Auto-Configured)**

Your project already includes these files:

### **3.1 netlify.toml** ✅
```toml
[build]
  command = "npm install && npx ng build --configuration production"
  publish = "dist/farmchainx-frontend"
  base = "frontend"

[build.environment]
  NODE_VERSION = "20.19.0"

# SPA Redirects for Angular
[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200

# Security Headers
[[headers]]
  for = "/*"
  [headers.values]
    Referrer-Policy = "strict-origin-when-cross-origin"
    X-Content-Type-Options = "nosniff"
    X-Frame-Options = "DENY"
    X-XSS-Protection = "1; mode=block"

# Cache Static Assets
[[headers]]
  for = "/assets/*"
  [headers.values]
    Cache-Control = "public, max-age=31536000, immutable"
```

### **3.2 .nvmrc** ✅
```
20.19.0
```

### **3.3 package.json Build Scripts** ✅
```json
{
  "scripts": {
    "build:prod": "ng build --configuration production"
  }
}
```

---

## **🧪 Step 4: Testing & Verification**

### **4.1 Test Frontend**

1. **Access Netlify URL**
   ```
   https://your-site-name.netlify.app
   ```

2. **Check Browser Console**
   - Press `F12` → Console tab
   - Look for any errors
   - Verify API calls are working

3. **Test Key Features**
   - ✅ Home page loads
   - ✅ Registration/Login forms
   - ✅ Navigation works
   - ✅ API calls to backend

### **4.2 Common Issues & Solutions**

**🔧 Issue: "ng: not found"**
- **Solution**: Build command should use `npx ng build`
- **Fix**: Update Netlify build command

**🔧 Issue: "404 on refresh"**
- **Solution**: SPA redirects needed
- **Fix**: `netlify.toml` already configured

**🔧 Issue: "CORS errors"**
- **Solution**: Backend CORS not configured
- **Fix**: Update backend CORS settings

**🔧 Issue: "Node version mismatch"**
- **Solution**: Wrong Node.js version
- **Fix**: `.nvmrc` file sets Node 20.19.0

---

## **🔄 Step 5: Updates & Redeployment**

### **5.1 Automatic Deployments**

Every push to `master` branch triggers automatic deployment:

```bash
# Make changes to your code
git add .
git commit -m "Update frontend"
git push origin master
# Netlify automatically rebuilds and deploys
```

### **5.2 Manual Deployments**

From Netlify dashboard:
1. Go to **"Deploys"** tab
2. Click **"Trigger deploy"** → **"Deploy site"**

### **5.3 Clear Cache & Rebuild**

If build issues persist:
1. **"Deploys"** tab → **"Trigger deploy"** 
2. Select **"Clear cache and deploy site"**

---

## **🎯 Step 6: Custom Domain (Optional)**

### **6.1 Add Custom Domain**

1. **Site Settings** → **"Domain management"**
2. Click **"Add custom domain"**
3. Enter your domain: `farmchainx.com`
4. Follow DNS configuration instructions

### **6.2 SSL Certificate**

- **Automatic**: Netlify provides free SSL
- **Custom**: Upload your own certificate

---

## **📊 Step 7: Performance & Monitoring**

### **7.1 Build Optimization**

Your Angular build is already optimized:
```json
// angular.json production config
"optimization": true,
"outputHashing": "all",
"sourceMap": false,
"budgets": [
  {
    "type": "initial",
    "maximumWarning": "500kB",
    "maximumError": "1MB"
  }
]
```

### **7.2 Analytics**

Enable in Netlify dashboard:
- **Site Settings** → **"Analytics"**
- Track page views and performance

---

## **🔗 Step 8: Connect with Backend**

### **8.1 Update Backend CORS**

Your backend must allow your Netlify domain:

```java
@CrossOrigin(origins = {
    "https://your-netlify-site.netlify.app",
    "http://localhost:4200"  // For development
})
```

### **8.2 Environment-Specific URLs**

```typescript
// environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://your-backend.onrender.com',  // Production backend
  name: 'production'
};

// environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',  // Development backend
  name: 'development'
};
```

---

## **🎉 Success! Your Frontend is Deployed**

### **📱 Final URLs**

- **Frontend**: `https://your-app.netlify.app`
- **Admin**: `https://your-app.netlify.app/admin`
- **GitHub**: `https://github.com/Rohan1o1/farmchainx`

### **✅ Deployment Checklist**

- [ ] Frontend builds successfully locally
- [ ] Environment variables configured
- [ ] Backend URL updated in environment.prod.ts
- [ ] Netlify build settings configured correctly
- [ ] Custom domain configured (optional)
- [ ] SSL certificate active
- [ ] CORS configured on backend
- [ ] End-to-end testing completed

---

## **🆘 Need Help?**

### **Build Logs**
- Netlify Dashboard → Site → Deploys → Click on deploy → View logs

### **Common Commands**
```bash
# Local development
npm start

# Production build
npm run build:prod

# Test production build
http-server dist/farmchainx-frontend -p 4200

# Check Angular version
ng version
```

### **Support Resources**
- [Netlify Documentation](https://docs.netlify.com)
- [Angular Deployment Guide](https://angular.io/guide/deployment)
- [GitHub Repository](https://github.com/Rohan1o1/farmchainx)

---

**🎊 Congratulations! Your FarmChainX frontend is now live on Netlify!**
