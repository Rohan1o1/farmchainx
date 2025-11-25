# FarmChainX — Blockchain-Powered Agricultural Traceability

Live Demo: http://localhost:4200  
Backend: http://localhost:8080

## Sample Accounts (Ready to Login)

| Role         | Email                          | Password | Notes |
|--------------|--------------------------------|----------|-------|
| Farmer       | farmer@farmchainx.com          | 1234     | Can upload |
| Distributor  | distributor@farmchainx.com     | 1234     | Can receive & handover |
| Retailer     | retailer@farmchainx.com        | 1234     | Can confirm receipt |
| Consumer     | consumer@farmchainx.com        | 1234     | Can scan & review |
| Admin        | admin@farmchainx.com           | 1234     | Full access |

All accounts pre-registered in DB via `data.sql`

## Sample Products Already Uploaded (by farmer)

- Basmati Rice  
- Red Onion  
- Alphonso Mango  
- Organic Tomato  

→ Just scan any QR from `/verify/...` links below

## Quick Demo Flow (Copy-Paste Links)

1. **Farmer uploads** → already done  
2. **Generate QR** → Done (on My Products page)  
3. **Verify QR** → Click any:  
   http://localhost:4200/verify/123e4567-e89b-12d3-a456-426614174000  
   http://localhost:4200/verify/123e4567-e89b-12d3-a456-426614174001  
4. **Distributor logs in** → Confirm receipt → Final handover to retailer  
5. **Retailer logs in** → Confirm receipt  
6. **Consumer scans** → Leaves 5-star review  
7. **Admin** → Sees everything in Admin Panel

## How to Run

```bash
# Backend (Spring Boot)
cd backend
./mvnw spring-boot:run

# Frontend (Angular)
cd frontend
npm install
ng serve --proxy-config proxy.conf.json 
