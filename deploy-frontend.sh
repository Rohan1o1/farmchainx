#!/bin/bash

# Frontend Deployment Script for Vercel
echo "🚀 Preparing FarmChainX Frontend for Vercel Deployment"

# Navigate to frontend directory
cd frontend

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Run production build
echo "🏗️ Building for production..."
npm run build:prod

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build successful! Ready for Vercel deployment."
    echo ""
    echo "Next steps:"
    echo "1. Push code to GitHub"
    echo "2. Connect repository to Vercel"
    echo "3. Set build command: npm run build:prod"
    echo "4. Set output directory: dist"
    echo "5. Deploy!"
else
    echo "❌ Build failed! Please check the errors above."
    exit 1
fi
