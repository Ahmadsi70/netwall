# Vercel Deployment Guide

## Prerequisites
1. Install Vercel CLI: `npm i -g vercel`
2. Create Vercel account at https://vercel.com

## Deployment Steps

### 1. Login to Vercel
```bash
vercel login
```

### 2. Set Environment Variables
```bash
# Set your OpenAI API key as a secret
vercel env add OPENAI_API_KEY
# When prompted, paste your API key and select all environments (production, preview, development)
```

### 3. Deploy
```bash
# From the proxy directory
cd proxy
vercel --prod
```

### 4. Get Your Deployment URL
After deployment, Vercel will provide a URL like:
`https://your-project-name.vercel.app`

### 5. Update Android App
Update the proxy URL in your Android app to:
- Moderate endpoint: `https://your-project-name.vercel.app/api/moderate`
- Suggest endpoint: `https://your-project-name.vercel.app/api/suggest`

## Testing Endpoints

### Test Moderation
```bash
curl -X POST https://your-project-name.vercel.app/api/moderate \
  -H "Content-Type: application/json" \
  -d '{"text": "This is a test message"}'
```

### Test Suggestions
```bash
curl -X POST https://your-project-name.vercel.app/api/suggest \
  -H "Content-Type: application/json" \
  -d '{"keyword": "violence", "language": "en", "category": "harmful"}'
```

## Environment Variables
- `OPENAI_API_KEY`: Your OpenAI API key (required)
- `OPENAI_MODERATE_MODEL`: Moderation model (default: text-moderation-latest)
- `OPENAI_SUGGEST_MODEL`: Chat model for suggestions (default: gpt-3.5-turbo)

## File Structure
```
proxy/
├── api/
│   ├── moderate.js    # Moderation endpoint
│   └── suggest.js     # AI suggestions endpoint
├── system_prompt.json # AI system prompt
├── vercel.json        # Vercel configuration
└── README-VERCEL.md   # This file
```
