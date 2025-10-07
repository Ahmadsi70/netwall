// در server.js - سیستم اشتراک با قیمت‌های جدید

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const fetch = require('node-fetch');
const fs = require('fs');
const app = express();
const PORT = process.env.PORT || 3000;

// Environment variables
const OPENAI_API_KEY = process.env.OPENAI_API_KEY;
const APP_SECRET = process.env.APP_SECRET || 'internetguard-pro-default';
const MODERATION_MODEL = process.env.OPENAI_MODERATION_MODEL || 'text-moderation-latest';
const SUGGEST_MODEL = process.env.OPENAI_SUGGEST_MODEL || 'gpt-4o-mini';

// Load system prompt configuration
let systemPromptConfig = {};
try {
    const systemPromptData = fs.readFileSync('./system_prompt.json', 'utf8');
    systemPromptConfig = JSON.parse(systemPromptData);
    console.log(`[${new Date().toISOString()}] ✅ System prompt configuration loaded successfully`);
} catch (error) {
    console.warn(`[${new Date().toISOString()}] ⚠️ Could not load system_prompt.json: ${error.message}`);
    console.log(`[${new Date().toISOString()}] Using default configuration`);
}

// Store user data (در production از database استفاده کنید)
const userData = new Map();

// Rate limiting storage
const rateLimit = new Map();
const MAX_REQUESTS_PER_MINUTE = 60;
const RATE_LIMIT_WINDOW = 60 * 1000; // 1 minute

// Clean up old rate limit entries
setInterval(() => {
    const now = Date.now();
    for (const [key, data] of rateLimit.entries()) {
        if (now > data.resetTime) {
            rateLimit.delete(key);
        }
    }
}, 5 * 60 * 1000); // Clean every 5 minutes

// Rate limiting middleware
const rateLimitMiddleware = (req, res, next) => {
    const clientIP = req.ip || req.connection?.remoteAddress || 'unknown';
    const now = Date.now();
    
    if (!rateLimit.has(clientIP)) {
        rateLimit.set(clientIP, { count: 1, resetTime: now + RATE_LIMIT_WINDOW });
    } else {
        const data = rateLimit.get(clientIP);
        if (now > data.resetTime) {
            data.count = 1;
            data.resetTime = now + RATE_LIMIT_WINDOW;
        } else if (data.count >= MAX_REQUESTS_PER_MINUTE) {
            console.warn(`[${new Date().toISOString()}] Rate limit exceeded for IP: ${clientIP}`);
            return res.status(429).json({ 
                error: 'Rate limit exceeded', 
                retryAfter: Math.ceil((data.resetTime - now) / 1000) 
            });
        } else {
            data.count++;
        }
    }
    
    next();
};

// Middleware
app.use(cors());
app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
    res.json({
        status: 'OK',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        version: '1.0.0',
        environment: process.env.NODE_ENV || 'development',
        openai_configured: !!OPENAI_API_KEY,
        port: PORT
    });
});

// Root endpoint
app.get('/', (req, res) => {
    res.json({
        message: 'InternetGuard Pro AI Backend',
        version: '1.0.0',
        endpoints: {
            health: '/health',
            moderate: '/api/moderate',
            suggest: '/api/suggest'
        },
        status: 'running'
    });
});

