# manna_regen
Magus manna regeneráció, pihenési idő, mágikus kifáradás

## Calculation Rules

### Time
- Time is tracked in **segments** (1 segment = 1 second).
- 2 segments = **half round**; 4 segments = 1 **round** (4 seconds).
- Actions that consume manna/pszi advance the clock by the specified duration.

### Magic Exhaustion Limit
- **Maximum** = `25 × level`
- Spending manna or pszi reduces the limit by the amount spent.
- The limit can go negative (with confirmation).
- Recovery happens during Skip Time (see below).

### Spend Pszi / Spend Mana
- `currentPszi -= amount` (floored at 0)
- `currentManna -= amount` (floored at 0)
- `magicExhaustionLimit -= amount`
- Time advances by the specified duration.
- If the limit would drop below 0, a warning is shown; the player can force through.

### Skip Time (Pszi Regeneration)
Pszi regenerates linearly from 0 to max over a fixed duration that depends on activity:

| Activity            | Time to fully refill pszi |
|---------------------|--------------------------|
| Deep Meditation     | 30 minutes               |
| Sleep               | 2 h 30 min               |
| Sitting             | 5 h 20 min               |
| Comfortable Walk    | 11 h 15 min              |
| Speed Riding/Fight  | 16 hours                 |

Formula: `psziRegen = (maxPszi / fillMinutes) × elapsedMinutes`

Current pszi is capped at maxPszi.

### Resting Period

After spending pszi or mana, a resting period begins from the **1st segment of the action** during which no further pszi or mana can be spent.

The length depends on what percentage of the maximum was spent:

| Spent % of max | Resting period         |
|----------------|------------------------|
| [1–25]%        | 1 segment              |
| ]25–75]%       | 2 segments             |
| ]75–100]%      | 3 segments             |
| 100%+          | 1 minute (60 segments) |

**Example:** A 5-segment cast with a 3-segment resting period — resting starts at segment 1 of the cast, so by the time the cast ends (segment 5), the rest is already over. With a 5-segment rest, there is 1 segment of rest remaining after the cast ends.

Attempting to spend pszi or mana while resting is a hard error — the action is blocked until the resting period ends.

### Skip Time (Magic Exhaustion Recovery)
Recovery **does not apply** during Speed Riding/Fight or when max pain points are affected.

Otherwise, recovery per minute = `level × multiplier`, where the multiplier depends on stamina:

| Stamina | Rate per minute       |
|---------|-----------------------|
| ≤ 3     | 0 (no recovery)       |
| 4–5     | level ÷ 33            |
| 6–7     | level ÷ 25            |
| 8–9     | level ÷ 18            |
| 10      | level ÷ 12            |
| 11–12   | level ÷ 10            |
| 13–14   | level ÷ 7             |
| 15–16   | level ÷ 5             |
| 17–18   | level ÷ 3             |
| 19–20   | level × 1             |
| 21–22   | level × 2             |
| 23–24   | level × 3             |
| 25+     | level × 4             |

`exhaustionRecovered = ratePerMinute × elapsedMinutes`

Magic exhaustion limit is capped at the maximum (`25 × level`).
