import React from 'react';
import { WIELDER_TYPES } from '../gameLogic';

function CharacterPanel({ state, maxMagicExhaustion, onUpdateField }) {
  return (
    <div className="character-panel">
      <h2>Character Stats</h2>

      <div className="stat-group">
        <div className="stat-pair-row">
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
            <span className="current-value">{state.currentManna}</span>
          </div>
        </div>
        <div className="stat-pair-row">
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
            <span className="current-value">{state.currentPszi}</span>
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
