# Railway Deployment Guide

## مراحل Deployment

### 1. ورود به Railway
- برو به [railway.app](https://railway.app)
- با GitHub وارد شو

### 2. ایجاد پروژه جدید
- کلیک "New Project"
- انتخاب "Deploy from GitHub repo"
- Repository: `Ahmadsi70/netwall`
- Root Directory: `local_backend`

### 3. تنظیم Environment Variables
در Railway Dashboard > Variables اضافه کن:

```
NODE_ENV=production
PORT=3000
OPENAI_API_KEY=your-openai-api-key-here
APP_SECRET=internetguard-pro-default
OPENAI_MODERATION_MODEL=text-moderation-latest
OPENAI_SUGGEST_MODEL=gpt-3.5-turbo
```

### 4. دریافت URL
Railway یک URL مثل `https://netwall-production.railway.app` می‌دهد

### 5. تست API
```bash
# Health Check
curl https://your-app-name.railway.app/health

# Test Suggestions
curl -X POST https://your-app-name.railway.app/api/suggest \
  -H "Content-Type: application/json" \
  -d '{"keyword": "test"}'
```

### 6. تغییر URL در Android
در فایل `RemoteConfig.kt`:
```kotlin
const val BASE_URL: String = "https://your-app-name.railway.app"
```
