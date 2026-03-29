import React, { useState } from 'react';

function HistoryPanel({ characterName, history, onClose, onRestore, onDelete }) {
  const [pendingDeleteId, setPendingDeleteId] = useState(null);

  const handleDeleteClick = (id) => {
    setPendingDeleteId(id);
  };

  const handleDeleteConfirm = async () => {
    await onDelete(pendingDeleteId);
    setPendingDeleteId(null);
  };

  return (
    <div className="dialog-overlay">
      <div className="dialog history-dialog">
        <h2>Action History: {characterName}</h2>

        {pendingDeleteId && (
          <div className="warning-dialog" style={{ marginBottom: '12px' }}>
            <p className="warning-text">Delete this history entry?</p>
            <div className="dialog-buttons">
              <button className="confirm-button" onClick={handleDeleteConfirm}>Yes, Delete</button>
              <button className="cancel-button" onClick={() => setPendingDeleteId(null)}>Cancel</button>
            </div>
          </div>
        )}

        {history.length === 0 ? (
          <p>No actions recorded yet.</p>
        ) : (
          <table className="history-table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Action</th>
                <th>Details</th>
                <th>Effects</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {history.map(entry => (
                <tr key={entry.id} className={entry.id === pendingDeleteId ? 'row-pending-delete' : ''}>
                  <td>{new Date(entry.performedAt).toLocaleString()}</td>
                  <td>{formatActionType(entry.actionType)}</td>
                  <td>{entry.description}</td>
                  <td>{entry.effects || '—'}</td>
                  <td>
                    {entry.snapshotTimeSegments != null && (
                      <button className="load-session-button" onClick={() => onRestore(entry)}>
                        Restore
                      </button>
                    )}
                  </td>
                  <td>
                    <button className="delete-button" onClick={() => handleDeleteClick(entry.id)}>
                      X
                    </button>
                  </td>
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
