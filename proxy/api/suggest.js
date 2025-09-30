const systemPrompt = require('../system_prompt.json');

module.exports = async function handler(req, res) {
  // Set CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  try {
    const { keyword, language, category } = req.body;
    
    if (!keyword || typeof keyword !== 'string') {
      return res.status(400).json({ error: 'Keyword is required' });
    }

    const apiKey = process.env.OPENAI_API_KEY;
    if (!apiKey) {
      return res.status(500).json({ error: 'OpenAI API key not configured' });
    }

    // Build user message
    let userMessage = `Keyword: "${keyword}"`;
    if (language) userMessage += `\nLanguage: ${language}`;
    if (category) userMessage += `\nCategory: ${category}`;

    // Call OpenAI Chat Completions API
    const response = await fetch('https://api.openai.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${apiKey}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: process.env.OPENAI_SUGGEST_MODEL || 'gpt-3.5-turbo',
        messages: [
          {
            role: 'system',
            content: systemPrompt.prompt
          },
          {
            role: 'user',
            content: userMessage
          }
        ],
        max_tokens: 1000,
        temperature: 0.7
      })
    });

    if (!response.ok) {
      console.error('OpenAI API error:', response.status, response.statusText);
      return res.status(500).json({ error: 'Suggestion service unavailable' });
    }

    const data = await response.json();
    const content = data.choices?.[0]?.message?.content;

    if (!content) {
      return res.status(500).json({ error: 'Invalid response from suggestion service' });
    }

    // Parse JSON response from OpenAI
    try {
      const suggestions = JSON.parse(content);
      
      // Ensure all required fields exist
      const result = {
        synonyms: suggestions.synonyms || [],
        variants: suggestions.variants || [],
        obfuscations: suggestions.obfuscations || [],
        regex: suggestions.regex || [],
        categories: suggestions.categories || [],
        notes: suggestions.notes || ''
      };

      return res.status(200).json(result);

    } catch (parseError) {
      console.error('Failed to parse OpenAI response:', parseError);
      return res.status(500).json({ error: 'Invalid suggestion format' });
    }

  } catch (error) {
    console.error('Suggestion error:', error);
    return res.status(500).json({ error: 'Internal server error' });
  }
}
