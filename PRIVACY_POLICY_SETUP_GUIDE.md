# راهنمای کامل تنظیم Privacy Policy برای Google Play Console

## 🎯 **مرحله 1: آپلود Privacy Policy به GitHub**

### 1.1 ایجاد Repository جدید (اختیاری)
```bash
# اگر می‌خواهید repository جداگانه برای Privacy Policy ایجاد کنید:
git init privacy-policy
cd privacy-policy
git add .
git commit -m "Add Privacy Policy for InternetGuard Pro"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/internetguard-pro-privacy.git
git push -u origin main
```

### 1.2 یا اضافه کردن به Repository موجود
```bash
# اگر می‌خواهید به repository موجود اضافه کنید:
git add privacy-policy.html
git add privacy-policy-fa.html
git commit -m "Add Privacy Policy files"
git push origin main
```

## 🌐 **مرحله 2: فعال‌سازی GitHub Pages**

### 2.1 تنظیم GitHub Pages
1. برو به repository در GitHub
2. کلیک روی **Settings**
3. اسکرول کن تا **Pages** را پیدا کنی
4. در **Source** انتخاب کن: **Deploy from a branch**
5. در **Branch** انتخاب کن: **main**
6. در **Folder** انتخاب کن: **/ (root)**
7. کلیک **Save**

### 2.2 دریافت URL
- GitHub Pages URL شما خواهد بود: `https://YOUR_USERNAME.github.io/REPOSITORY_NAME/`
- برای مثال: `https://ahmadsi70.github.io/internetguard-pro-privacy/`

## 📱 **مرحله 3: آپلود در Google Play Console**

### 3.1 ورود به Google Play Console
1. برو به [Google Play Console](https://play.google.com/console)
2. اپلیکیشن **InternetGuard Pro** را انتخاب کن
3. از منوی سمت راست **Policy and programs** را انتخاب کن
4. **Privacy Policy** را کلیک کن

### 3.2 وارد کردن URL
```
Privacy Policy URL: https://YOUR_USERNAME.github.io/REPOSITORY_NAME/privacy-policy.html
```

### 3.3 برای نسخه فارسی (اختیاری)
```
Privacy Policy URL (Persian): https://YOUR_USERNAME.github.io/REPOSITORY_NAME/privacy-policy-fa.html
```

## ✅ **مرحله 4: تأیید و تست**

### 4.1 تست URL
- URL را در مرورگر باز کن
- مطمئن شو که Privacy Policy به درستی نمایش داده می‌شود
- تست کن که در موبایل هم درست کار می‌کند

### 4.2 تأیید در Google Play Console
- دکمه **Save** را کلیک کن
- وضعیت **Privacy Policy** باید **Complete** شود

## 🔧 **مرحله 5: تنظیمات پیشرفته (اختیاری)**

### 5.1 Custom Domain (اختیاری)
اگر دامنه شخصی داری:
```bash
# فایل CNAME ایجاد کن
echo "privacy.yourdomain.com" > CNAME
git add CNAME
git commit -m "Add custom domain"
git push origin main
```

### 5.2 SSL Certificate
- GitHub Pages خودکار SSL ارائه می‌دهد
- URL باید با **https://** شروع شود

## 📋 **چک‌لیست نهایی**

### ✅ موارد ضروری:
- [ ] Privacy Policy به GitHub آپلود شده
- [ ] GitHub Pages فعال شده
- [ ] URL در Google Play Console وارد شده
- [ ] Privacy Policy در مرورگر قابل دسترسی است
- [ ] محتوا کامل و دقیق است
- [ ] تاریخ آخرین به‌روزرسانی درست است

### ✅ موارد اختیاری:
- [ ] نسخه فارسی اضافه شده
- [ ] Custom domain تنظیم شده
- [ ] SSL فعال است
- [ ] Mobile-friendly design

## 🚨 **نکات مهم**

### ⚠️ هشدارها:
1. **URL باید همیشه در دسترس باشد** - اگر GitHub Pages غیرفعال شود، Google Play Console خطا می‌دهد
2. **محتوای Privacy Policy باید دقیق باشد** - Google ممکن است محتوا را بررسی کند
3. **تاریخ آخرین به‌روزرسانی** باید همیشه به‌روز باشد
4. **زبان** باید با مخاطبان هدف مطابقت داشته باشد

### 💡 نکات مفید:
1. **Backup** - همیشه یک کپی از Privacy Policy در جای امن نگه دار
2. **Version Control** - تغییرات را در Git track کن
3. **Testing** - قبل از آپلود، در مرورگرهای مختلف تست کن
4. **Mobile Test** - در موبایل هم تست کن

## 📞 **پشتیبانی**

اگر مشکلی داری:
1. GitHub Pages documentation را چک کن
2. Google Play Console Help را مطالعه کن
3. از طریق app support با ما تماس بگیر

---

**🎉 تبریک! Privacy Policy شما آماده است و می‌توانید آن را در Google Play Console آپلود کنید.**
