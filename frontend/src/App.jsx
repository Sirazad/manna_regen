import React, { useState } from 'react';
import CharacterPanel from './components/CharacterPanel';
import TimeDisplay from './components/TimeDisplay';
import ActionDialog from './components/ActionDialog';
import SessionPanel from './components/SessionPanel';
import HistoryPanel from './components/HistoryPanel';
import CharacterManagerPanel from './components/CharacterManagerPanel';
import {
  createInitialState,
  performActionApi,
  saveSessionApi,
  listSessionsApi,
  getHistoryApi,
  restoreFromHistoryApi,
  deleteHistoryEntryApi,
} from './gameLogic';
import './App.css';

function App() {
  const [state, setState] = useState(createInitialState());
  const [characterName, setCharacterName] = useState('');
  const [showActionDialog, setShowActionDialog] = useState(false);
  const [showCharacterManager, setShowCharacterManager] = useState(false);
  const [sessions, setSessions] = useState([]);
  const [history, setHistory] = useState([]);
  const [showHistory, setShowHistory] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');

  const maxMagicExhaustion = 25 * state.level;

  const updateField = (field, value) => {
    setState(prev => {
      const updates = { [field]: value };
      if (field === 'maxManna') updates.currentManna = value;
      if (field === 'maxPszi') updates.currentPszi = value;
      if (field === 'level') updates.magicExhaustionLimit = 25 * value;
      return { ...prev, ...updates };
    });
  };

  const showStatus = (msg) => {
    setStatusMessage(msg);
    setTimeout(() => setStatusMessage(''), 3000);
  };

  const handleFullyRested = () => {
    setState(prev => ({
      ...prev,
      currentManna: prev.maxManna,
      currentPszi: prev.maxPszi,
      magicExhaustionLimit: 25 * prev.level,
      currentTimeSegments: 0,
      restingUntilSegments: 0,
    }));
    showStatus('Character fully rested.');
  };

  const handleSpendPszi = async (amount, timeSegments, force) => {
    try {
      const response = await performActionApi(characterName, {
        actionType: 'SPEND_PSZI',
        amount,
        timeSegments,
        forceAction: force,
        currentState: state,
      });
      if (response.warning) {
        return { warning: true, error: response.error, message: response.warningMessage };
      }
      setState(response.updatedState);
      return { warning: false };
    } catch (err) {
      return { warning: true, error: false, message: 'Backend error: ' + err.message };
    }
  };

  const handleSpendMana = async (amount, timeSegments, force) => {
    try {
      const response = await performActionApi(characterName, {
        actionType: 'SPEND_MANA',
        amount,
        timeSegments,
        forceAction: force,
        currentState: state,
      });
      if (response.warning) {
        return { warning: true, error: response.error, message: response.warningMessage };
      }
      setState(response.updatedState);
      return { warning: false };
    } catch (err) {
      return { warning: true, message: 'Backend error: ' + err.message };
    }
  };

  const handleSkipTime = async (timeSegments, activityType, maxPainPointsAffected) => {
    try {
      const response = await performActionApi(characterName, {
        actionType: 'SKIP_TIME',
        timeSegments,
        activityType,
        maxPainPointsAffected,
        currentState: state,
      });
      if (response.warning) {
        return { warning: true, error: response.error, message: response.warningMessage };
      }
      setState(response.updatedState);
      return { warning: false };
    } catch (err) {
      return { warning: true, error: false, message: 'Backend error: ' + err.message };
    }
  };

  const handleSave = async () => {
    if (!characterName.trim()) {
      alert('Please enter a character name before saving.');
      return;
    }
    try {
      await saveSessionApi(characterName.trim(), state);
      showStatus(`Saved session for "${characterName}".`);
    } catch (err) {
      alert('Save error: ' + err.message);
    }
  };

  const handleLoadList = async () => {
    try {
      const list = await listSessionsApi();
      setSessions(list);
    } catch (err) {
      alert('Load error: ' + err.message);
    }
  };

  const handleLoadSession = async (session) => {
    setState({
      maxManna: session.maxManna,
      currentManna: session.currentManna,
      maxPszi: session.maxPszi,
      currentPszi: session.currentPszi,
      wielderType: session.wielderType,
      level: session.level,
      stamina: session.stamina,
      magicExhaustionLimit: session.magicExhaustionLimit,
      currentTimeSegments: session.currentTimeSegments,
      restingUntilSegments: session.restingUntilSegments || 0,
    });
    setCharacterName(session.characterName);
    setSessions([]);
    showStatus(`Loaded session for "${session.characterName}".`);
  };

  const handleShowHistory = async () => {
    if (!characterName.trim()) {
      alert('Please enter a character name to view history.');
      return;
    }
    try {
      const hist = await getHistoryApi(characterName.trim());
      setHistory(hist);
      setShowHistory(true);
    } catch (err) {
      alert('History error: ' + err.message);
    }
  };

  const handleDeleteHistoryEntry = async (logId) => {
    try {
      await deleteHistoryEntryApi(logId);
      setHistory(prev => prev.filter(e => e.id !== logId));
    } catch (err) {
      alert('Delete error: ' + err.message);
    }
  };

  const handleRestoreFromHistory = async (entry) => {
    try {
      const restoredState = await restoreFromHistoryApi(entry.id);
      setState(restoredState);
      setShowHistory(false);
      showStatus(`Restored state from ${new Date(entry.performedAt).toLocaleString()}.`);
    } catch (err) {
      alert('Restore error: ' + err.message);
    }
  };

  const handleLoadCharacter = (name, loadedState) => {
    setState(loadedState);
    setCharacterName(name);
    setShowCharacterManager(false);
    showStatus(`Loaded character "${name}".`);
  };

  return (
    <div className="App">
      <h1>Manna Regeneration Tracker</h1>

      {statusMessage && <div className="status-message">{statusMessage}</div>}

      <div className="main-layout">
        <div className="left-panel">
          <SessionPanel
            characterName={characterName}
            onCharacterNameChange={setCharacterName}
            onSave={handleSave}
            onLoadList={handleLoadList}
            onShowHistory={handleShowHistory}
            sessions={sessions}
            onLoadSession={handleLoadSession}
            onCloseSessions={() => setSessions([])}
          />

          <CharacterPanel
            state={state}
            maxMagicExhaustion={maxMagicExhaustion}
            onUpdateField={updateField}
          />
        </div>

        <div className="right-panel">
          <TimeDisplay
            currentTimeSegments={state.currentTimeSegments}
            restingUntilSegments={state.restingUntilSegments || 0}
            onResetTimer={() => setState(prev => ({ ...prev, currentTimeSegments: 0, restingUntilSegments: 0 }))}
          />

          <div className="magic-exhaustion-display">
            <label>Magic Exhaustion Limit:</label>
            <span className={state.magicExhaustionLimit < 0 ? 'warning-value' : ''}>
              {state.magicExhaustionLimit.toFixed(1)} / {maxMagicExhaustion.toFixed(1)}
            </span>
          </div>

          <button className="action-button" onClick={() => setShowActionDialog(true)}>
            Perform Action
          </button>

          <button className="fully-rested-button" onClick={handleFullyRested}>
            Fully Rested
          </button>

          <button className="characters-button" onClick={() => setShowCharacterManager(true)}>
            Characters
          </button>
        </div>
      </div>

      {showActionDialog && (
        <ActionDialog
          state={state}
          onSpendPszi={handleSpendPszi}
          onSpendMana={handleSpendMana}
          onSkipTime={handleSkipTime}
          onClose={() => setShowActionDialog(false)}
        />
      )}

      {showHistory && (
        <HistoryPanel
          characterName={characterName}
          history={history}
          onClose={() => setShowHistory(false)}
          onRestore={handleRestoreFromHistory}
          onDelete={handleDeleteHistoryEntry}
        />
      )}

      {showCharacterManager && (
        <CharacterManagerPanel
          onLoad={handleLoadCharacter}
          onClose={() => setShowCharacterManager(false)}
        />
      )}
    </div>
  );
}

export default App;
