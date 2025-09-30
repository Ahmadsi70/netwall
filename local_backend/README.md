# Local AI Backend Server

##  Quick Start

### 1. Install Dependencies
`ash
cd local_backend
npm install
`

### 2. Start Server
`ash
npm start
`

### 3. Test Endpoints
- **Health Check:** http://localhost:3000/health
- **Moderation:** http://localhost:3000/api/moderate
- **Suggestions:** http://localhost:3000/api/suggest

##  API Endpoints

### POST /api/moderate
`json
{
  "input": "Hello world test"
}
`

**Response:**
`json
{
  "inappropriate": false,
  "confidence": 0.1,
  "category": null,
  "language": "en"
}
`

### POST /api/suggest
`json
{
  "keyword": "violence",
  "language": "English",
  "category": "General"
}
`

**Response:**
`json
{
  "synonyms": ["aggression", "harm", "hurt"],
  "variants": ["violent", "violently"],
  "obfuscations": ["v1olence", "v*olence"],
  "regex": ["v[0-9]olence"],
  "categories": ["violence", "harmful"],
  "notes": "Content related to violence"
}
`

##  Features

-  **No API Key Required** - Uses mock responses
-  **Keyword-based Detection** - Simple but effective
-  **CORS Enabled** - Works with mobile apps
-  **JSON Responses** - Compatible with existing app
-  **Health Check** - Monitor server status

##  Development

`ash
# Install dependencies
npm install

# Start with auto-reload
npm run dev

# Start production
npm start
`

##  App Configuration

Update your Android app to use:
`
http://localhost:3000/api/moderate
http://localhost:3000/api/suggest
`

##  Security Note

This is a **local development server**. For production, use proper authentication and security measures.

## 🎯 **آماده برای Railway Deployment!**

### **✅ کارهای انجام شده:**

1. **📁 Git Repository:** آماده و commit شده
2. **📄 فایل‌های لازم:** README, .env.example, .gitignore
3. **🚀 کد سرور:** با سیستم اشتراک Premium
4. **📱 برنامه Android:** کامپایل شده

### **🚀 مراحل بعدی:**

#### **مرحله 1: GitHub Repository**
```bash
<code_block_to_apply_changes_from>
```

#### **مرحله 2: Railway Deployment**
1. **برو به [railway.app](https://railway.app)**
2. **Sign up با GitHub**
3. **New Project → Deploy from GitHub repo**
4. **Repository را انتخاب کن**
5. **Root Directory: `local_backend`**
6. **Deploy!**

#### **مرحله 3: دریافت URL**
- Railway یک URL مثل `https://your-app-name.railway.app` می‌دهد
- این URL را کپی کن

#### **مرحله 4: تغییر URL در برنامه**
```bash
# جایگزین کن:
http://localhost:3000 → https://your-app-name.railway.app
```

### **🎉 نتیجه:**
- **✅ سرور ابری:** آماده
- **✅ HTTPS:** فعال
- **✅ Premium System:** کار می‌کند
- **✅ Google Play:** آماده

**آیا می‌خواهید ادامه دهیم؟** 🚀
