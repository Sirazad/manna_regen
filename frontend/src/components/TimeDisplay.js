import React from 'react';
import { formatTime } from '../gameLogic';

function TimeDisplay({ currentTimeSegments }) {
  return (
    <div className="time-display">
      <h2>Current Time</h2>
      <div className="time-value">{formatTime(currentTimeSegments)}</div>
      <div className="time-raw">({currentTimeSegments} segments total)</div>
    </div>
  );
}

export default TimeDisplay;