// Authentication middleware
const authenticate = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const expectedAuth = `Bearer ${APP_SECRET}`;
    
    if (!authHeader || authHeader !== expectedAuth) {
        console.warn(`[${new Date().toISOString()}] Unauthorized access attempt from IP: ${req.ip}`);
        return res.status(401).json({ error: 'Unauthorized' });
    }
    
    next();
};

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
app.post('/api/moderate', rateLimitMiddleware, authenticate, checkSubscription, async (req, res) => {
    const startTime = Date.now();
    const clientIP = req.ip || req.connection?.remoteAddress || 'unknown';
    
    try {
        console.log(`[${new Date().toISOString()}] Moderation request: "${req.body.input?.substring(0, 50)}${req.body.input?.length > 50 ? '...' : ''}" | IP: ${clientIP}`);
    
    const { input } = req.body;
    
        if (!input || typeof input !== 'string') {
            console.warn(`[${new Date().toISOString()}] Invalid input from IP: ${clientIP}`);
            return res.status(400).json({ error: 'input is required' });
        }
        
        let response;
        
        // Try OpenAI API first if available
        if (OPENAI_API_KEY) {
            try {
                const openaiResponse = await fetch('https://api.openai.com/v1/moderations', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${OPENAI_API_KEY}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        input: input,
                        model: MODERATION_MODEL
                    })
                });
                
                if (openaiResponse.ok) {
                    const data = await openaiResponse.json();
                    const result = data?.results?.[0];
                    
                    response = {
                        inappropriate: result?.flagged || false,
                        confidence: result?.flagged ? 0.85 : 0.1,
                        category: result?.flagged ? Object.keys(result?.categories || {}).find(k => result.categories[k]) || 'inappropriate' : null,
                        language: 'auto'
                    };
                    
                    console.log(`[${new Date().toISOString()}] OpenAI moderation result: ${response.inappropriate ? 'BLOCKED' : 'ALLOWED'} | Duration: ${Date.now() - startTime}ms | IP: ${clientIP}`);
                } else {
                    throw new Error(`OpenAI API error: ${openaiResponse.status}`);
                }
            } catch (error) {
                console.error(`[${new Date().toISOString()}] OpenAI API error: ${error.message} | IP: ${clientIP}`);
                // Fallback to keyword-based detection
                response = fallbackModeration(input);
            }
        } else {
            // Fallback to keyword-based detection
            response = fallbackModeration(input);
        }
        
        // اضافه کردن اطلاعات subscription به response
        res.json({
            ...response,
            subscription: req.subscriptionInfo
        });
        
    } catch (error) {
        const duration = Date.now() - startTime;
        console.error(`[${new Date().toISOString()}] Moderation error: ${error.message} | Duration: ${duration}ms | IP: ${clientIP}`);
        res.status(500).json({ error: 'Internal server error' });
    }
});

// Fallback moderation function
function fallbackModeration(input) {
    const harmfulKeywords = ['violence', 'hate', 'harm', 'danger', 'kill', 'death', 'drug', 'porn', 'sex'];
    const isInappropriate = harmfulKeywords.some(keyword => 
        input.toLowerCase().includes(keyword)
    );
    
    return {
        inappropriate: isInappropriate,
        confidence: isInappropriate ? 0.75 : 0.1,
        category: isInappropriate ? 'inappropriate' : null,
        language: 'en'
    };
}

