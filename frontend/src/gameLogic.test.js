import { test, expect } from 'vitest';
import {
  formatTime,
  parseTimeToSegments,
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
