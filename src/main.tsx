
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import './index.css'

// Global error handler
window.addEventListener('error', (event) => {
  console.error('Global error caught:', event.error);
  // You could render a fallback UI here if needed
});

try {
  createRoot(document.getElementById("root")!).render(<App />);
} catch (error) {
  console.error("Failed to render application:", error);
  // You could render a fallback error UI here
  const rootElement = document.getElementById("root");
  if (rootElement) {
    rootElement.innerHTML = `
      <div style="padding: 20px; text-align: center; font-family: system-ui;">
        <h2>Application Error</h2>
        <p>Sorry, there was a problem loading the application.</p>
        <p>Please check the console for more details.</p>
      </div>
    `;
  }
}
