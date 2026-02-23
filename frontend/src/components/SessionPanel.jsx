import React from 'react';

function SessionPanel({
  characterName,
  onCharacterNameChange,
  onSave,
  onLoadList,
  onShowHistory,
  sessions,
  onLoadSession,
  onCloseSessions,
}) {
  return (
    <div className="session-panel">
      <h2>Session</h2>
      <div className="stat-row">
        <label>Character Name:</label>
        <input
          type="text"
          value={characterName}
          onChange={e => onCharacterNameChange(e.target.value)}
          placeholder="Enter character name"
        />
      </div>
      <div className="session-buttons">
        <button className="save-button" onClick={onSave}>Save</button>
        <button className="load-button" onClick={onLoadList}>Load</button>
        <button className="history-button" onClick={onShowHistory}>History</button>
      </div>

      {sessions.length > 0 && (
        <div className="session-list">
          <h3>Saved Sessions</h3>
          <table>
            <thead>
              <tr>
                <th>Character</th>
                <th>Saved At</th>
                <th>Level</th>
                <th>Manna</th>
                <th>Pszi</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {sessions.map(s => (
                <tr key={s.id}>
                  <td>{s.characterName}</td>
                  <td>{new Date(s.savedAt).toLocaleString()}</td>
                  <td>{s.level}</td>
                  <td>{s.currentManna}/{s.maxManna}</td>
                  <td>{s.currentPszi}/{s.maxPszi}</td>
                  <td>
                    <button className="load-session-button" onClick={() => onLoadSession(s)}>
                      Load
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <button className="cancel-button" onClick={onCloseSessions}>Close</button>
        </div>
      )}
    </div>
  );
}

export default SessionPanel;
