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
    const { text } = req.body;
    
    if (!text || typeof text !== 'string') {
      return res.status(400).json({ error: 'Text is required' });
    }

    const apiKey = process.env.OPENAI_API_KEY;
    if (!apiKey) {
      return res.status(500).json({ error: 'OpenAI API key not configured' });
    }

    // Call OpenAI Moderations API
    const response = await fetch('https://api.openai.com/v1/moderations', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${apiKey}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        input: text,
        model: process.env.OPENAI_MODERATE_MODEL || 'text-moderation-latest'
      })
    });

    if (!response.ok) {
      console.error('OpenAI API error:', response.status, response.statusText);
      return res.status(500).json({ error: 'Moderation service unavailable' });
    }

    const data = await response.json();
    const result = data.results?.[0];

    if (!result) {
      return res.status(500).json({ error: 'Invalid response from moderation service' });
    }

    // Return the moderation result
    return res.status(200).json({
      flagged: result.flagged,
      categories: result.categories,
      category_scores: result.category_scores
    });

  } catch (error) {
    console.error('Moderation error:', error);
    return res.status(500).json({ error: 'Internal server error' });
  }
}