app.post('/api/suggest', rateLimitMiddleware, authenticate, checkSubscription, async (req, res) => {
    const startTime = Date.now();
    const clientIP = req.ip || req.connection?.remoteAddress || 'unknown';
    
    try {
        console.log(`[${new Date().toISOString()}] Suggestion request: "${req.body.keyword}" | IP: ${clientIP}`);
        
        const { keyword, language, category } = req.body;
        
        if (!keyword || typeof keyword !== 'string') {
            console.warn(`[${new Date().toISOString()}] Invalid keyword from IP: ${clientIP}`);
            return res.status(400).json({ error: 'keyword is required' });
        }
        
        let suggestions;
        
        // Try OpenAI API first if available
        if (OPENAI_API_KEY) {
            try {
                // Use system prompt from configuration
                const systemPrompt = systemPromptConfig.prompt_templates?.suggestion || `You are a content moderation assistant for InternetGuard Pro. Generate related terms for blocking inappropriate content.
                
Return ONLY a valid JSON object with these fields:
{
  "synonyms": ["word1", "word2"],
  "variants": ["variant1", "variant2"], 
  "obfuscations": ["w0rd", "w*rd"],
  "regex": ["pattern1", "pattern2"],
  "categories": ["category1"],
  "notes": "brief explanation"
}

Focus on harmful content categories: violence, sexual content, drugs, hate speech, self-harm, gambling, scams.`;

                const userMessage = `Keyword: "${keyword}"${language ? `\nLanguage: ${language}` : ''}${category ? `\nCategory: ${category}` : ''}\n\nReturn JSON with up to 12 synonyms/variants/obfuscations and safe regex patterns.`;

                const openaiResponse = await fetch('https://api.openai.com/v1/chat/completions', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${OPENAI_API_KEY}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        model: SUGGEST_MODEL,
                        messages: [
                            { role: 'system', content: systemPrompt },
                            { role: 'user', content: userMessage }
                        ],
                        response_format: { type: 'json_object' },
                        max_tokens: 800,
                        temperature: 0.2
                    })
                });
                
                if (openaiResponse.ok) {
                    const data = await openaiResponse.json();
                    const content = data?.choices?.[0]?.message?.content;
                    
                    if (content) {
                        try {
                            const parsed = JSON.parse(content);
                            suggestions = {
                                synonyms: Array.isArray(parsed.synonyms) ? parsed.synonyms.slice(0, 12) : [],
                                variants: Array.isArray(parsed.variants) ? parsed.variants.slice(0, 12) : [],
                                obfuscations: Array.isArray(parsed.obfuscations) ? parsed.obfuscations.slice(0, 12) : [],
                                regex: Array.isArray(parsed.regex) ? parsed.regex.slice(0, 6) : [],
                                categories: Array.isArray(parsed.categories) ? parsed.categories.slice(0, 6) : [],
                                notes: typeof parsed.notes === 'string' ? parsed.notes.slice(0, 200) : ''
                            };
                            
                            console.log(`[${new Date().toISOString()}] OpenAI suggestions generated: ${suggestions.synonyms.length + suggestions.variants.length} terms | Duration: ${Date.now() - startTime}ms | IP: ${clientIP}`);
                        } catch (parseError) {
                            console.error(`[${new Date().toISOString()}] Failed to parse OpenAI response: ${parseError.message} | IP: ${clientIP}`);
                            suggestions = fallbackSuggestions(keyword);
                        }
                    } else {
                        throw new Error('No content in OpenAI response');
                    }
                } else {
                    throw new Error(`OpenAI API error: ${openaiResponse.status}`);
                }
            } catch (error) {
                console.error(`[${new Date().toISOString()}] OpenAI API error: ${error.message} | IP: ${clientIP}`);
                suggestions = fallbackSuggestions(keyword);
            }
        } else {
            suggestions = fallbackSuggestions(keyword);
        }
    
    // اضافه کردن اطلاعات subscription به response
    res.json({
            ...suggestions,
        subscription: req.subscriptionInfo
    });
        
    } catch (error) {
        const duration = Date.now() - startTime;
        console.error(`[${new Date().toISOString()}] Suggestion error: ${error.message} | Duration: ${duration}ms | IP: ${clientIP}`);
        res.status(500).json({ error: 'Internal server error' });
    }
});

