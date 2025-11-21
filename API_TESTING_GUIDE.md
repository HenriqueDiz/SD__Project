# Spring Boot REST API - Testing Guide

## Overview

This guide explains how to test the Spring Boot REST API that connects your React frontend to the distributed search engine backend.

## Architecture

```
React Frontend (localhost:3000)
    ↓ HTTP/JSON
Spring Boot API (localhost:8080) 
    ↓ RMI
Gateway (localhost:7000)
    ↓ RMI
Barrels + Downloaders + URLQueue
```

---

## Step 1: Start the Backend Components

You need to start these components **in order**:

### Terminal 1 - Gateway
```bash
cd backend
mvn exec:java -Dexec.mainClass="gateway.Gateway"
```
Wait until you see: `Gateway running on port 7000`

### Terminal 2 - URLQueue
```bash
cd backend
mvn exec:java -Dexec.mainClass="queue.URLQueue"
```

### Terminal 3 - Barrel
```bash
cd backend
mvn exec:java -Dexec.mainClass="barrel.Barrel"
```

### Terminal 4 - Downloader (optional, for indexing)
```bash
cd backend
mvn exec:java -Dexec.mainClass="downloader.Downloader"
```

---

## Step 2: Start the Spring Boot API

### Terminal 5 - Spring Boot
```bash
cd backend
mvn spring-boot:run
```

You should see:
```
  ____                   _   ____                      _     
 / ___| ___   ___   __ _| | / ___|  ___  __ _ _ __ ___| |__  
| |  _ / _ \ / _ \ / _` | | \___ \ / _ \/ _` | '__/ __| '_ \ 
| |_| | (_) | (_) | (_| | |  ___) |  __/ (_| | | | (__| | | |
 \____|\___/ \___/ \__, |_| |____/ \___|\__,_|_|  \___|_| |_|
                   |___/                                      
                                                              
 Distributed Search Engine REST API
 Connecting to Gateway via RMI...

Tomcat started on port(s): 8080 (http)
```

---

## Step 3: Test the API Endpoints

### 3.1 Health Check

```bash
curl http://localhost:8080/api/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "gateway": "CONNECTED",
  "timestamp": "2024-11-21T15:30:00"
}
```

### 3.2 System Info

```bash
curl http://localhost:8080/api/info
```

**Expected Response:**
```json
{
  "appName": "Googol Search Engine API",
  "version": "1.0.0",
  "gatewayHost": "localhost",
  "gatewayPort": 7000
}
```

### 3.3 Add URL for Indexing

```bash
curl -X POST http://localhost:8080/api/barrels/add-url \
  -H "Content-Type: application/json" \
  -d '{"url": "https://pt.wikipedia.org/wiki/Java"}'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "URL added successfully",
  "url": "https://pt.wikipedia.org/wiki/Java"
}
```

### 3.4 Search (GET)

```bash
curl "http://localhost:8080/api/search?q=java&page=0"
```

**Expected Response:**
```json
{
  "query": "java",
  "results": [
    {
      "url": "https://pt.wikipedia.org/wiki/Java",
      "title": "Java (linguagem de programação)",
      "snippet": "Java é uma linguagem de programação orientada a objetos...",
      "references": 15
    }
  ],
  "totalResults": 1,
  "page": 0,
  "timestamp": "2024-11-21T15:35:00"
}
```

### 3.5 Search (POST - Multi-word)

```bash
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{"query": "java programação", "page": 0}'
```

### 3.6 Get Active Barrels

```bash
curl http://localhost:8080/api/barrels/active
```

**Expected Response:**
```json
[
  {
    "name": "barrel1",
    "host": "localhost",
    "port": 8186,
    "indexSize": 1520
  }
]
```

### 3.7 Get Registered Barrels

```bash
curl http://localhost:8080/api/barrels/registered
```

---

## Step 4: Test with React Frontend

### 4.1 Update Frontend Search Component

Make sure your React app calls the Spring Boot API:

