const express = require('express');
const router = express.Router();
const { GoogleGenerativeAI } = require('@google/generative-ai');

// Middleware if you have it. If not, we'll just expose it.
// Assuming auth middleware exists since it's a private tracker
const { protect } = require('../middleware/auth'); 

router.post('/workout-plan', protect, async (req, res) => {
  try {
    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
      return res.status(500).json({ 
        message: 'GEMINI_API_KEY is not set in the server environment variables.' 
      });
    }

    const { goal, frequency, level } = req.body;
    if (!goal || !frequency || !level) {
      return res.status(400).json({ message: 'Goal, frequency, and level are required.' });
    }

    const genAI = new GoogleGenerativeAI(apiKey);
    const model = genAI.getGenerativeModel({ model: 'gemini-flash-latest' });

    const prompt = `You are an expert fitness trainer. Create a highly structured, weekly workout routine based on the following parameters:
- **Primary Goal:** ${goal}
- **Workout Frequency:** ${frequency} days per week
- **Experience Level:** ${level}

Format your response strictly using Markdown. Include:
1. A brief encouraging introduction.
2. The weekly schedule clearly broken down by day (e.g., Day 1: Upper Body, Day 2: Rest).
3. For workout days, list specific exercises, sets, and reps (e.g., Bench Press: 3 sets x 10 reps).
4. A small tip section on nutrition or recovery at the end.

Keep it concise, well-formatted, and realistic.`;

    const result = await model.generateContent(prompt);
    const text = result.response.text();
    
    res.json({ plan: text });
  } catch (err) {
    console.error('AI Workout Plan Error:', err);
    res.status(500).json({ message: 'Failed to generate workout plan', error: err.message });
  }
});

module.exports = router;
