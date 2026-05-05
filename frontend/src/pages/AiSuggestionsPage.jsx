import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import {
  getPendingAiSuggestions,
  acceptAiSuggestion,
  rejectAiSuggestion,
} from "../api/aiSuggestions";

import "./AiSuggestionsPage.css";

export default function AiSuggestionsPage() {
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [sidebarOpen, setSidebarOpen] = useState(true);

  async function loadSuggestions() {
    try {
      setLoading(true);
      setError("");
      const data = await getPendingAiSuggestions();
      setSuggestions(data);
    } catch (err) {
      setError(err.message || "Failed to load AI suggestions");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadSuggestions();
  }, []);

  async function handleAccept(id) {
    try {
      await acceptAiSuggestion(id);
      setSuggestions((prev) => prev.filter((item) => item.id !== id));
    } catch (err) {
      setError(err.message || "Failed to accept suggestion");
    }
  }

  async function handleReject(id) {
    try {
      await rejectAiSuggestion(id);
      setSuggestions((prev) => prev.filter((item) => item.id !== id));
    } catch (err) {
      setError(err.message || "Failed to reject suggestion");
    }
  }

  function statusClass(status) {
    return `status-pill status-${String(status).toLowerCase()}`;
  }

  function confidenceClass(confidence) {
    return `confidence-badge confidence-${String(confidence).toLowerCase()}`;
  }

  return (
    <div className="app-layout">
      <Sidebar open={sidebarOpen} setOpen={setSidebarOpen} />

      <main className="ai-suggestions-page main-content">
        <header className="ai-suggestions-header">
          <div>
            <h1>AI Suggestions</h1>
            <p>Review suggested status updates before applying them.</p>
          </div>

          <button className="refresh-button" onClick={loadSuggestions}>
            Refresh
          </button>
        </header>

        {loading && <p className="loading-text">Loading suggestions...</p>}
        {error && <p className="error-text">{error}</p>}

        {!loading && !error && suggestions.length === 0 && (
          <div className="empty-state">
            <div className="empty-state-icon">◎</div>
            <h2>No pending suggestions</h2>
            <p>New AI suggestions will appear here after email analysis.</p>
          </div>
        )}

        <div className="ai-suggestions-list">
          {suggestions.map((suggestion) => (
            <article key={suggestion.id} className="ai-suggestion-card">
              <div className="ai-suggestion-card__header">
                <div>
                  <h2>{suggestion.company}</h2>
                  <p>{suggestion.position}</p>
                </div>

                <span className={confidenceClass(suggestion.confidence)}>
                  {suggestion.confidence}
                </span>
              </div>

              <div className="status-change">
                <span className={statusClass(suggestion.currentStatus)}>
                  {suggestion.currentStatus}
                </span>
                <span className="status-arrow">→</span>
                <span className={statusClass(suggestion.suggestedStatus)}>
                  {suggestion.suggestedStatus}
                </span>
              </div>

              <p className="suggestion-reason">{suggestion.reason}</p>

              {suggestion.emailSnippet && (
                <blockquote className="email-snippet">
                  {suggestion.emailSnippet}
                </blockquote>
              )}

              <div className="ai-suggestion-actions">
                <button
                  className="accept-button"
                  onClick={() => handleAccept(suggestion.id)}
                >
                  Accept
                </button>

                <button
                  className="reject-button"
                  onClick={() => handleReject(suggestion.id)}
                >
                  Reject
                </button>
              </div>
            </article>
          ))}
        </div>
      </main>
    </div>
  );
}