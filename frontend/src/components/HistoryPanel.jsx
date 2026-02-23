import React from 'react';

function HistoryPanel({ characterName, history, onClose }) {
  return (
    <div className="dialog-overlay">
      <div className="dialog history-dialog">
        <h2>Action History: {characterName}</h2>

        {history.length === 0 ? (
          <p>No actions recorded yet.</p>
        ) : (
          <table className="history-table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Action</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              {history.map(entry => (
                <tr key={entry.id}>
                  <td>{new Date(entry.performedAt).toLocaleString()}</td>
                  <td>{formatActionType(entry.actionType)}</td>
                  <td>{entry.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        <div className="dialog-buttons">
          <button className="cancel-button" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}

function formatActionType(type) {
  switch (type) {
    case 'SPEND_PSZI': return 'Spend Pszi';
    case 'SPEND_MANA': return 'Spend Mana';
    case 'SKIP_TIME': return 'Skip Time';
    default: return type;
  }
}

export default HistoryPanel;
