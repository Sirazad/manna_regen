import React, { useState } from 'react';
import CharacterPanel from './components/CharacterPanel';
import TimeDisplay from './components/TimeDisplay';
import ActionDialog from './components/ActionDialog';
import { createInitialState, performActionApi } from './gameLogic';
import './App.css';

function App() {
  const [state, setState] = useState(createInitialState());
  const [showActionDialog, setShowActionDialog] = useState(false);

  const maxMagicExhaustion = 25 * state.level;

  const updateField = (field, value) => {
    setState(prev => ({ ...prev, [field]: value }));
  };

  const handleSpendPszi = async (amount, timeSegments, force) => {
    try {
      const response = await performActionApi({
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
      const response = await performActionApi({
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
      const response = await performActionApi({
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

  return (
    <div className="App">
      <h1>Manna Regeneration Tracker</h1>

      <div className="main-layout">
        <CharacterPanel
          state={state}
          maxMagicExhaustion={maxMagicExhaustion}
          onUpdateField={updateField}
        />

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
    </div>
  );
}

export default App;
