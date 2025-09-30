# InternetGuard Moderation Proxy

A minimal proxy that hides API keys and exposes a stable /moderate endpoint for the Android app.

## Setup

1. Create .env with:
- PORT=8787
- OPENAI_API_KEY=sk-...
- OPENAI_MODERATION_MODEL=text-moderation-latest
- SYSTEM_PROMPT_PATH=./system_prompt.json

2. Install deps:
- npm i

3. Run:
- npm run start

## Endpoint
- POST /moderate
- Body: { "input": "text to check" }
- Response: { inappropriate: boolean, confidence: number, category?: string, language?: string }

## Notes
- Keep the system prompt under system_prompt.json.
- Add rate limiting and auth if exposed publicly.
