import React from 'react';
import { formatTime } from '../gameLogic';

function TimeDisplay({ currentTimeSegments, restingUntilSegments, onResetTimer }) {
  const isResting = restingUntilSegments > currentTimeSegments;
  const restingRemaining = restingUntilSegments - currentTimeSegments;

  return (
    <div className="time-display">
      <h2>Current Time</h2>
      <div className="time-value">{formatTime(currentTimeSegments)}</div>
      <div className="time-raw">({currentTimeSegments} segments total)</div>
      <button className="reset-timer-button" onClick={onResetTimer}>Reset Timer</button>
      {isResting && (
        <div className="resting-indicator">
          Resting: {restingRemaining} segment{restingRemaining !== 1 ? 's' : ''} remaining
        </div>
      )}
    </div>
  );
}

export default TimeDisplay;