// Fallback suggestions function
function fallbackSuggestions(keyword) {
    const k = keyword.toLowerCase();
    
    if (k.includes('violence')) {
        return {
            synonyms: ["aggression", "harm", "hurt", "damage"],
            variants: ["violent", "violently", "aggressive"],
            obfuscations: ["v1olence", "v*olence", "v10l3nc3", "v!olence"],
            regex: ["v[0-9]olence", "v[*]olence", "v[!]olence"],
            categories: ["violence", "harmful"],
            notes: "Content related to violence and aggression"
        };
    } else if (k.includes('game')) {
        return {
            synonyms: ["gaming", "play", "entertainment", "fun"],
            variants: ["games", "gamer", "gaming"],
            obfuscations: ["g4me", "g*me", "g@me", "gam3"],
            regex: ["g[0-9]me", "g[*]me", "g[@]me"],
            categories: ["gaming", "entertainment"],
            notes: "Gaming and entertainment related content"
        };
    } else {
        // Default suggestions
        return {
            synonyms: [keyword + " related", keyword + " content"],
            variants: [keyword + "s", keyword + "ing"],
            obfuscations: [keyword.replace(/[aeiou]/g, '*')],
            regex: [keyword.replace(/[aeiou]/g, '[aeiou]')],
            categories: ["general"],
            notes: "General content filtering"
        };
    }
}

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
    const healthData = {
        status: 'ok',
        timestamp: new Date().toISOString(),
        version: '2.0.0',
        uptime: process.uptime(),
        memory: {
            used: Math.round(process.memoryUsage().heapUsed / 1024 / 1024),
            total: Math.round(process.memoryUsage().heapTotal / 1024 / 1024),
            external: Math.round(process.memoryUsage().external / 1024 / 1024)
        },
        environment: {
            nodeVersion: process.version,
            platform: process.platform,
            arch: process.arch
        },
        services: {
            openai: OPENAI_API_KEY ? 'configured' : 'not_configured',
            moderation: MODERATION_MODEL,
            suggest: SUGGEST_MODEL,
            authentication: 'enabled',
            rateLimit: 'enabled',
            subscription: 'enabled'
        },
        subscription: {
            free: SUBSCRIPTION_TYPES.FREE,
            monthly: SUBSCRIPTION_TYPES.MONTHLY,
            yearly: SUBSCRIPTION_TYPES.YEARLY
        }
    };
    
    console.log(`[${new Date().toISOString()}] Health check requested from IP: ${req.ip || req.connection?.remoteAddress || 'unknown'}`);
    res.json(healthData);
});

// Start server
app.listen(PORT, () => {
    console.log(`[${new Date().toISOString()}] 🚀 AI Backend with Premium Subscription System Started!`);
    console.log('=' .repeat(60));
    console.log(`[${new Date().toISOString()}] 📡 Server running on: http://localhost:${PORT}`);
    console.log(`[${new Date().toISOString()}] 🔍 Moderation API: http://localhost:${PORT}/api/moderate`);
    console.log(`[${new Date().toISOString()}] 💡 Suggestions API: http://localhost:${PORT}/api/suggest`);
    console.log(`[${new Date().toISOString()}] 💳 Subscribe API: http://localhost:${PORT}/api/subscribe`);
    console.log(`[${new Date().toISOString()}] 📊 Subscription Info: http://localhost:${PORT}/api/subscription-info`);
    console.log(`[${new Date().toISOString()}] ❤️ Health Check: http://localhost:${PORT}/health`);
    console.log('=' .repeat(60));
    console.log(`[${new Date().toISOString()}] ✅ Ready to receive requests!`);
    console.log(`[${new Date().toISOString()}] 🔐 Authentication: ${APP_SECRET ? 'Enabled' : 'Disabled'}`);
    console.log(`[${new Date().toISOString()}] 🤖 OpenAI API: ${OPENAI_API_KEY ? 'Configured' : 'Not configured (using fallback)'}`);
    console.log(`[${new Date().toISOString()}] 📈 Rate Limiting: Enabled (${MAX_REQUESTS_PER_MINUTE} requests/minute)`);
    console.log('=' .repeat(60));
    console.log(`[${new Date().toISOString()}] 💰 Subscription Plans:`);
    console.log(`[${new Date().toISOString()}]    🆓 Free Plan: ${SUBSCRIPTION_TYPES.FREE.requests} requests`);
    console.log(`[${new Date().toISOString()}]    📅 Monthly Premium: ${SUBSCRIPTION_TYPES.MONTHLY.requests} requests - $${SUBSCRIPTION_TYPES.MONTHLY.price}`);
    console.log(`[${new Date().toISOString()}]    📆 Yearly Premium: ${SUBSCRIPTION_TYPES.YEARLY.requests} requests - $${SUBSCRIPTION_TYPES.YEARLY.price} (Save $${(SUBSCRIPTION_TYPES.MONTHLY.price * 12) - SUBSCRIPTION_TYPES.YEARLY.price}!)`);
    console.log('=' .repeat(60));
});

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\n Shutting down server...');
    process.exit(0);
});
