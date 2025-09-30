#  Railway Deployment Guide

## مرحله 1: ایجاد Repository در GitHub

### 1.1 برو به GitHub.com
- Sign in به حساب GitHub خود
- کلیک روی "New repository"
- نام: internetguard-pro-backend
- Description: Premium AI Backend for InternetGuard Pro
- Public یا Private (انتخاب خودتان)
- کلیک "Create repository"

### 1.2 Push کد به GitHub
`ash
# اضافه کردن remote origin
git remote add origin https://github.com/YOUR_USERNAME/internetguard-pro-backend.git

# Push کد
git branch -M main
git push -u origin main
`

## مرحله 2: Railway Deployment

### 2.1 Sign up در Railway
- برو به [railway.app](https://railway.app)
- کلیک "Login"  "GitHub"
- Authorize Railway

### 2.2 ایجاد Project جدید
- کلیک "New Project"
- انتخاب "Deploy from GitHub repo"
- Repository internetguard-pro-backend را انتخاب کن
- کلیک "Deploy"

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

### 2.5 دریافت URL
- Railway یک URL مثل https://your-app-name.railway.app می‌دهد
- این URL را کپی کن

## مرحله 3: تغییر URL در برنامه Android

### 3.1 تغییر URL ها
`ash
# در KeywordAccessibilityService.kt
http://localhost:3000/api/moderate  https://your-app-name.railway.app/api/moderate

# در KeywordListFragment.kt  
http://localhost:3000/api/suggest  https://your-app-name.railway.app/api/suggest

# در ApiKeyTester.kt
http://localhost:3000/api/moderate  https://your-app-name.railway.app/api/moderate
http://localhost:3000/api/suggest  https://your-app-name.railway.app/api/suggest
`

### 3.2 Build مجدد برنامه
`ash
./gradlew assembleFullDebug
`

## مرحله 4: تست

### 4.1 تست API
`ash
# Health Check
curl https://your-app-name.railway.app/health

# Test Moderation
curl -X POST https://your-app-name.railway.app/api/moderate \
  -H "Content-Type: application/json" \
  -d '{"input": "Hello world"}'

# Test Suggestions
curl -X POST https://your-app-name.railway.app/api/suggest \
  -H "Content-Type: application/json" \
  -d '{"keyword": "violence"}'
`

### 4.2 تست برنامه Android
- APK جدید را نصب کن
- AI features را تست کن
- Rate limiting را تست کن

## مرحله 5: Google Play Store

### 5.1 آماده‌سازی برای انتشار
- برنامه را با URL های Railway build کن
- تست کامل انجام بده
- APK را sign کن

### 5.2 آپلود به Google Play
- Google Play Console
- Create new app
- Upload APK
- Fill store listing
- Submit for review

##  نتیجه

 سرور ابری آماده  
 HTTPS فعال  
 Rate limiting  
 Premium subscription system  
 آماده برای Google Play Store  

**URL نهایی:** https://your-app-name.railway.app
"@ | Out-File -FilePath "RAILWAY_DEPLOYMENT_GUIDE.md" -Encoding UTF8
qی
@"
# دستورات برای GitHub Repository

## 1. برو به GitHub.com
- Sign in به حساب GitHub خود
- کلیک "New repository"
- نام: internetguard-pro-backend
- Description: Premium AI Backend for InternetGuard Pro
- Public یا Private (انتخاب خودتان)
- کلیک "Create repository"

## 2. دستورات Terminal
بعد از ایجاد repository، این دستورات را اجرا کن:

git remote add origin https://github.com/YOUR_USERNAME/internetguard-pro-backend.git
git branch -M main
git push -u origin main

## 3. جایگزین کردن YOUR_USERNAME
YOUR_USERNAME را با نام کاربری GitHub خودت جایگزین کن
