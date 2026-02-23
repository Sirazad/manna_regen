import React, { useState } from 'react';
import CharacterPanel from './components/CharacterPanel';
import TimeDisplay from './components/TimeDisplay';
import ActionDialog from './components/ActionDialog';
import { createInitialState, calculatePsziRegen, calculateMagicExhaustionRecovery } from './gameLogic';
import './App.css';

function App() {
  const [state, setState] = useState(createInitialState());
  const [showActionDialog, setShowActionDialog] = useState(false);

  const maxMagicExhaustion = 25 * state.level;

  const updateField = (field, value) => {
    setState(prev => ({ ...prev, [field]: value }));
  };

  const handleSpendPszi = (amount, timeSegments, force) => {
    const newExhaustion = state.magicExhaustionLimit - amount;
    if (newExhaustion < 0 && !force) {
      return {
        warning: true,
        message: `Magic exhaustion limit would go below 0 (to ${newExhaustion.toFixed(1)}). Do you want to proceed?`,
      };
    }
    setState(prev => ({
      ...prev,
      currentPszi: Math.max(0, prev.currentPszi - amount),
      magicExhaustionLimit: newExhaustion,
      currentTimeSegments: prev.currentTimeSegments + timeSegments,
    }));
    return { warning: false };
  };

  const handleSpendMana = (amount, timeSegments, force) => {
    const newExhaustion = state.magicExhaustionLimit - amount;
    if (newExhaustion < 0 && !force) {
      return {
        warning: true,
        message: `Magic exhaustion limit would go below 0 (to ${newExhaustion.toFixed(1)}). Do you want to proceed?`,
      };
    }
    setState(prev => ({
      ...prev,
      currentManna: Math.max(0, prev.currentManna - amount),
      magicExhaustionLimit: newExhaustion,
      currentTimeSegments: prev.currentTimeSegments + timeSegments,
    }));
    return { warning: false };
  };

  const handleSkipTime = (timeSegments, activityType, maxPainPointsAffected) => {
    setState(prev => {
      const psziRegen = calculatePsziRegen(prev.maxPszi, timeSegments, activityType);
      const newPszi = Math.min(prev.maxPszi, prev.currentPszi + psziRegen);

      const exhaustionRecovery = calculateMagicExhaustionRecovery(
        prev.stamina, prev.level, timeSegments, activityType, maxPainPointsAffected
      );
      const maxExh = 25 * prev.level;
      const newExhaustion = Math.min(maxExh, prev.magicExhaustionLimit + exhaustionRecovery);

      return {
        ...prev,
        currentPszi: Math.floor(newPszi),
        magicExhaustionLimit: newExhaustion,
        currentTimeSegments: prev.currentTimeSegments + timeSegments,
      };
    });
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
