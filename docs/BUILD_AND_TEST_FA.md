# 🔨 راهنمای Build و Test

## 📋 پیش‌نیازها

قبل از build، مطمئن شوید:
- ✅ Android Studio نصب است
- ✅ Android SDK نصب است
- ✅ JDK 17+ نصب است
- ✅ Gradle wrapper موجود است

---

## 🚀 مرحله 1: Clean Build

### Windows PowerShell:
```powershell
cd "C:\Users\badri\InternetGuard Pro"
.\gradlew clean
```

### Linux/Mac:
```bash
./gradlew clean
```

**زمان تقریبی:** 30 ثانیه

---

## 🔨 مرحله 2: Build Debug APK

### Windows PowerShell:
```powershell
.\gradlew assembleFullDebug
```

### Linux/Mac:
```bash
./gradlew assembleFullDebug
```

**زمان تقریبی:** 2-5 دقیقه (اولین بار بیشتر)

### خروجی موفق:
```
BUILD SUCCESSFUL in 3m 45s
```

### مسیر APK:
```
app\build\outputs\apk\full\debug\app-full-debug.apk
```

---

## 📦 مرحله 3: بررسی APK

### چک کردن وجود APK:
```powershell
dir app\build\outputs\apk\full\debug\*.apk
```

### خروجی:
```
-a----  9/30/2025  app-full-debug.apk  (~ 15-25 MB)
```

---

## 📱 مرحله 4: نصب روی دستگاه

### پیش‌نیاز:
1. **USB Debugging** را فعال کنید:
   ```
   Settings → About Phone → 7 بار روی Build Number کلیک
   Settings → Developer Options → USB Debugging ✅
   ```

2. **دستگاه را وصل کنید** و تأیید کنید:
   ```powershell
   adb devices
   ```
   
   خروجی باید شامل دستگاه شما باشد:
   ```
   List of devices attached
   ABC123456789    device
   ```

### نصب APK:
```powershell
adb install -r app\build\outputs\apk\full\debug\app-full-debug.apk
```

**فلگ‌ها:**
- `-r`: Replace existing app (نگه داشتن data)
- بدون `-r`: Fresh install (پاک کردن data)

### خروجی موفق:
```
Performing Streamed Install
Success
```

---

## 🧪 مرحله 5: Test Features

### Test 1: Optimistic UI ✅
```
1. باز کردن App List
2. Toggle کردن یک Switch
3. بررسی:
   ✅ فوراً تغییر می‌کند (< 100ms)
   ✅ Loading می‌چرخد
   ✅ لرزش احساس می‌شود
   ✅ بعد از 400ms، Animation موفقیت
```

### Test 2: Quick Actions ✅
```
1. کلیک روی Menu (⋮)
2. انتخاب "اقدامات سریع"
3. انتخاب "شبکه‌های اجتماعی"
4. بررسی لیست اپ‌ها
5. تأیید
6. بررسی:
   ✅ همه social media apps مسدود می‌شوند
   ✅ در < 3s تمام می‌شود
   ✅ Toast موفقیت نمایش داده می‌شود
```

### Test 3: Batch Selection ✅
```
1. کلیک روی Menu (⋮)
2. انتخاب "انتخاب چندتایی"
3. UI تغییر می‌کند:
   ✅ Checkbox‌ها ظاهر می‌شوند
   ✅ Switch‌ها مخفی می‌شوند
   ✅ Selection Toolbar ظاهر می‌شود
4. انتخاب 5-10 اپ
5. بررسی:
   ✅ تعداد به‌روز می‌شود: "5 اپ انتخاب شده"
   ✅ دکمه Block فعال است
6. کلیک روی "Block"
7. بررسی:
   ✅ همه اپ‌ها مسدود می‌شوند
   ✅ در < 2s تمام می‌شود
   ✅ Selection Mode بسته می‌شود
```

### Test 4: VPN Batch Updates ✅
```
1. Toggle کردن 10 اپ پشت سر هم (سریع)
2. بررسی Logcat:
   ✅ "Queued update for..." (10 بار)
   ✅ "Processing 10 batched updates"
   ✅ "Batch updates applied in XXXms"
3. بررسی زمان:
   ✅ باید < 2s باشد
```

### Test 5: Performance ⚡
```
1. باز کردن App List با 50+ اپ
2. Scroll کردن لیست
3. بررسی:
   ✅ روان است (60 FPS)
   ✅ بدون lag
   ✅ بدون stuttering
4. Toggle کردن چند اپ
5. بررسی:
   ✅ فوری پاسخ می‌دهد
   ✅ CPU usage کم است
```

---

## 🔍 Debug با Logcat

### دیدن Logs:
```powershell
adb logcat -s NetworkGuardVpnService VpnUpdateManager AppListFragment AppListAdapter
```

### Logs مهم:

#### VPN Batch Updates:
```
VpnUpdateManager: Queued update for com.instagram.android
VpnUpdateManager: Queued update for com.facebook.katana
...
VpnUpdateManager: Processing 5 batched updates
NetworkGuardVpnService: Applying 5 batched updates
NetworkGuardVpnService: VPN reconfigured with batch updates
VpnUpdateManager: Batch updates applied in 850ms
```

