import React, { useState } from 'react';
import { WIELDER_TYPES } from '../gameLogic';

function CharacterPanel({ state, maxMagicExhaustion, onUpdateField }) {
  const [mannaAdjustAmt, setMannaAdjustAmt] = useState(5);
  const [psziAdjustAmt, setPsziAdjustAmt] = useState(5);

  const adjustValue = (field, delta, min, max) => {
    const newVal = Math.max(min, Math.min(max, state[field] + delta));
    onUpdateField(field, newVal);
  };

  return (
    <div className="character-panel">
      <h2>Character Stats</h2>

      <div className="stat-group">
        <div className="stat-row">
          <label>Max Manna:</label>
          <input
            type="number"
            min="0"
            value={state.maxManna}
            onChange={e => onUpdateField('maxManna', Math.max(0, parseInt(e.target.value) || 0))}
          />
        </div>
        <div className="stat-row">
          <label>Current Manna:</label>
          <div className="adjust-controls">
            <button onClick={() => adjustValue('currentManna', -1, 0, state.maxManna)}>-1</button>
            <input
              type="number"
              min="0"
              max={state.maxManna}
              value={state.currentManna}
              onChange={e => {
                const val = Math.max(0, Math.min(state.maxManna, parseInt(e.target.value) || 0));
                onUpdateField('currentManna', val);
              }}
            />
            <button onClick={() => adjustValue('currentManna', 1, 0, state.maxManna)}>+1</button>
            <input
              type="number"
              className="adjust-amount"
              min="1"
              value={mannaAdjustAmt}
              onChange={e => setMannaAdjustAmt(Math.max(1, parseInt(e.target.value) || 1))}
              placeholder="amt"
            />
            <button onClick={() => adjustValue('currentManna', -mannaAdjustAmt, 0, state.maxManna)}>-</button>
            <button onClick={() => adjustValue('currentManna', mannaAdjustAmt, 0, state.maxManna)}>+</button>
          </div>
        </div>
      </div>

      <div className="stat-group">
        <div className="stat-row">
          <label>Max Pszi:</label>
          <input
            type="number"
            min="0"
            value={state.maxPszi}
            onChange={e => onUpdateField('maxPszi', Math.max(0, parseInt(e.target.value) || 0))}
          />
        </div>
        <div className="stat-row">
          <label>Current Pszi:</label>
          <div className="adjust-controls">
            <button onClick={() => adjustValue('currentPszi', -1, 0, state.maxPszi)}>-1</button>
            <input
              type="number"
              min="0"
              max={state.maxPszi}
              value={state.currentPszi}
              onChange={e => {
                const val = Math.max(0, Math.min(state.maxPszi, parseInt(e.target.value) || 0));
                onUpdateField('currentPszi', val);
              }}
            />
            <button onClick={() => adjustValue('currentPszi', 1, 0, state.maxPszi)}>+1</button>
            <input
              type="number"
              className="adjust-amount"
              min="1"
              value={psziAdjustAmt}
              onChange={e => setPsziAdjustAmt(Math.max(1, parseInt(e.target.value) || 1))}
              placeholder="amt"
            />
            <button onClick={() => adjustValue('currentPszi', -psziAdjustAmt, 0, state.maxPszi)}>-</button>
            <button onClick={() => adjustValue('currentPszi', psziAdjustAmt, 0, state.maxPszi)}>+</button>
          </div>
        </div>
      </div>

      <div className="stat-group">
        <div className="stat-row">
          <label>Magic Wielding Type:</label>
          <select
            value={state.wielderType}
            onChange={e => onUpdateField('wielderType', e.target.value)}
          >
            {WIELDER_TYPES.map(wt => (
              <option key={wt.value} value={wt.value}>{wt.label}</option>
            ))}
          </select>
        </div>

        <div className="stat-row">
          <label>Level:</label>
          <input
            type="number"
            step="0.1"
            min="0"
            value={state.level}
            onChange={e => onUpdateField('level', Math.max(0, parseFloat(e.target.value) || 0))}
          />
        </div>

        <div className="stat-row">
          <label>Stamina:</label>
          <input
            type="number"
            min="0"
            value={state.stamina}
            onChange={e => onUpdateField('stamina', Math.max(0, parseInt(e.target.value) || 0))}
          />
        </div>
      </div>
    </div>
  );
}

export default CharacterPanel;
