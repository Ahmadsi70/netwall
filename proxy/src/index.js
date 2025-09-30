import express from 'express'
import cors from 'cors'
import helmet from 'helmet'
import fetch from 'node-fetch'
import dotenv from 'dotenv'
import fs from 'fs'

dotenv.config()

const app = express()
app.use(helmet())
app.use(cors({ origin: true }))
app.use(express.json({ limit: '8kb' }))

const PORT = process.env.PORT || 8787
const OPENAI_API_KEY = process.env.OPENAI_API_KEY
const MODERATION_MODEL = process.env.OPENAI_MODERATION_MODEL || 'text-moderation-latest'
const SYSTEM_PROMPT_PATH = process.env.SYSTEM_PROMPT_PATH || './system_prompt.json'
const SUGGEST_MODEL = process.env.OPENAI_SUGGEST_MODEL || 'gpt-4o-mini'

let systemPrompt = ''
try {
  systemPrompt = fs.readFileSync(SYSTEM_PROMPT_PATH, 'utf8')
} catch {
  systemPrompt = ''
}

app.post('/moderate', async (req, res) => {
  try {
    if (!OPENAI_API_KEY) return res.status(500).json({ error: 'Server not configured' })
    const input = (req.body?.input || '').toString().slice(0, 512)
    if (!input) return res.json({ inappropriate: false, confidence: 0 })

    // Prefer OpenAI Moderation endpoint
    const response = await fetch('https://api.openai.com/v1/moderations', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${OPENAI_API_KEY}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ model: MODERATION_MODEL, input })
    })

    if (!response.ok) {
      return res.status(502).json({ inappropriate: false, confidence: 0 })
    }
    const data = await response.json()
    // Normalize result
    const result = data?.results?.[0]
    const flagged = !!result?.flagged
    const categories = result?.categories || {}
    const category = Object.keys(categories).find(k => categories[k]) || null

    return res.json({ inappropriate: flagged, confidence: flagged ? 0.85 : 0.1, category, language: null })
  } catch (e) {
    return res.status(500).json({ inappropriate: false, confidence: 0 })
  }
})

// Suggest related terms/variants using system prompt (Chat models)
app.post('/suggest', async (req, res) => {
  try {
    if (!OPENAI_API_KEY) return res.status(500).json({ error: 'Server not configured' })
    const keyword = (req.body?.keyword || '').toString().slice(0, 64)
    const language = (req.body?.language || '').toString().slice(0, 8)
    const category = (req.body?.category || '').toString().slice(0, 32)
    if (!keyword) return res.json({ synonyms: [], variants: [], obfuscations: [], regex: [], categories: [], notes: [] })

    const sys = buildSuggestSystemPrompt(systemPrompt)
    const user = buildSuggestUserPrompt({ keyword, language, category })

    const response = await fetch('https://api.openai.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${OPENAI_API_KEY}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: SUGGEST_MODEL,
        temperature: 0.2,
        response_format: { type: 'json_object' },
        messages: [
          { role: 'system', content: sys },
          { role: 'user', content: user }
        ]
      })
    })

    if (!response.ok) return res.status(502).json({ synonyms: [], variants: [], obfuscations: [], regex: [], categories: [], notes: [] })
    const data = await response.json()
    const content = data?.choices?.[0]?.message?.content || '{}'
    let parsed
    try {
      parsed = JSON.parse(content)
    } catch {
      parsed = {}
    }
    // Normalize fields
    const out = {
      synonyms: Array.isArray(parsed.synonyms) ? parsed.synonyms.slice(0, 12) : [],
      variants: Array.isArray(parsed.variants) ? parsed.variants.slice(0, 12) : [],
      obfuscations: Array.isArray(parsed.obfuscations) ? parsed.obfuscations.slice(0, 12) : [],
      regex: Array.isArray(parsed.regex) ? parsed.regex.slice(0, 6) : [],
      categories: Array.isArray(parsed.categories) ? parsed.categories.slice(0, 6) : [],
      notes: typeof parsed.notes === 'string' ? parsed.notes.slice(0, 200) : ''
    }
    return res.json(out)
  } catch (e) {
    return res.status(500).json({ synonyms: [], variants: [], obfuscations: [], regex: [], categories: [], notes: [] })
  }
})

function buildSuggestSystemPrompt(jsonStr) {
  try {
    const cfg = JSON.parse(jsonStr || '{}')
    const policy = cfg?.policy?.description || ''
    const prompt = cfg?.prompt || ''
    return `${prompt}\nYou also generate related terms for blacklist expansion. Always output ONLY valid minified JSON with keys: synonyms, variants, obfuscations, regex, categories, notes.`
  } catch {
    return 'You generate related blacklist terms. Output JSON keys: synonyms, variants, obfuscations, regex, categories, notes.'
  }
}

function buildSuggestUserPrompt({ keyword, language, category }) {
  return `keyword: ${keyword}\nlanguage: ${language || 'auto'}\ncategory_hint: ${category || 'auto'}\n\nReturn JSON with up to 12 synonyms/variants/obfuscations and safe regex capturing common evasions.`
}

app.get('/health', (_req, res) => res.json({ ok: true }))

app.listen(PORT, () => console.log(`Moderation proxy listening on ${PORT}`))


