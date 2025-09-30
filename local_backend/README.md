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
