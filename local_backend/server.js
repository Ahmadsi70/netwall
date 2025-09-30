// در server.js - سیستم اشتراک با قیمت‌های جدید

const express = require('express');
const cors = require('cors');
const app = express();
const PORT = 3000;

// Store user data (در production از database استفاده کنید)
const userData = new Map();

// Middleware
app.use(cors());
app.use(express.json());

// Subscription types with new pricing
const SUBSCRIPTION_TYPES = {
    FREE: { 
        requests: 5, 
        name: 'Free',
        price: 0,
        description: '5 free requests to try our AI features'
    },
    MONTHLY: { 
        requests: 1000, 
        name: 'Monthly Premium',
        price: 7.99,
        description: '1000 AI requests per month'
    },
    YEARLY: { 
        requests: 12000, 
        name: 'Yearly Premium',
        price: 77.99,
        description: '12000 AI requests per year (Save .89!)'
    }
};

// Check subscription middleware
const checkSubscription = (req, res, next) => {
    const userIP = req.ip || req.connection.remoteAddress;
    
    if (!userData.has(userIP)) {
        userData.set(userIP, {
            subscription: 'FREE',
            usedRequests: 0,
            subscriptionExpiry: null
        });
    }
    
    const user = userData.get(userIP);
    const subscription = SUBSCRIPTION_TYPES[user.subscription];
    
    // بررسی انقضای اشتراک
    if (user.subscriptionExpiry && new Date() > user.subscriptionExpiry) {
        user.subscription = 'FREE';
        user.usedRequests = 0;
        user.subscriptionExpiry = null;
    }
    
    // بررسی محدودیت درخواست
    if (user.usedRequests >= subscription.requests) {
        return res.status(429).json({
            error: 'Request limit exceeded',
            message: 'You have used all your requests. Please upgrade your subscription.',
            subscription: user.subscription,
            used: user.usedRequests,
            limit: subscription.requests,
            remaining: 0,
            upgradeRequired: true,
            subscriptionOptions: {
                monthly: { 
                    price: 7.99, 
                    requests: 1000,
                    name: 'Monthly Premium',
                    description: '1000 AI requests per month'
                },
                yearly: { 
                    price: 77.99, 
                    requests: 12000,
                    name: 'Yearly Premium',
                    description: '12000 AI requests per year (Save .89!)'
                }
            }
        });
    }
    
    // افزایش تعداد درخواست‌ها
    user.usedRequests++;
    
    // اضافه کردن اطلاعات subscription به response
    req.subscriptionInfo = {
        type: user.subscription,
        used: user.usedRequests,
        limit: subscription.requests,
        remaining: subscription.requests - user.usedRequests,
        upgradeRequired: false
    };
    
    next();
};

// API endpoints
app.post('/api/moderate', checkSubscription, (req, res) => {
    console.log(' Moderation request:', req.body);
    
    const { input } = req.body;
    
    // Simple keyword-based detection
    const harmfulKeywords = ['violence', 'hate', 'harm', 'danger', 'kill', 'death'];
    const isInappropriate = harmfulKeywords.some(keyword => 
        input.toLowerCase().includes(keyword)
    );
    
    const response = {
        inappropriate: isInappropriate,
        confidence: isInappropriate ? 0.85 : 0.1,
        category: isInappropriate ? 'violence' : null,
        language: 'en'
    };
    
    console.log(' Moderation response:', response);
    
    // اضافه کردن اطلاعات subscription به response
    res.json({
        ...response,
        subscription: req.subscriptionInfo
    });
});

