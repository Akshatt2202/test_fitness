// ai.js — AI Workout Generator Page Logic

window.addEventListener('DOMContentLoaded', async () => {
  if (!requireAuth()) return;
  populateSidebarUser();
});

// ── AI Workout Generator ──
const aiWorkoutForm = document.getElementById('aiWorkoutForm');
if (aiWorkoutForm) {
  aiWorkoutForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('aiGenerateBtn');
    const resultContainer = document.getElementById('aiResultContainer');
    const planContent = document.getElementById('aiPlanContent');
    
    const payload = {
      goal: document.getElementById('aiGoal').value,
      frequency: document.getElementById('aiFrequency').value,
      level: document.getElementById('aiLevel').value
    };

    btn.disabled = true;
    btn.textContent = 'Generating... ⏳';
    resultContainer.style.display = 'block';
    planContent.innerHTML = '<div class="loading"><div class="spinner"></div> Generating your custom plan...</div>';

    try {
      const data = await api.generateAIWorkoutPlan(payload);
      // Use marked to parse the markdown response
      if (typeof marked !== 'undefined') {
        planContent.innerHTML = marked.parse(data.plan);
      } else {
        // Fallback if marked is not loaded
        planContent.innerHTML = `<pre style="white-space: pre-wrap; font-family: inherit;">${data.plan}</pre>`;
      }
      showToast('Workout plan generated!', 'success');
    } catch (err) {
      planContent.innerHTML = `<div style="color: var(--danger);"><strong>Error:</strong> ${err.message}</div>`;
      showToast('Failed to generate plan', 'error');
    } finally {
      btn.disabled = false;
      btn.textContent = 'Generate Plan ✨';
    }
  });
}