#### Quick Actions:
```
AppListFragment: Quick Actions dialog shown
AppListFragment: Category SOCIAL_MEDIA selected (5 apps)
AppListFragment: Blocking category apps...
AppListFragment: ✅ 5 apps blocked successfully
```

#### Selection Mode:
```
AppListAdapter: Selection mode enabled
AppListAdapter: Item selected: com.instagram.android
AppListAdapter: Selection count: 1
...
AppListFragment: Blocking 5 selected apps
AppListFragment: ✅ 5 apps blocked
```

---

## 🐛 عیب‌یابی

### مشکل 1: Build Failed
```
خطا: "Execution failed for task ':app:compileFullDebugKotlin'"

راه‌حل:
1. Invalidate Caches:
   Android Studio → File → Invalidate Caches / Restart

2. Clean و Rebuild:
   .\gradlew clean
   .\gradlew assembleFullDebug

3. بررسی Kotlin version در build.gradle
```

### مشکل 2: APK نصب نمی‌شود
```
خطا: "INSTALL_FAILED_UPDATE_INCOMPATIBLE"

راه‌حل:
1. حذف نسخه قبلی:
   adb uninstall com.internetguard.pro

2. نصب دوباره:
   adb install app\build\outputs\apk\full\debug\app-full-debug.apk
```

### مشکل 3: VPN Permission
```
مشکل: VPN کار نمی‌کند

راه‌حل:
1. Settings → Apps → Internet Guard Pro → Permissions
2. اجازه VPN را بدهید
3. اپ را Restart کنید
```

### مشکل 4: کندی عملکرد
```
مشکل: UI کند است

بررسی:
1. Logcat را چک کنید
2. CPU Profiler را باز کنید
3. Memory Profiler را چک کنید

احتمالاً:
- لیست اپ‌ها خیلی بزرگ است
- Database queries در Main Thread
- بیش از حد logging
```

---

## 📊 Benchmarking

### ابزارها:
```powershell
# CPU Usage
adb shell top -m 10

# Memory Usage
adb shell dumpsys meminfo com.internetguard.pro

# Battery Stats
adb shell dumpsys batterystats com.internetguard.pro
```

### نتایج انتظاری:

| Metric | Target | Status |
|--------|--------|--------|
| **App Size** | < 25 MB | ✅ |
| **RAM Usage** | < 100 MB | ✅ |
| **CPU (idle)** | < 5% | ✅ |
| **CPU (active)** | < 15% | ✅ |
| **Battery/hour** | < 2% | ✅ |
| **Toggle Speed** | < 100ms | ✅ |
| **Batch Speed** | < 2s | ✅ |

---

## 🎯 Performance Tests

### Test Script:
```kotlin
// در Android Studio Terminal
adb shell am instrument -w -e class \
  com.internetguard.pro.PerformanceTest \
  com.internetguard.pro.test/androidx.test.runner.AndroidJUnitRunner
```

### Manual Performance Test:
```
1. Block 1 app → measure time → < 100ms ✅
2. Block 10 apps (one by one) → < 2s total ✅
3. Block 10 apps (batch select) → < 2s ✅
4. Block category (5 apps) → < 3s ✅
5. Unblock all → < 2s ✅
```

---

## 📱 Test Devices

### پیشنهادی:
```
✅ Android 10 (API 29) - حداقل
✅ Android 12 (API 31) - توصیه شده
✅ Android 13 (API 33) - جدید
✅ Android 14 (API 34) - آخرین
```

### تست روی:
- ✅ گوشی کم‌حافظه (2-3 GB RAM)
- ✅ گوشی متوسط (4-6 GB RAM)
- ✅ گوشی قدرتمند (8+ GB RAM)

---

## ✅ Checklist نهایی

قبل از Release:
- [ ] همه tests پاس می‌شوند
- [ ] Performance هدف را برآورده می‌کند
- [ ] UI روان و responsive است
- [ ] بدون memory leak
- [ ] بدون crash
- [ ] همه features کار می‌کنند:
  - [ ] Optimistic UI
  - [ ] Batch Updates
  - [ ] Quick Actions
  - [ ] Selection Mode
  - [ ] VPN Blocking
- [ ] مستندات کامل است
- [ ] Logs تمیز هستند
- [ ] Error handling صحیح است

---

## 🚀 Build Release

### برای تولید نسخه Release:

```powershell
# 1. تنظیم keystore
# در app/build.gradle:
signingConfigs {
    release {
        storeFile file("your-keystore.jks")
        storePassword "your-password"
        keyAlias "your-alias"
        keyPassword "your-password"
    }
}

# 2. Build Release
.\gradlew bundleFullRelease

# 3. خروجی AAB:
app\build\outputs\bundle\fullRelease\app-full-release.aab
```

---

## 📦 خلاصه دستورات

```powershell
# Clean
.\gradlew clean

# Build Debug
.\gradlew assembleFullDebug

# Build Release
.\gradlew bundleFullRelease

# Install
adb install -r app\build\outputs\apk\full\debug\app-full-debug.apk

# Uninstall
adb uninstall com.internetguard.pro

# Logs
adb logcat -s NetworkGuardVpnService VpnUpdateManager

# Devices
adb devices
```

---

**موفق باشید! 🎉**

*آخرین به‌روزرسانی: 30 سپتامبر 2025*
*نسخه: 2.0.0* 