```typescript
// In your search component
const API_URL = 'http://localhost:8080/api';

async function searchQuery(query: string, page: number = 0) {
  const response = await fetch(`${API_URL}/search?q=${encodeURIComponent(query)}&page=${page}`);
  const data = await response.json();
  return data;
}
```

### 4.2 Start React Dev Server

```bash
cd frontend
npm install
npm run dev
```

Visit: `http://localhost:3000`

---

## Step 5: Verify CORS is Working

Open browser DevTools (F12) → Network tab → Try a search

You should see:
```
Request URL: http://localhost:8080/api/search?q=java
Request Method: GET
Status Code: 200 OK
Access-Control-Allow-Origin: http://localhost:3000
```

If you see CORS errors, check `backend/src/main/java/webapp/config/CorsConfig.java`

---

## Common Issues

### Issue 1: "Cannot connect to Gateway"

**Symptom:** API returns 500 error
```json
{
  "error": "Gateway connection failed"
}
```

**Solution:**
1. Make sure Gateway is running: `lsof -i :7000` (macOS/Linux)
2. Check `backend/src/main/resources/application.properties`:
   ```properties
   rmi.gateway.host=localhost
   rmi.gateway.port=7000
   ```

### Issue 2: "Port 8080 already in use"

**Solution:**
```bash
# Find process using port 8080
lsof -ti:8080 | xargs kill -9

# Or change port in application.properties
server.port=8081
```

### Issue 3: Empty search results

**Solution:**
1. Add URLs first: `curl -X POST http://localhost:8080/api/barrels/add-url ...`
2. Wait for Downloader to index (check Gateway logs)
3. Try search again

### Issue 4: CORS errors in browser

**Symptom:** 
```
Access to fetch at 'http://localhost:8080/api/search' from origin 'http://localhost:3000' 
has been blocked by CORS policy
```

**Solution:**
1. Check `CorsConfig.java` allows `http://localhost:3000`
2. Restart Spring Boot after changes
3. Clear browser cache

---

## API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |
| GET | `/api/info` | System information |
| GET/POST | `/api/search` | Search query |
| POST | `/api/barrels/add-url` | Add URL to index |
| GET | `/api/barrels/active` | List active barrels |
| GET | `/api/barrels/registered` | List registered barrels |

---

## Performance Testing

### Load Test with curl

```bash
# Test 100 searches
for i in {1..100}; do
  curl -s "http://localhost:8080/api/search?q=java" > /dev/null
  echo "Request $i completed"
done
```

### Monitor with Spring Boot Actuator

```bash
# Check application metrics
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

---

## Next Steps

1. ✅ **pom.xml updated** with Spring Boot dependencies
2. ✅ **All API files created** (controllers, services, config)
3. ✅ **CORS configured** for React frontend
4. ⏭️ **Test the API** using this guide
5. ⏭️ **Update React frontend** to use API endpoints
6. ⏭️ **Deploy** (optional)

---

## Files Created in Previous Session

```
backend/src/main/java/webapp/
├── WebApplication.java              # Main Spring Boot class
├── config/
│   ├── RMIConfig.java              # RMI Gateway connection
│   └── CorsConfig.java             # CORS configuration
├── dto/
│   ├── SearchRequestDTO.java       # Search request
│   ├── SearchResponseDTO.java      # Search response
│   └── SearchResultDTO.java        # Individual result
├── service/
│   └── GatewayServiceClient.java   # RMI wrapper service
└── controller/
    ├── SearchController.java       # /api/search
    ├── BarrelController.java       # /api/barrels
    └── HealthController.java       # /api/health

backend/src/main/resources/
├── application.properties           # Spring Boot config
└── banner.txt                       # Startup banner
```

---

## Support

If you encounter issues:
1. Check Gateway logs for RMI errors
2. Check Spring Boot console for exceptions
3. Use `curl -v` for verbose HTTP debugging
4. Check browser DevTools Network tab for CORS issues

**Happy Testing!**
