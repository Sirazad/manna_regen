import React, { useState } from 'react';
import { ACTIVITY_TYPES, parseTimeToSegments } from '../gameLogic';

function ActionDialog({ state, onSpendPszi, onSpendMana, onSkipTime, onClose }) {
  const [actionType, setActionType] = useState('SPEND_PSZI');
  const [amount, setAmount] = useState(1);
  const [timeDays, setTimeDays] = useState(0);
  const [timeHours, setTimeHours] = useState(0);
  const [timeMinutes, setTimeMinutes] = useState(0);
  const [timeSeconds, setTimeSeconds] = useState(0);
  const [activityType, setActivityType] = useState('DEEP_MEDITATION');
  const [maxPainPointsAffected, setMaxPainPointsAffected] = useState(false);
  const [warningMessage, setWarningMessage] = useState(null);
  const [pendingAction, setPendingAction] = useState(null);

  const getTimeSegments = () =>
    parseTimeToSegments({ days: timeDays, hours: timeHours, minutes: timeMinutes, seconds: timeSeconds });

  const handleSubmit = async () => {
    const timeSegments = getTimeSegments();

    if (actionType === 'SPEND_PSZI') {
      const result = await onSpendPszi(amount, timeSegments, false);
      if (result.warning) {
        setWarningMessage(result.message);
        setPendingAction(() => async () => {
          await onSpendPszi(amount, timeSegments, true);
          onClose();
        });
        return;
      }
    } else if (actionType === 'SPEND_MANA') {
      const result = await onSpendMana(amount, timeSegments, false);
      if (result.warning) {
        setWarningMessage(result.message);
        setPendingAction(() => async () => {
          await onSpendMana(amount, timeSegments, true);
          onClose();
        });
        return;
      }
    } else if (actionType === 'SKIP_TIME') {
      await onSkipTime(timeSegments, activityType, maxPainPointsAffected);
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
              <button className="confirm-button" onClick={handleForceConfirm}>Yes, Proceed</button>
              <button className="cancel-button" onClick={() => {
                setWarningMessage(null);
                setPendingAction(null);
              }}>Cancel</button>
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

            <div className="time-input-group">
              <label>Time:</label>
              <div className="time-inputs">
                <div>
                  <input type="number" min="0" value={timeDays} onChange={e => setTimeDays(Math.max(0, parseInt(e.target.value) || 0))} />
                  <span>days</span>
                </div>
                <div>
                  <input type="number" min="0" max="23" value={timeHours} onChange={e => setTimeHours(Math.max(0, parseInt(e.target.value) || 0))} />
                  <span>hours</span>
                </div>
                <div>
                  <input type="number" min="0" max="59" value={timeMinutes} onChange={e => setTimeMinutes(Math.max(0, parseInt(e.target.value) || 0))} />
                  <span>minutes</span>
                </div>
                <div>
                  <input type="number" min="0" max="59" value={timeSeconds} onChange={e => setTimeSeconds(Math.max(0, parseInt(e.target.value) || 0))} />
                  <span>seconds</span>
                </div>
              </div>
            </div>

            <div className="dialog-buttons">
              <button className="confirm-button" onClick={handleSubmit}>Perform</button>
              <button className="cancel-button" onClick={onClose}>Cancel</button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default ActionDialog;
