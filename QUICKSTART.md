# Quick Start - Spring Boot API

## 📋 Prerequisites
- Java 21
- Maven 3.6+
- Gateway running on port 7000

## 🚀 Quick Start (3 steps)

### 1️⃣ Start Gateway
```bash
cd backend
mvn exec:java -Dexec.mainClass="gateway.Gateway"
# Wait for: "Gateway running on port 7000"
```

### 2️⃣ Start API
```bash
# New terminal
./start-api.sh
# Or: cd backend && mvn spring-boot:run
# Wait for: "Tomcat started on port(s): 8080"
```

### 3️⃣ Test
```bash
curl http://localhost:8080/api/health
# Expected: {"status":"UP","gateway":"CONNECTED"}
```

## 🔗 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/health` | GET | Health check |
| `/api/search?q=query` | GET | Search |
| `/api/barrels/active` | GET | Active barrels |
| `/api/barrels/add-url` | POST | Add URL |

## 🧪 Quick Test Commands

```bash
# Health check
curl http://localhost:8080/api/health

# Add URL
curl -X POST http://localhost:8080/api/barrels/add-url \
  -H "Content-Type: application/json" \
  -d '{"url": "https://pt.wikipedia.org/wiki/Java"}'

# Search
curl "http://localhost:8080/api/search?q=java"
```

## 🔧 React Integration

```typescript
const API_URL = 'http://localhost:8080/api';

async function search(query: string) {
  const res = await fetch(`${API_URL}/search?q=${query}`);
  return await res.json();
}
```

## 🐛 Common Issues

**Port 8080 in use?**
```bash
lsof -ti:8080 | xargs kill -9
```

**Gateway not running?**
```bash
lsof -i :7000  # Check if Gateway is running
```

**CORS errors?**
- Check `webapp/config/CorsConfig.java` allows `http://localhost:3000`
- Restart Spring Boot after changes

## 📚 Full Documentation

- `SPRING_BOOT_SUMMARY.md` - Complete implementation summary
- `API_TESTING_GUIDE.md` - Detailed testing guide
- `README.md` - Project overview

## ✅ Success Checklist

- [ ] Gateway running (port 7000)
- [ ] API running (port 8080)
- [ ] Health check returns UP
- [ ] Search returns JSON
- [ ] React can call API without CORS errors

---

**Quick Help:**
- Kill API: `Ctrl+C` or `lsof -ti:8080 | xargs kill -9`
- View logs: Check terminal where `mvn spring-boot:run` is running
- Re-compile: `cd backend && mvn clean compile`
