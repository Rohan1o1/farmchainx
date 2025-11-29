# Security Configuration Guide for FarmChainX

## ⚠️ IMPORTANT: Before Pushing to GitHub

This project contains sensitive information that MUST NOT be committed to version control:

### Sensitive Files (Already in .gitignore):
- `backend/src/main/resources/application.properties` - Contains database passwords and API keys
- `.env` files - Environment variables
- `backend/uploads/` - User uploaded files
- `frontend/uploads/` - Frontend upload cache
- Database files (*.db, *.sqlite)

### Setup Instructions:

1. **Configure Database:**
   - Edit `backend/src/main/resources/application.properties`
   - Change the database password from `Qwerty@123` to your secure password
   - Update Cloudinary credentials with your own

2. **Use Environment Variables (Recommended):**
   ```bash
   # Copy templates
   cp .env.template .env
   cp backend/src/main/resources/application.properties.template backend/src/main/resources/application.properties
   ```

3. **Your application.properties should look like:**
   ```properties
   spring.datasource.password=${DB_PASSWORD:your_secure_password}
   cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME:your_cloud_name}
   cloudinary.api-key=${CLOUDINARY_API_KEY:your_api_key}
   cloudinary.api-secret=${CLOUDINARY_API_SECRET:your_api_secret}
   ```

### Git Status Check:
Before committing, always run:
```bash
git status
```
Ensure these files are NOT listed:
- application.properties
- .env files
- uploads/ directories

### Emergency: If Sensitive Data Was Committed:
1. Immediately change all passwords and API keys
2. Use `git filter-branch` to remove from history
3. Force push to overwrite remote history
4. Notify team members to re-clone the repository
