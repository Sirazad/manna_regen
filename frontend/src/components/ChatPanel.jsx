import React, { useState, useRef, useEffect } from 'react';
import styles from './ChatPanel.module.css';

function ChatPanel() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = () => {
    const text = input.trim();
    if (!text || loading) return;

    setMessages(prev => [...prev, { role: 'user', text }]);
    setInput('');
    setLoading(true);

    setTimeout(() => {
      setMessages(prev => [...prev, { role: 'assistant', text: 'Ez a funkció hamarosan elérhető lesz.' }]);
      setLoading(false);
    }, 500);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <>
      {open && (
        <div className={styles.panel}>
          <div className={styles.header}>
            <span className={styles.headerTitle}>Asszisztens</span>
            <button className={styles.clearButton} onClick={() => setMessages([])}>
              Törlés
            </button>
          </div>

          <div className={styles.messages}>
            {messages.length === 0 ? (
              <span className={styles.emptyState}>Írj egy üzenetet...</span>
            ) : (
              messages.map((msg, i) => (
                <div
                  key={i}
                  className={`${styles.messageBubble} ${msg.role === 'user' ? styles.userMessage : styles.assistantMessage}`}
                >
                  {msg.text}
                </div>
              ))
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className={styles.inputRow}>
            <textarea
              className={styles.inputArea}
              rows={2}
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={loading}
              placeholder="Írj egy üzenetet..."
            />
            <button
              className={styles.sendButton}
              onClick={sendMessage}
              disabled={loading || !input.trim()}
            >
              Küldés
            </button>
          </div>
        </div>
      )}

      <button
        className={styles.toggleButton}
        onClick={() => setOpen(o => !o)}
        aria-label="Asszisztens"
      >
        💬
      </button>
    </>
  );
}

export default ChatPanel;
