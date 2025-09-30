# 🔧 راهنمای تنظیم Proxy برای توسعه‌دهنده

## 📋 خلاصه

برنامه **فقط از Proxy Server شما** برای ارتباط با OpenAI API استفاده می‌کند. کاربران **نیازی به کلید API ندارند** و همه چیز توسط شما مدیریت می‌شود.

---

## 🎯 معماری

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────┐
│   کاربر     │────────▶│  Proxy Server    │────────▶│  OpenAI API │
│  (Android)  │         │ (Vercel/Your)    │         │   (GPT-4)   │
└─────────────┘         └──────────────────┘         └─────────────┘
                             ↑
                       کلید API شما
```

### مزایا این روش:

✅ **کاربر:** هیچ تنظیمی لازم نیست
✅ **شما:** کنترل کامل روی هزینه و استفاده
✅ **امنیت:** کلید API هرگز در اپلیکیشن کاربر نیست
✅ **مدیریت:** Rate limiting و monitoring متمرکز

---

## 🚀 نصب Proxy

### گزینه 1: Vercel (توصیه شده)

#### 1. نصب Vercel CLI

```bash
npm install -g vercel
```

#### 2. Login به Vercel

```bash
vercel login
```

#### 3. تنظیم API Key

```bash
cd proxy
vercel env add OPENAI_API_KEY
```

وقتی پرسید، کلید OpenAI خود را وارد کنید و برای همه محیط‌ها (production, preview, development) انتخاب کنید.

#### 4. Deploy

```bash
vercel --prod
```

#### 5. دریافت URL

بعد از deploy، Vercel یک URL می‌دهد مثل:
```
https://internetguard-proxy.vercel.app
```

شما دو endpoint خواهید داشت:
```
https://your-project.vercel.app/api/moderate
https://your-project.vercel.app/api/suggest
```

---

### گزینه 2: Self-Hosted (Node.js)

#### 1. نصب Dependencies

```bash
cd proxy
npm install
```

#### 2. ایجاد .env

```env
PORT=8787
OPENAI_API_KEY=sk-your-actual-key-here
OPENAI_MODERATE_MODEL=text-moderation-latest
OPENAI_SUGGEST_MODEL=gpt-3.5-turbo
```

#### 3. اجرا

```bash
npm start
```

یا با PM2 برای production:

```bash
pm2 start src/index.js --name internetguard-proxy
pm2 save
pm2 startup
```

---

## 🔧 تنظیم برنامه Android

بعد از deploy proxy، باید URL آن را در کد برنامه قرار دهید.

### فایل: `KeywordAccessibilityService.kt`

```kotlin
// خط 95
val proxyUrl = "https://YOUR-PROXY-URL.vercel.app/api/moderate"
moderationClient = RemoteModerationClient(endpoint = proxyUrl, timeoutMs = 2500)
```

### فایل: `KeywordListFragment.kt`

```kotlin
// خط 180
val suggestUrl = "https://YOUR-PROXY-URL.vercel.app/api/suggest"
val client = RemoteModerationClient(endpoint = suggestUrl, timeoutMs = 2500)
```

---

## 📊 مدیریت هزینه

### تخمین هزینه

```
Moderation API:  $0.002 per 1,000 requests
GPT-3.5-turbo:   $0.0015 per 1K tokens (input)

مثال با 10,000 کاربر:
- هر کاربر 100 request در ماه
- کل: 1,000,000 request
- هزینه Moderation: $2
- هزینه Suggestions: ~$5
- مجموع: ~$7/ماه
```

### تنظیم محدودیت در OpenAI

1. برو به: https://platform.openai.com/account/billing/limits
2. تنظیم کن:
   ```
   Monthly budget: $20
   Email alerts: Enabled at 80%
   ```

---

## 🔒 امنیت Proxy

### تنظیمات امنیتی (اختیاری اما توصیه شده)

#### 1. اضافه کردن Rate Limiting

در `api/moderate.js`:

```javascript
// Add at the top
const rateLimit = {};
const MAX_REQUESTS_PER_IP = 100; // per minute
const WINDOW = 60000; // 1 minute

module.exports = async function handler(req, res) {
  // Rate limiting
  const ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress;
  const now = Date.now();
  
  if (!rateLimit[ip]) {
    rateLimit[ip] = { count: 0, resetAt: now + WINDOW };
  }
  
  if (now > rateLimit[ip].resetAt) {
    rateLimit[ip] = { count: 0, resetAt: now + WINDOW };
  }
  
  if (rateLimit[ip].count >= MAX_REQUESTS_PER_IP) {
    return res.status(429).json({ error: 'Too many requests' });
  }
  
  rateLimit[ip].count++;
  
  // ... rest of code
}
```

#### 2. اضافه کردن API Token (اختیاری)

```javascript
// Add secret token check
const API_TOKEN = process.env.API_TOKEN;

