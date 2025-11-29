@echo off
echo Starting FarmChainX Backend Server...
cd /d "c:\Users\rohan\OneDrive\Desktop\infosys\FarmChainX\backend"
start "Backend Server" cmd /c "mvnw.cmd spring-boot:run -Dmaven.test.skip=true & pause"

echo Waiting for backend to start...
timeout /t 5 /nobreak

echo Starting FarmChainX Frontend Server...
cd /d "c:\Users\rohan\OneDrive\Desktop\infosys\FarmChainX\frontend"
start "Frontend Server" cmd /c "npm install && ng serve & pause"

echo Both servers are starting...
echo Backend: http://localhost:8080
echo Frontend: http://localhost:4200
pause
