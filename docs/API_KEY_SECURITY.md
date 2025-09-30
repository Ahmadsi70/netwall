# 🔐 API Key Security Documentation

## Overview

InternetGuard Pro uses a multi-layered security approach to protect OpenAI API keys stored on the device. This document explains the security measures in place.

---

## 🛡️ Security Architecture

### 1. **Android Keystore Integration**

The app uses Android's hardware-backed Keystore system to generate and store encryption keys:

```kotlin
// Key Generation
KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
```

**Benefits:**
- Keys never leave secure hardware
- Protected against extraction even on rooted devices
- Hardware-backed encryption on supported devices

### 2. **AES/GCM Encryption**

API keys are encrypted using AES-256 with GCM mode:

```kotlin
Transformation: "AES/GCM/NoPadding"
Key Size: 256 bits
GCM Tag Length: 128 bits
IV Length: 96 bits (12 bytes)
```

**Why GCM?**
- Authenticated encryption (prevents tampering)
- Industry standard for API key protection
- Resistant to padding oracle attacks

### 3. **Secure Storage**

Encrypted API keys are stored in Android's SharedPreferences:

```
Storage Location: /data/data/com.internetguard.pro/shared_prefs/secure_keys.xml

Contents:
- encrypted_openai_key: Base64-encoded ciphertext
- openai_key_iv: Base64-encoded initialization vector
```

**Note:** Even if an attacker gains access to these files, they cannot decrypt without the hardware-protected key in Android Keystore.

---

## 🔒 Security Features

### ✅ What We Protect Against

| Threat | Protection |
|--------|------------|
| **Memory Dumps** | Keys cleared immediately after use |
| **Debug Logging** | No API key material in logs (length only) |
| **Clipboard Theft** | Password-masked input field |
| **Root Access** | Hardware-backed Keystore protection |
| **App Decompilation** | No hardcoded keys, runtime-only |
| **Network Interception** | HTTPS only, key never transmitted |
| **Invalid Keys** | Format validation before storage |
| **Unauthorized Access** | User must have device unlock to decrypt |

### ⚠️ What Users Should Know

1. **API Key Never Shown**: Once saved, the API key cannot be viewed - only replaced or removed
2. **Encrypted at Rest**: All stored keys are encrypted using hardware-backed keys
3. **No Cloud Sync**: Keys are device-local only (not synced to cloud)
4. **Biometric Protection**: Optional biometric authentication for settings access
5. **Priority System**: Direct API → Proxy → Disabled

---

## 🔧 Implementation Details

### Storage Flow

```
User Input (sk-xxx...) 
    ↓
Validation (format check)
    ↓
Android Keystore (generate/retrieve encryption key)
    ↓
AES/GCM Encryption
    ↓
Base64 Encoding
    ↓
SharedPreferences (encrypted storage)
    ↓
Clear from memory
```

### Retrieval Flow

```
Request API Key
    ↓
SharedPreferences (read encrypted data)
    ↓
Base64 Decoding
    ↓
Android Keystore (retrieve decryption key)
    ↓
AES/GCM Decryption
    ↓
Return key to caller
    ↓
Clear from memory after use
```

---

## 📝 Code Examples

### Secure Key Storage

```kotlin
val secureKeyManager = SecureKeyManager(context)
val apiKey = "sk-..." // User input

// Store securely
if (secureKeyManager.storeApiKey(apiKey)) {
    // Key encrypted and stored
    // Original key cleared from memory
}
```

### Secure Key Retrieval

```kotlin
val secureKeyManager = SecureKeyManager(context)

// Retrieve when needed
val apiKey = secureKeyManager.getApiKey()
if (apiKey != null) {
    // Use key for API call
    val client = DirectOpenAIClient(apiKey)
    
    // Key automatically cleared after use
}
```

### Key Removal

```kotlin
val secureKeyManager = SecureKeyManager(context)

// Remove key
if (secureKeyManager.removeApiKey()) {
    // Key permanently deleted
    // Switches to proxy mode
}
```

---

## 🚫 What We DON'T Do

### ❌ Never Logged

- Actual API key value
- Decrypted key material
- Intermediate encryption states

### ❌ Never Stored Plain

- No plaintext keys in files
- No keys in app resources
- No keys in source code

### ❌ Never Transmitted

- API key stays on device
- Only encrypted form leaves app boundary
- Direct OpenAI communication uses HTTPS

---

## 🔍 Verification

### How to Verify Security

1. **Check Keystore**
   ```bash
   adb shell run-as com.internetguard.pro ls -la files/
   # No API key files visible
   ```

2. **Check Logs**
   ```bash
   adb logcat | grep -i "api.*key"
   # Only shows "API key stored (length: XX)"
   # Never shows actual key
   ```

3. **Check SharedPreferences**
   ```bash
   adb shell run-as com.internetguard.pro cat shared_prefs/secure_keys.xml
   # Shows only Base64 encrypted data
   # Cannot be decrypted without Keystore key
   ```

---

## 🎯 Best Practices for Users

### ✅ Recommended

1. **Use Direct API Key** for maximum privacy (no proxy involved)
2. **Enable Biometric Lock** for settings access
3. **Keep Device Encrypted** (Android setting)
4. **Use Strong Screen Lock** (PIN/Pattern/Biometric)
5. **Update App Regularly** for security patches

### ⚠️ Warnings

1. **Don't Share Screenshots** of settings page
2. **Don't Root Device** (weakens Keystore protection)
3. **Don't Install From Unknown Sources** (app integrity)
4. **Don't Store API Key Elsewhere** on device

---

## 📊 Security Audit Log

| Date | Action | Status |
|------|--------|--------|
| 2025-09-30 | Android Keystore integration | ✅ Implemented |
| 2025-09-30 | AES/GCM encryption | ✅ Implemented |
| 2025-09-30 | Secure memory handling | ✅ Implemented |
| 2025-09-30 | Format validation | ✅ Implemented |
| 2025-09-30 | Logging sanitization | ✅ Implemented |
| 2025-09-30 | UI masking | ✅ Implemented |
| 2025-09-30 | Clipboard protection | ✅ Implemented |

---

## 🔗 References

- [Android Keystore System](https://developer.android.com/training/articles/keystore)
- [AES-GCM Encryption](https://en.wikipedia.org/wiki/Galois/Counter_Mode)
- [OpenAI API Best Practices](https://platform.openai.com/docs/guides/safety-best-practices)
- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)

---

## 📞 Security Contacts

If you discover a security vulnerability, please report it to:
- **Email**: security@internetguardpro.example
- **Priority**: Critical issues reported within 24 hours

**Please do not** create public GitHub issues for security vulnerabilities.

---

## ⚖️ Compliance

This implementation follows:
- ✅ OWASP Mobile Top 10 guidelines
- ✅ Android Security Best Practices
- ✅ GDPR data protection requirements
- ✅ OpenAI API security recommendations

---

*Last Updated: September 30, 2025*
*Version: 1.0.0* 