app.post('/api/suggest', checkSubscription, (req, res) => {
    console.log(' Suggestions request:', req.body);
    
    const { keyword } = req.body;
    
    // Generate suggestions based on keyword
    let suggestions = {
        synonyms: [],
        variants: [],
        obfuscations: [],
        regex: [],
        categories: [],
        notes: ""
    };
    
    if (keyword.toLowerCase().includes('violence')) {
        suggestions = {
            synonyms: ["aggression", "harm", "hurt", "damage"],
            variants: ["violent", "violently", "aggressive"],
            obfuscations: ["v1olence", "v*olence", "v10l3nc3", "v!olence"],
            regex: ["v[0-9]olence", "v[*]olence", "v[!]olence"],
            categories: ["violence", "harmful"],
            notes: "Content related to violence and aggression"
        };
    } else if (keyword.toLowerCase().includes('game')) {
        suggestions = {
            synonyms: ["gaming", "play", "entertainment", "fun"],
            variants: ["games", "gamer", "gaming"],
            obfuscations: ["g4me", "g*me", "g@me", "gam3"],
            regex: ["g[0-9]me", "g[*]me", "g[@]me"],
            categories: ["gaming", "entertainment"],
            notes: "Gaming and entertainment related content"
        };
    } else {
        // Default suggestions
        suggestions = {
            synonyms: [keyword + " related", keyword + " content"],
            variants: [keyword + "s", keyword + "ing"],
            obfuscations: [keyword.replace(/[aeiou]/g, '*')],
            regex: [keyword.replace(/[aeiou]/g, '[aeiou]')],
            categories: ["general"],
            notes: "General content filtering"
        };
    }
    
    console.log(' Suggestions response:', suggestions);
    
    // اضافه کردن اطلاعات subscription به response
    res.json({
        ...suggestions,
        subscription: req.subscriptionInfo
    });
});

// Subscription management endpoints
app.post('/api/subscribe', (req, res) => {
    const { subscriptionType, paymentToken } = req.body;
    const userIP = req.ip || req.connection.remoteAddress;
    
    if (!userData.has(userIP)) {
        userData.set(userIP, {
            subscription: 'FREE',
            usedRequests: 0,
            subscriptionExpiry: null
        });
    }
    
    const user = userData.get(userIP);
    
    // در اینجا باید payment verification انجام دهید
    // برای demo، فقط subscription را تغییر می‌دهیم
    
    if (subscriptionType === 'MONTHLY') {
        user.subscription = 'MONTHLY';
        user.subscriptionExpiry = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);
        user.usedRequests = 0;
    } else if (subscriptionType === 'YEARLY') {
        user.subscription = 'YEARLY';
        user.subscriptionExpiry = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000);
        user.usedRequests = 0;
    }
    
    res.json({
        success: true,
        message: 'Subscription activated successfully',
        subscription: user.subscription,
        expiry: user.subscriptionExpiry,
        price: SUBSCRIPTION_TYPES[user.subscription].price
    });
});

// Get subscription info
app.get('/api/subscription-info', (req, res) => {
    const userIP = req.ip || req.connection.remoteAddress;
    
    if (!userData.has(userIP)) {
        userData.set(userIP, {
            subscription: 'FREE',
            usedRequests: 0,
            subscriptionExpiry: null
        });
    }
    
    const user = userData.get(userIP);
    const subscription = SUBSCRIPTION_TYPES[user.subscription];
    
    res.json({
        subscription: user.subscription,
        used: user.usedRequests,
        limit: subscription.requests,
        remaining: subscription.requests - user.usedRequests,
        expiry: user.subscriptionExpiry,
        options: {
            monthly: SUBSCRIPTION_TYPES.MONTHLY,
            yearly: SUBSCRIPTION_TYPES.YEARLY
        }
    });
});

// Health check
app.get('/health', (req, res) => {
    res.json({ 
        status: 'OK', 
        message: 'AI Backend with Premium Subscription System',
        timestamp: new Date().toISOString()
    });
});

// Start server
app.listen(PORT, () => {
    console.log(' AI Backend with Premium Subscription System Started!');
    console.log('=' .repeat(50));
    console.log(' Server running on: http://localhost:' + PORT);
    console.log(' Moderation API: http://localhost:' + PORT + '/api/moderate');
    console.log(' Suggestions API: http://localhost:' + PORT + '/api/suggest');
    console.log(' Subscribe API: http://localhost:' + PORT + '/api/subscribe');
    console.log(' Subscription Info: http://localhost:' + PORT + '/api/subscription-info');
    console.log(' Health Check: http://localhost:' + PORT + '/health');
    console.log('=' .repeat(50));
    console.log(' Ready to receive requests!');
    console.log(' Free Plan: 5 requests');
    console.log(' Monthly Premium: 1000 requests - .99');
    console.log(' Yearly Premium: 12000 requests - .99 (Save .89!)');
});

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\n Shutting down server...');
    process.exit(0);
});
