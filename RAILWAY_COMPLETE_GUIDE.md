#  Railway Deployment - راهنمای کامل

## مرحله 1: GitHub Repository

### 1.1 ایجاد Repository
1. برو به [github.com](https://github.com)
2. کلیک "New repository"
3. نام: internetguard-pro-backend
4. Description: Premium AI Backend for InternetGuard Pro
5. Public یا Private
6. کلیک "Create repository"

### 1.2 Push کد
`ash
# در Terminal اجرا کن:
git remote add origin https://github.com/YOUR_USERNAME/internetguard-pro-backend.git
git branch -M main
git push -u origin main
`

## مرحله 2: Railway Deployment

### 2.1 Sign up
1. برو به [railway.app](https://railway.app)
2. کلیک "Login"  "GitHub"
3. Authorize Railway

### 2.2 Deploy Project
1. کلیک "New Project"
2. انتخاب "Deploy from GitHub repo"
3. Repository internetguard-pro-backend را انتخاب کن
4. کلیک "Deploy"

### 2.3 تنظیمات Project
- Railway خودکار local_backend/ folder را detect می‌کند
- Root Directory: local_backend
- Build Command: 
pm install
- Start Command: 
pm start

### 2.4 Environment Variables
در Railway Dashboard  Variables:
`
NODE_ENV=production
PORT=3000
`

## مرحله 3: دریافت URL

### 3.1 URL نهایی
Railway یک URL مثل https://your-app-name.railway.app می‌دهد

### 3.2 تست API
`ash
# Health Check
curl https://your-app-name.railway.app/health

# Test Moderation
curl -X POST https://your-app-name.railway.app/api/moderate \
  -H "Content-Type: application/json" \
  -d '{"input": "Hello world"}'
`

## مرحله 4: تغییر URL در برنامه Android

### 4.1 جایگزینی URL ها
`ash
# در KeywordAccessibilityService.kt
http://localhost:3000/api/moderate  https://your-app-name.railway.app/api/moderate

# در KeywordListFragment.kt  
http://localhost:3000/api/suggest  https://your-app-name.railway.app/api/suggest
`

### 4.2 Build مجدد
`ash
./gradlew assembleFullDebug
`

## مرحله 5: Google Play Store

### 5.1 آماده‌سازی
- برنامه را با URL های Railway build کن
- تست کامل انجام بده
- APK را sign کن

### 5.2 انتشار
- Google Play Console
- Create new app
- Upload APK
- Submit for review

##  نتیجه نهایی

 سرور ابری: https://your-app-name.railway.app  
 HTTPS: فعال  
 Premium System: کار می‌کند  
 Rate Limiting: 5 free requests  
 Google Play: آماده  

**درآمد پیش‌بینی شده: ,098/ماه**
