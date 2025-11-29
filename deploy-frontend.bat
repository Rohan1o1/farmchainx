@echo off

REM Frontend Deployment Script for Vercel (Windows)
echo 🚀 Preparing FarmChainX Frontend for Vercel Deployment

REM Navigate to frontend directory
cd frontend

REM Install dependencies
echo 📦 Installing dependencies...
npm install

REM Run production build
echo 🏗️ Building for production...
npm run build:prod

REM Check if build was successful
if %errorlevel% == 0 (
    echo ✅ Build successful! Ready for Vercel deployment.
    echo.
    echo Next steps:
    echo 1. Push code to GitHub
    echo 2. Connect repository to Vercel
    echo 3. Set build command: npm run build:prod
    echo 4. Set output directory: dist
    echo 5. Deploy!
) else (
    echo ❌ Build failed! Please check the errors above.
    exit /b 1
)
