import React, { useState } from 'react';
import CharacterPanel from './components/CharacterPanel';
import TimeDisplay from './components/TimeDisplay';
import ActionDialog from './components/ActionDialog';
import SessionPanel from './components/SessionPanel';
import HistoryPanel from './components/HistoryPanel';
import {
  createInitialState,
  performActionApi,
  saveSessionApi,
  listSessionsApi,
  loadSessionApi,
  getHistoryApi,
} from './gameLogic';
import './App.css';

function App() {
  const [state, setState] = useState(createInitialState());
  const [characterName, setCharacterName] = useState('');
  const [showActionDialog, setShowActionDialog] = useState(false);
  const [sessions, setSessions] = useState([]);
  const [history, setHistory] = useState([]);
  const [showHistory, setShowHistory] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');

  const maxMagicExhaustion = 25 * state.level;

  const updateField = (field, value) => {
    setState(prev => ({ ...prev, [field]: value }));
  };

  const showStatus = (msg) => {
    setStatusMessage(msg);
    setTimeout(() => setStatusMessage(''), 3000);
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
        return { warning: true, message: response.warningMessage };
      }
      setState(response.updatedState);
      return { warning: false };
    } catch (err) {
      return { warning: true, message: 'Backend error: ' + err.message };
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
        return { warning: true, message: response.warningMessage };
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
      setState(response.updatedState);
    } catch (err) {
      alert('Backend error: ' + err.message);
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
          <TimeDisplay currentTimeSegments={state.currentTimeSegments} />

          <div className="magic-exhaustion-display">
            <label>Magic Exhaustion Limit:</label>
            <span className={state.magicExhaustionLimit < 0 ? 'warning-value' : ''}>
              {state.magicExhaustionLimit.toFixed(1)} / {maxMagicExhaustion.toFixed(1)}
            </span>
          </div>

          <button
            className="action-button"
            onClick={() => setShowActionDialog(true)}
          >
            Perform Action
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
        />
      )}
    </div>
  );
}

export default App;
