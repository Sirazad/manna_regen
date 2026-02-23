import { render, screen } from '@testing-library/react';
import App from './App';

test('renders main heading', () => {
  render(<App />);
  const heading = screen.getByText(/Manna Regeneration Tracker/i);
  expect(heading).toBeInTheDocument();
});

test('renders perform action button', () => {
  render(<App />);
  const button = screen.getByText(/Perform Action/i);
  expect(button).toBeInTheDocument();
});

test('renders character stats panel', () => {
  render(<App />);
  expect(screen.getByText(/Character Stats/i)).toBeInTheDocument();
  expect(screen.getByText(/Max Manna/i)).toBeInTheDocument();
  expect(screen.getByText(/Max Pszi/i)).toBeInTheDocument();
  expect(screen.getByText(/Level/i)).toBeInTheDocument();
  expect(screen.getByText(/Stamina/i)).toBeInTheDocument();
});

test('renders time display', () => {
  render(<App />);
  expect(screen.getByText(/Current Time/i)).toBeInTheDocument();
});

test('renders magic exhaustion limit', () => {
  render(<App />);
  expect(screen.getByText(/Magic Exhaustion Limit/i)).toBeInTheDocument();
});
