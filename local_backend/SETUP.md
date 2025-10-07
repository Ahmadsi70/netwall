# Backend Setup Guide

## Environment Variables Setup

1. Copy `env.example` to `.env`:
```bash
cp env.example .env
```

2. Edit `.env` file and add your OpenAI API key:
```env
OPENAI_API_KEY=sk-your-actual-openai-api-key-here
```

## Railway Deployment

1. Install Railway CLI:
```bash
npm install -g @railway/cli
```

2. Login to Railway:
```bash
railway login
```

3. Deploy:
```bash
railway up
```

4. Set environment variables in Railway dashboard:
   - Go to your project dashboard
   - Go to Variables tab
   - Add: `OPENAI_API_KEY=sk-your-actual-key`

## Testing

1. Start local server:
```bash
npm start
```

2. Test endpoints:
```bash
# Test moderation
curl -X POST http://localhost:3000/api/moderate \
  -H "Content-Type: application/json" \
  -d '{"input": "test content"}'

# Test suggestions
curl -X POST http://localhost:3000/api/suggest \
  -H "Content-Type: application/json" \
  -d '{"keyword": "test"}'
```

## Current Status

- ✅ Server code ready
- ✅ API endpoints implemented
- ✅ Subscription system working
- ⚠️ Need to set OpenAI API key
- ⚠️ Need to deploy to Railway
- ⚠️ Need to update URL in Android app