if (req.headers['authorization'] !== `Bearer ${API_TOKEN}`) {
  return res.status(401).json({ error: 'Unauthorized' });
}
```

و در برنامه Android:

```kotlin
.addHeader("Authorization", "Bearer YOUR_SECRET_TOKEN")
```

---

## 📈 Monitoring

### Vercel Dashboard

برو به: https://vercel.com/dashboard
- مشاهده تعداد requests
- زمان پاسخ
- خطاها

### OpenAI Dashboard

برو به: https://platform.openai.com/usage
- مشاهده هزینه روزانه
- تعداد tokens استفاده شده
- API calls

---

## 🧪 تست Proxy

### تست Moderation Endpoint

```bash
curl -X POST https://your-proxy.vercel.app/api/moderate \
  -H "Content-Type: application/json" \
  -d '{"text": "This is a test message"}'
```

پاسخ باید مثل این باشد:

```json
{
  "flagged": false,
  "categories": {
    "sexual": false,
    "hate": false,
    "harassment": false,
    "self-harm": false,
    "sexual/minors": false,
    "hate/threatening": false,
    "violence/graphic": false,
    "self-harm/intent": false,
    "self-harm/instructions": false,
    "harassment/threatening": false,
    "violence": false
  },
  "category_scores": { ... }
}
```

### تست Suggestions Endpoint

```bash
curl -X POST https://your-proxy.vercel.app/api/suggest \
  -H "Content-Type: application/json" \
  -d '{"keyword": "violence", "language": "en", "category": "harmful"}'
```

پاسخ:

```json
{
  "synonyms": ["aggression", "assault", "brutality"],
  "variants": ["violent", "violently"],
  "obfuscations": ["v!olence", "vi0lence"],
  "regex": ["viol.*", "v[i1]ol.*"],
  "categories": ["violence", "harm"],
  "notes": "Physical harm or aggression"
}
```

---

## 🐛 عیب‌یابی

### مشکل: Proxy پاسخ نمی‌دهد

**راه‌حل:**
```bash
# بررسی logs در Vercel
vercel logs

# یا اگر self-hosted:
pm2 logs internetguard-proxy
```

### مشکل: خطای "API key not configured"

**راه‌حل:**
```bash
# بررسی environment variables
vercel env ls

# یا add کردن دوباره
vercel env add OPENAI_API_KEY
```

### مشکل: خطای 429 (Rate limit)

**راه‌حل:**
- محدودیت OpenAI را افزایش دهید
- Rate limiting در proxy را adjust کنید
- Caching اضافه کنید

---

## 📝 Endpoints

### POST /api/moderate

**Request:**
```json
{
  "text": "متن برای بررسی"
}
```

**Response:**
```json
{
  "flagged": boolean,
  "categories": object,
  "category_scores": object
}
```

### POST /api/suggest

**Request:**
```json
{
  "keyword": "کلمه کلیدی",
  "language": "fa",
  "category": "harmful"
}
```

**Response:**
```json
{
  "synonyms": string[],
  "variants": string[],
  "obfuscations": string[],
  "regex": string[],
  "categories": string[],
  "notes": string
}
```

---

## 🔄 به‌روزرسانی

### به‌روزرسانی Proxy

```bash
cd proxy

# Pull changes
git pull

# Deploy
vercel --prod
```

### به‌روزرسانی system_prompt.json

```bash
cd proxy

# Edit system_prompt.json
nano system_prompt.json

# Deploy
vercel --prod
```

---

## 💡 نکات بهینه‌سازی

### 1. استفاده از Caching

```javascript
// Simple in-memory cache
const cache = new Map();
const CACHE_TTL = 60000; // 1 minute

if (cache.has(text)) {
  const cached = cache.get(text);
  if (Date.now() - cached.timestamp < CACHE_TTL) {
    return res.json(cached.result);
  }
}
```

### 2. استفاده از مدل‌های ارزان‌تر

برای suggestions می‌توانید از `gpt-3.5-turbo` استفاده کنید که خیلی ارزان‌تر از GPT-4 است.

### 3. Text Preprocessing

قبل از ارسال به OpenAI، متن را پردازش کنید:
- حذف فضاهای اضافی
- کوتاه کردن متن‌های بلند
- حذف تکرارها

---

## 📞 پشتیبانی

### مشکل دارید؟

1. بررسی logs
2. تست endpoints با curl
3. بررسی OpenAI dashboard
4. بررسی Vercel dashboard

### منابع مفید

- [Vercel Docs](https://vercel.com/docs)
- [OpenAI API Docs](https://platform.openai.com/docs)
- [Node.js Best Practices](https://github.com/goldbergyoni/nodebestpractices)

---

*آخرین به‌روزرسانی: 30 سپتامبر 2025*
*نسخه: 2.0.0 (Proxy-Only)* 