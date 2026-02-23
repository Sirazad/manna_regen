/**
 * Format time in segments to a human-readable string.
 * 1 segment = 1 second, 2 segments = half-round, 4 segments = 1 round
 */
export function formatTime(totalSegments) {
  if (totalSegments === 0) return '0 segments';

  const days = Math.floor(totalSegments / 86400);
  let remaining = totalSegments % 86400;
  const hours = Math.floor(remaining / 3600);
  remaining = remaining % 3600;
  const minutes = Math.floor(remaining / 60);
  remaining = remaining % 60;
  const rounds = Math.floor(remaining / 4);
  remaining = remaining % 4;
  const halfRounds = Math.floor(remaining / 2);
  const segments = remaining % 2;

  const parts = [];
  if (days > 0) parts.push(`${days}d`);
  if (hours > 0) parts.push(`${hours}h`);
  if (minutes > 0) parts.push(`${minutes}m`);
  if (rounds > 0) parts.push(`${rounds} round${rounds > 1 ? 's' : ''}`);
  if (halfRounds > 0) parts.push(`${halfRounds} half-round`);
  if (segments > 0) parts.push(`${segments} segment${segments > 1 ? 's' : ''}`);

  return parts.join(' ') || '0 segments';
}

/**
 * Parse a time input into total segments.
 * Accepts: { days, hours, minutes, seconds }
 */
export function parseTimeToSegments({ days = 0, hours = 0, minutes = 0, seconds = 0 }) {
  return (days * 86400) + (hours * 3600) + (minutes * 60) + seconds;
}

/**
 * Wielder type options for the dropdown.
 */
export const WIELDER_TYPES = [
  { value: 'NONE', label: 'None' },
  { value: 'MAGIC_USER', label: 'Magic User' },
  { value: 'MAGE', label: 'Mage' },
  { value: 'WITCH', label: 'Witch' },
  { value: 'PRIEST', label: 'Priest' },
  { value: 'PALADIN', label: 'Paladin' },
  { value: 'BARD', label: 'Bard' },
];

/**
 * Activity type options for skip time.
 */
export const ACTIVITY_TYPES = [
  { value: 'DEEP_MEDITATION', label: 'Deep Meditation' },
  { value: 'SLEEP', label: 'Sleep' },
  { value: 'SITTING', label: 'Sitting' },
  { value: 'COMFORTABLE_WALK', label: 'Comfortable walk on foot or horse, light mental load' },
  { value: 'SPEED_RIDING_FIGHT', label: 'Speed riding on a horse, fight, high mental load' },
];

/**
 * Default initial game state.
 */
export function createInitialState() {
  return {
    maxManna: 0,
    currentManna: 0,
    maxPszi: 0,
    currentPszi: 0,
    wielderType: 'NONE',
    level: 1,
    stamina: 10,
    magicExhaustionLimit: 25,
    currentTimeSegments: 0,
  };
}

/**
 * Call the backend API to perform an action (with logging under character name).
 * All game calculations are done server-side.
 */
export async function performActionApi(characterName, actionRequest) {
  const url = characterName
    ? `/api/action/${encodeURIComponent(characterName)}`
    : '/api/action';
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(actionRequest),
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  return response.json();
}

/**
 * Save a game session under a character name.
 */
export async function saveSessionApi(characterName, currentState) {
  const response = await fetch('/api/session/save', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ characterName, currentState }),
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  return response.json();
}

/**
 * List all saved sessions.
 */
export async function listSessionsApi() {
  const response = await fetch('/api/session/list');
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  return response.json();
}

/**
 * Load a saved session by its ID.
 */
export async function loadSessionApi(id) {
  const response = await fetch(`/api/session/${id}`);
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  return response.json();
}

/**
 * Get action history for a character.
 */
export async function getHistoryApi(characterName) {
  const response = await fetch(`/api/history/${encodeURIComponent(characterName)}`);
  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }
  return response.json();
}
