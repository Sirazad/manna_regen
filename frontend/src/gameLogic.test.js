import { test, expect } from 'vitest';
import {
  formatTime,
  parseTimeToSegments,
  calculatePsziRegen,
  getMagicExhaustionRecoveryPerMinute,
  calculateMagicExhaustionRecovery,
} from './gameLogic';

// --- formatTime ---

test('formatTime returns 0 segments for 0', () => {
  expect(formatTime(0)).toBe('0 segments');
});

test('formatTime formats single segment', () => {
  expect(formatTime(1)).toBe('1 segment');
});

test('formatTime formats half-round (2 segments)', () => {
  expect(formatTime(2)).toBe('1 half-round');
});

test('formatTime formats 1 round (4 segments)', () => {
  expect(formatTime(4)).toBe('1 round');
});

test('formatTime formats complex time', () => {
  // 1 day + 2 hours + 3 minutes + 1 round + 1 half-round + 1 segment
  const segments = 86400 + 7200 + 180 + 4 + 2 + 1;
  expect(formatTime(segments)).toBe('1d 2h 3m 1 round 1 half-round 1 segment');
});

// --- parseTimeToSegments ---

test('parseTimeToSegments converts correctly', () => {
  expect(parseTimeToSegments({ days: 1, hours: 0, minutes: 0, seconds: 0 })).toBe(86400);
  expect(parseTimeToSegments({ days: 0, hours: 1, minutes: 0, seconds: 0 })).toBe(3600);
  expect(parseTimeToSegments({ days: 0, hours: 0, minutes: 1, seconds: 0 })).toBe(60);
  expect(parseTimeToSegments({ days: 0, hours: 0, minutes: 0, seconds: 1 })).toBe(1);
});

// --- calculatePsziRegen ---

test('deep meditation fills in 30 minutes', () => {
  // 30 max pszi, 30 minutes = 1800 segments
  const regen = calculatePsziRegen(30, 1800, 'DEEP_MEDITATION');
  expect(regen).toBeCloseTo(30, 1);
});

test('sleep fills in 2.5 hours', () => {
  const regen = calculatePsziRegen(30, 9000, 'SLEEP');
  expect(regen).toBeCloseTo(30, 1);
});

test('sitting fills in 5h20m', () => {
  const regen = calculatePsziRegen(30, 19200, 'SITTING');
  expect(regen).toBeCloseTo(30, 1);
});

test('walking fills in 11h15m', () => {
  const regen = calculatePsziRegen(30, 40500, 'COMFORTABLE_WALK');
  expect(regen).toBeCloseTo(30, 1);
});

test('fighting fills in 16h', () => {
  const regen = calculatePsziRegen(30, 57600, 'SPEED_RIDING_FIGHT');
  expect(regen).toBeCloseTo(30, 1);
});

test('partial regen for deep meditation 1 minute', () => {
  const regen = calculatePsziRegen(30, 60, 'DEEP_MEDITATION');
  expect(regen).toBeCloseTo(1, 1);
});

test('zero pszi returns zero regen', () => {
  expect(calculatePsziRegen(0, 1800, 'DEEP_MEDITATION')).toBe(0);
});

// --- getMagicExhaustionRecoveryPerMinute ---

test('no recovery for low stamina', () => {
  expect(getMagicExhaustionRecoveryPerMinute(2, 10)).toBe(0);
  expect(getMagicExhaustionRecoveryPerMinute(3, 10)).toBe(0);
});

test('stamina 4-5 recovery rate', () => {
  expect(getMagicExhaustionRecoveryPerMinute(4, 33)).toBeCloseTo(1, 2);
});

test('stamina 6-7 recovery rate', () => {
  expect(getMagicExhaustionRecoveryPerMinute(6, 25)).toBeCloseTo(1, 2);
});

test('stamina 19-20 recovery rate', () => {
  expect(getMagicExhaustionRecoveryPerMinute(19, 10)).toBeCloseTo(10, 2);
});

test('stamina 21-22 recovery rate', () => {
  expect(getMagicExhaustionRecoveryPerMinute(21, 10)).toBeCloseTo(20, 2);
});

// --- calculateMagicExhaustionRecovery ---

test('no recovery during fighting', () => {
  expect(calculateMagicExhaustionRecovery(10, 10, 600, 'SPEED_RIDING_FIGHT', false)).toBe(0);
});

test('no recovery when pain points affected', () => {
  expect(calculateMagicExhaustionRecovery(10, 10, 600, 'SLEEP', true)).toBe(0);
});

test('recovery during sleep', () => {
  // stamina 10, level 12, 10 minutes (600 segments)
  // rate = 12/12 = 1 per minute, 10 minutes = 10
  const recovery = calculateMagicExhaustionRecovery(10, 12, 600, 'SLEEP', false);
  expect(recovery).toBeCloseTo(10, 1);
});
