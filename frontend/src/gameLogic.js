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
 * Pszi fill times in minutes for each activity.
 */
const PSZI_FILL_MINUTES = {
  DEEP_MEDITATION: 30,
  SLEEP: 150,
  SITTING: 320,
  COMFORTABLE_WALK: 675,
  SPEED_RIDING_FIGHT: 960,
};

/**
 * Calculate pszi regenerated during skip time.
 */
export function calculatePsziRegen(maxPszi, timeSegments, activityType) {
  if (maxPszi <= 0) return 0;
  const totalMinutes = PSZI_FILL_MINUTES[activityType];
  const regenPerMinute = maxPszi / totalMinutes;
  const elapsedMinutes = timeSegments / 60;
  return regenPerMinute * elapsedMinutes;
}

/**
 * Get magic exhaustion recovery rate per minute based on stamina and level.
 */
export function getMagicExhaustionRecoveryPerMinute(stamina, level) {
  if (stamina < 4) return 0;
  if (stamina <= 5) return level / 33;
  if (stamina <= 7) return level / 25;
  if (stamina <= 9) return level / 18;
  if (stamina === 10) return level / 12;
  if (stamina <= 12) return level / 10;
  if (stamina <= 14) return level / 7;
  if (stamina <= 16) return level / 5;
  if (stamina <= 18) return level / 3;
  if (stamina <= 20) return level;
  if (stamina <= 22) return level * 2;
  if (stamina <= 24) return level * 3;
  if (stamina <= 26) return level * 4;
  return level * 4;
}

/**
 * Calculate magic exhaustion recovery during skip time.
 */
export function calculateMagicExhaustionRecovery(stamina, level, timeSegments, activityType, maxPainPointsAffected) {
  if (activityType === 'SPEED_RIDING_FIGHT') return 0;
  if (maxPainPointsAffected) return 0;
  const rate = getMagicExhaustionRecoveryPerMinute(stamina, level);
  const elapsedMinutes = timeSegments / 60;
  return rate * elapsedMinutes;
}

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
