import React, { useState, useEffect } from 'react';
import {
  listCharactersApi,
  listDeletedCharactersApi,
  createCharacterApi,
  updateCharacterApi,
  deleteCharacterApi,
  restoreCharacterApi,
  loadCharacterApi,
  WIELDER_TYPES,
} from '../gameLogic';

const EMPTY_FORM = {
  name: '',
  maxManna: 0,
  maxPszi: 0,
  wielderType: 'NONE',
  level: 1,
  stamina: 10,
};

function CharacterManagerPanel({ onLoad, onClose }) {
  const [characters, setCharacters] = useState([]);
  const [deleted, setDeleted] = useState([]);
  const [showDeleted, setShowDeleted] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [pendingDeleteId, setPendingDeleteId] = useState(null);

  useEffect(() => {
    refresh();
  }, []);

  const refresh = async () => {
    const [active, del] = await Promise.all([
      listCharactersApi(),
      listDeletedCharactersApi(),
    ]);
    setCharacters(active);
    setDeleted(del);
  };

  const handleFormChange = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = async () => {
    if (!form.name.trim()) return;
    if (editingId) {
      await updateCharacterApi(editingId, form);
    } else {
      await createCharacterApi(form);
    }
    setForm(EMPTY_FORM);
    setEditingId(null);
    await refresh();
  };

  const handleEdit = (character) => {
    setEditingId(character.id);
    setForm({
      name: character.name,
      maxManna: character.maxManna,
      maxPszi: character.maxPszi,
      wielderType: character.wielderType || 'NONE',
      level: character.level,
      stamina: character.stamina,
    });
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
  };

  const handleDeleteConfirm = async () => {
    await deleteCharacterApi(pendingDeleteId);
    setPendingDeleteId(null);
    await refresh();
  };

  const handleRestore = async (id) => {
    await restoreCharacterApi(id);
    await refresh();
  };

  const handleLoad = async (character) => {
    const state = await loadCharacterApi(character.id);
    onLoad(character.name, state);
  };

  return (
    <div className="dialog-overlay">
      <div className="dialog character-manager-dialog">
        <h2>Characters</h2>

        {pendingDeleteId && (
          <div className="warning-dialog" style={{ marginBottom: '12px' }}>
            <p className="warning-text">Move this character to deleted?</p>
            <div className="dialog-buttons">
              <button className="confirm-button" onClick={handleDeleteConfirm}>Yes</button>
              <button className="cancel-button" onClick={() => setPendingDeleteId(null)}>Cancel</button>
            </div>
          </div>
        )}

        {/* Active characters */}
        {characters.length === 0 ? (
          <p>No characters yet. Create one below.</p>
        ) : (
          <table className="history-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Level</th>
                <th>Manna</th>
                <th>Pszi</th>
                <th>Type</th>
                <th>Stamina</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {characters.map(c => (
                <tr key={c.id}>
                  <td>{c.name}</td>
                  <td>{c.level}</td>
                  <td>{c.maxManna}</td>
                  <td>{c.maxPszi}</td>
                  <td>{c.wielderType}</td>
                  <td>{c.stamina}</td>
                  <td style={{ whiteSpace: 'nowrap' }}>
                    <button className="confirm-button" style={{ marginRight: '4px', padding: '4px 10px', fontSize: '12px' }}
                      onClick={() => handleLoad(c)}>Load</button>
                    <button className="load-session-button" style={{ marginRight: '4px' }}
                      onClick={() => handleEdit(c)}>Edit</button>
                    <button className="delete-button"
                      onClick={() => setPendingDeleteId(c.id)}>X</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {/* Create / Edit form */}
        <div className="character-form">
          <h3>{editingId ? 'Edit Character' : 'New Character'}</h3>
          <div className="character-form-grid">
            <div className="stat-row">
              <label>Name:</label>
              <input type="text" value={form.name}
                onChange={e => handleFormChange('name', e.target.value)} />
            </div>
            <div className="stat-row">
              <label>Max Manna:</label>
              <input type="number" min="0" value={form.maxManna}
                onChange={e => handleFormChange('maxManna', Math.max(0, parseInt(e.target.value) || 0))} />
            </div>
            <div className="stat-row">
              <label>Max Pszi:</label>
              <input type="number" min="0" value={form.maxPszi}
                onChange={e => handleFormChange('maxPszi', Math.max(0, parseInt(e.target.value) || 0))} />
            </div>
            <div className="stat-row">
              <label>Wielder Type:</label>
              <select value={form.wielderType}
                onChange={e => handleFormChange('wielderType', e.target.value)}>
                {WIELDER_TYPES.map(wt => (
                  <option key={wt.value} value={wt.value}>{wt.label}</option>
                ))}
              </select>
            </div>
            <div className="stat-row">
              <label>Level:</label>
              <input type="number" step="0.1" min="0" value={form.level}
                onChange={e => handleFormChange('level', Math.max(0, parseFloat(e.target.value) || 0))} />
            </div>
            <div className="stat-row">
              <label>Stamina:</label>
              <input type="number" min="0" value={form.stamina}
                onChange={e => handleFormChange('stamina', Math.max(0, parseInt(e.target.value) || 0))} />
            </div>
          </div>
          <div className="dialog-buttons" style={{ justifyContent: 'flex-start', marginTop: '8px' }}>
            <button className="confirm-button" onClick={handleSave}>
              {editingId ? 'Update' : 'Create'}
            </button>
            {editingId && (
              <button className="cancel-button" onClick={handleCancelEdit}>Cancel</button>
            )}
          </div>
        </div>

        {/* Deleted characters */}
        {deleted.length > 0 && (
          <div style={{ marginTop: '16px' }}>
            <button className="load-session-button"
              onClick={() => setShowDeleted(v => !v)}>
              {showDeleted ? 'Hide' : 'Show'} Deleted Characters ({deleted.length})
            </button>
            {showDeleted && (
              <table className="history-table" style={{ marginTop: '8px' }}>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Level</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {deleted.map(c => (
                    <tr key={c.id}>
                      <td>{c.name}</td>
                      <td>{c.level}</td>
                      <td>
                        <button className="load-session-button"
                          onClick={() => handleRestore(c.id)}>Restore</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        <div className="dialog-buttons" style={{ marginTop: '16px' }}>
          <button className="cancel-button" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}

export default CharacterManagerPanel;
