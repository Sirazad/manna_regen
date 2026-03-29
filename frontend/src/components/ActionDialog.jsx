import React, { useState } from 'react';
import { ACTIVITY_TYPES } from '../gameLogic';

const TIME_UNITS = [
  { label: 'Segment', multiplier: 1 },
  { label: 'Half-round', multiplier: 2 },
  { label: 'Round', multiplier: 4 },
  { label: 'Minutes', multiplier: 60 },
  { label: 'Hours', multiplier: 3600 },
];

function ActionDialog({ state, onSpendPszi, onSpendMana, onSkipTime, onClose }) {
  const [actionType, setActionType] = useState('SPEND_PSZI');
  const [amount, setAmount] = useState(1);
  const [timeAmount, setTimeAmount] = useState(1);
  const [timeMultiplier, setTimeMultiplier] = useState(1);
  const [activityType, setActivityType] = useState('DEEP_MEDITATION');
  const [maxPainPointsAffected, setMaxPainPointsAffected] = useState(false);
  const [warningMessage, setWarningMessage] = useState(null);
  const [isHardError, setIsHardError] = useState(false);
  const [pendingAction, setPendingAction] = useState(null);
  const [noTimeConfirm, setNoTimeConfirm] = useState(false);

  const getTimeSegments = () => Math.round(timeAmount * timeMultiplier);

  const handleSubmit = async () => {
    const timeSegments = getTimeSegments();

    if (timeSegments === 0 && (actionType === 'SPEND_PSZI' || actionType === 'SPEND_MANA') && !noTimeConfirm) {
      setNoTimeConfirm(true);
      return;
    }
    setNoTimeConfirm(false);

    if (actionType === 'SPEND_PSZI') {
      const result = await onSpendPszi(amount, timeSegments, false);
      if (result.warning) {
        setWarningMessage(result.message);
        setIsHardError(!!result.error);
        if (!result.error) {
          setPendingAction(() => async () => {
            await onSpendPszi(amount, timeSegments, true);
            onClose();
          });
        }
        return;
      }
    } else if (actionType === 'SPEND_MANA') {
      const result = await onSpendMana(amount, timeSegments, false);
      if (result.warning) {
        setWarningMessage(result.message);
        setIsHardError(!!result.error);
        if (!result.error) {
          setPendingAction(() => async () => {
            await onSpendMana(amount, timeSegments, true);
            onClose();
          });
        }
        return;
      }
    } else if (actionType === 'SKIP_TIME') {
      const result = await onSkipTime(timeSegments, activityType, maxPainPointsAffected);
      if (result && result.warning) {
        setWarningMessage(result.message);
        setIsHardError(!!result.error);
        return;
      }
    }

    onClose();
  };

  const handleForceConfirm = async () => {
    if (pendingAction) {
      await pendingAction();
    }
    setWarningMessage(null);
    setPendingAction(null);
  };

  return (
    <div className="dialog-overlay">
      <div className="dialog">
        <h2>Perform Action</h2>

        {warningMessage ? (
          <div className="warning-dialog">
            <p className="warning-text">⚠️ {warningMessage}</p>
            <div className="dialog-buttons">
              {!isHardError && (
                <button className="confirm-button" onClick={handleForceConfirm}>Yes, Proceed</button>
              )}
              <button className="cancel-button" onClick={() => {
                setWarningMessage(null);
                setIsHardError(false);
                setPendingAction(null);
              }}>{isHardError ? 'OK' : 'Cancel'}</button>
            </div>
          </div>
        ) : (
          <>
            <div className="action-type-select">
              <label>Action Type:</label>
              <select value={actionType} onChange={e => setActionType(e.target.value)}>
                <option value="SPEND_PSZI">Spend Pszi</option>
                <option value="SPEND_MANA">Spend Mana</option>
                <option value="SKIP_TIME">Skip Time</option>
              </select>
            </div>

            {(actionType === 'SPEND_PSZI' || actionType === 'SPEND_MANA') && (
              <div className="stat-row">
                <label>Amount:</label>
                <input
                  type="number"
                  min="1"
                  value={amount}
                  onChange={e => setAmount(Math.max(1, parseInt(e.target.value) || 1))}
                />
              </div>
            )}

            {actionType === 'SKIP_TIME' && (
              <>
                <div className="stat-row">
                  <label>Activity:</label>
                  <select value={activityType} onChange={e => setActivityType(e.target.value)}>
                    {ACTIVITY_TYPES.map(at => (
                      <option key={at.value} value={at.value}>{at.label}</option>
                    ))}
                  </select>
                </div>
                <div className="stat-row">
                  <label>
                    <input
                      type="checkbox"
                      checked={maxPainPointsAffected}
                      onChange={e => setMaxPainPointsAffected(e.target.checked)}
                    />
                    Max pain points affected
                  </label>
                </div>
              </>
            )}

            <div className="stat-row">
              <label>Time:</label>
              <input
                type="number"
                min="0"
                value={timeAmount}
                onChange={e => setTimeAmount(Math.max(0, parseFloat(e.target.value) || 0))}
                style={{ width: '80px' }}
              />
              <select
                value={timeMultiplier}
                onChange={e => setTimeMultiplier(Number(e.target.value))}
              >
                {TIME_UNITS.map(u => (
                  <option key={u.label} value={u.multiplier}>{u.label}</option>
                ))}
              </select>
              <span className="time-segments-preview">= {getTimeSegments()} seg</span>
            </div>

            {noTimeConfirm && (
              <p className="warning-text">No time specified. Proceed?</p>
            )}

            <div className="dialog-buttons">
              <button className="confirm-button" onClick={handleSubmit}>
                {noTimeConfirm ? 'Yes, Proceed' : 'Perform'}
              </button>
              <button className="cancel-button" onClick={noTimeConfirm ? () => setNoTimeConfirm(false) : onClose}>
                Cancel
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default ActionDialog;
