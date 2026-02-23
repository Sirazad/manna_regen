/**
 * Manna Regen – Frontend Application
 *
 * Communicates with the Spring Boot backend via REST API.
 * All game logic lives in the backend; this script handles UI rendering
 * and user interactions.
 */

const API = '/api';

// ----------------------------------------------------------------
// State
// ----------------------------------------------------------------
let character = null;
// Stores a pending forced action (when magic exhaustion warning was shown)
let pendingForceAction = null;

// ----------------------------------------------------------------
// Boot
// ----------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    loadCharacter();
    document.getElementById('resetBtn').addEventListener('click', resetCharacter);
});

// ----------------------------------------------------------------
// API helpers
// ----------------------------------------------------------------
async function apiGet(path) {
    const res = await fetch(API + path);
    if (!res.ok) throw new Error(await res.text());
    return res.json();
}

async function apiPost(path, body) {
    const res = await fetch(API + path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
}

async function apiPatch(path, body) {
    const res = await fetch(API + path, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
}

// ----------------------------------------------------------------
// Load & render
// ----------------------------------------------------------------
async function loadCharacter() {
    try {
        character = await apiGet('/character');
        renderAll();
    } catch (e) {
        notify('Hiba a karakter betöltésekor: ' + e.message, 'error');
    }
}

function renderAll() {
    if (!character) return;
    renderCharacterFields();
    renderResources();
    renderTime();
}

function renderCharacterFields() {
    setValue('magicWieldingType', character.magicWieldingType);
    setValue('level', character.level);
    setValue('stamina', character.stamina);
    setContent('magicExhaustionLimit', fmt(character.magicExhaustionLimit));
}

function renderResources() {
    setValue('maxManna', character.maxManna);
    setContent('currentManna', fmt(character.currentManna));

    setValue('maxPszi', character.maxPszi);
    setContent('currentPszi', fmt(character.currentPszi));

    const exhaustion = character.magicExhaustion;
    const limit = character.magicExhaustionLimit;
    setContent('currentExhaustion', fmt(exhaustion));

    const fill = document.getElementById('exhaustionBarFill');
    if (exhaustion < 0) {
        fill.style.width = '0%';
        fill.classList.add('negative');
    } else {
        fill.classList.remove('negative');
        fill.style.width = limit > 0 ? Math.min(100, (exhaustion / limit) * 100) + '%' : '0%';
    }
}

function renderTime() {
    const seg = character.currentTimeSegments;
    const { days, hours, minutes, seconds } = segmentsToTime(seg);
    setContent('timeDays', days);
    setContent('timeHours', pad(hours));
    setContent('timeMinutes', pad(minutes));
    setContent('timeSeconds', pad(seconds));
    setContent('totalSegments', seg);
}

// ----------------------------------------------------------------
// Character settings
// ----------------------------------------------------------------
async function applyLevel() {
    const val = parseFloat(document.getElementById('level').value);
    if (isNaN(val) || val <= 0) { notify('Érvénytelen szint érték.', 'error'); return; }
    await patchCharacter({ level: val });
}

async function applyStamina() {
    const val = parseInt(document.getElementById('stamina').value);
    if (isNaN(val) || val < 1) { notify('Érvénytelen állóképesség érték.', 'error'); return; }
    await patchCharacter({ stamina: val });
}

async function applyMaxManna() {
    const val = parseFloat(document.getElementById('maxManna').value);
    if (isNaN(val) || val < 0) { notify('Érvénytelen maximum manna.', 'error'); return; }
    await patchCharacter({ maxManna: val });
}

async function applyMaxPszi() {
    const val = parseFloat(document.getElementById('maxPszi').value);
    if (isNaN(val) || val < 0) { notify('Érvénytelen maximum pszi.', 'error'); return; }
    await patchCharacter({ maxPszi: val });
}

document.addEventListener('change', (e) => {
    if (e.target.id === 'magicWieldingType') {
        patchCharacter({ magicWieldingType: e.target.value });
    }
});

async function patchCharacter(fields) {
    try {
        character = await apiPatch('/character', fields);
        renderAll();
        notify('Frissítve!', 'success');
    } catch (e) {
        notify('Hiba: ' + e.message, 'error');
    }
}

async function resetCharacter() {
    if (!confirm('Biztosan visszaállítod az alapértékeket?')) return;
    try {
        character = await apiPost('/character/reset', {});
        renderAll();
        notify('Karakter visszaállítva!', 'success');
    } catch (e) {
        notify('Hiba: ' + e.message, 'error');
    }
}

// ----------------------------------------------------------------
// Manual manna/pszi adjustments
// ----------------------------------------------------------------
async function adjustManna(delta) {
    await patchCharacter({ currentManna: character.currentManna + delta });
}

async function adjustPszi(delta) {
    await patchCharacter({ currentPszi: character.currentPszi + delta });
}

async function adjustMannaByAmount(sign) {
    const val = parseFloat(document.getElementById('mannaAdjustAmt').value);
    if (isNaN(val) || val <= 0) { notify('Adj meg egy érvényes mennyiséget!', 'error'); return; }
    await patchCharacter({ currentManna: character.currentManna + sign * val });
}

async function adjustPsziByAmount(sign) {
    const val = parseFloat(document.getElementById('psziAdjustAmt').value);
    if (isNaN(val) || val <= 0) { notify('Adj meg egy érvényes mennyiséget!', 'error'); return; }
    await patchCharacter({ currentPszi: character.currentPszi + sign * val });
}

// ----------------------------------------------------------------
// Modal management
// ----------------------------------------------------------------
function openActionModal() {
    document.getElementById('actionModal').classList.add('open');
}

function closeActionModal() {
    document.getElementById('actionModal').classList.remove('open');
}

function closeConfirmModal() {
    document.getElementById('confirmModal').classList.remove('open');
    pendingForceAction = null;
}

function selectTab(name) {
    document.querySelectorAll('.tab-btn').forEach((btn, i) => {
        btn.classList.toggle('active', btn.getAttribute('onclick') === `selectTab('${name}')`);
    });
    document.querySelectorAll('.tab-content').forEach(tc => {
        tc.classList.toggle('active', tc.id === 'tab-' + name);
    });
}

// ----------------------------------------------------------------
// Time helpers
// ----------------------------------------------------------------
function getTimeSegmentsFromInputs(prefix) {
    // If segments field is filled, use it directly
    const segInput = document.getElementById(prefix + 'Segments');
    if (segInput && segInput.value !== '' && parseInt(segInput.value) > 0) {
        return parseInt(segInput.value);
    }
    const days = parseInt(document.getElementById(prefix + 'Days').value) || 0;
    const hours = parseInt(document.getElementById(prefix + 'Hours').value) || 0;
    const minutes = parseInt(document.getElementById(prefix + 'Minutes').value) || 0;
    const seconds = parseInt(document.getElementById(prefix + 'Seconds').value) || 0;
    return days * 86400 + hours * 3600 + minutes * 60 + seconds;
}

function segmentsToTime(seg) {
    const seconds = seg % 60;
    const totalMinutes = Math.floor(seg / 60);
    const minutes = totalMinutes % 60;
    const totalHours = Math.floor(totalMinutes / 60);
    const hours = totalHours % 24;
    const days = Math.floor(totalHours / 24);
    return { days, hours, minutes, seconds };
}

// ----------------------------------------------------------------
// Actions
// ----------------------------------------------------------------
async function doSpendPszi(force = false) {
    const amount = parseFloat(document.getElementById('psziSpendAmount').value);
    if (isNaN(amount) || amount <= 0) { notify('Adj meg egy érvényes pszi mennyiséget!', 'error'); return; }
    const timeSegments = getTimeSegmentsFromInputs('psziSpend');
    if (timeSegments <= 0) { notify('Adj meg egy érvényes időtartamot!', 'error'); return; }

    try {
        const response = await apiPost('/action/spend-pszi', { amount, timeSegments, force });
        handleActionResponse(response, () => doSpendPszi(true));
    } catch (e) {
        notify('Hiba: ' + e.message, 'error');
    }
}

async function doSpendMana(force = false) {
    const amount = parseFloat(document.getElementById('manaSpendAmount').value);
    if (isNaN(amount) || amount <= 0) { notify('Adj meg egy érvényes manna mennyiséget!', 'error'); return; }
    const timeSegments = getTimeSegmentsFromInputs('manaSpend');
    if (timeSegments <= 0) { notify('Adj meg egy érvényes időtartamot!', 'error'); return; }

    try {
        const response = await apiPost('/action/spend-mana', { amount, timeSegments, force });
        handleActionResponse(response, () => doSpendMana(true));
    } catch (e) {
        notify('Hiba: ' + e.message, 'error');
    }
}

async function doSkipTime() {
    const activity = document.getElementById('skipActivity').value;
    const timeSegments = getTimeSegmentsFromInputs('skip');
    if (timeSegments <= 0) { notify('Adj meg egy érvényes időtartamot!', 'error'); return; }
    const maxPainPointsAffected = document.getElementById('maxPainPointsAffected').checked;

    try {
        const response = await apiPost('/action/skip-time', { timeSegments, activity, maxPainPointsAffected });
        handleActionResponse(response, null);
    } catch (e) {
        notify('Hiba: ' + e.message, 'error');
    }
}

function handleActionResponse(response, forcedAction) {
    if (response.requiresConfirmation && response.warning) {
        // Store the forced action callback
        pendingForceAction = forcedAction;
        document.getElementById('confirmMessage').textContent = response.warning;
        document.getElementById('confirmModal').classList.add('open');
    } else {
        character = response.character;
        renderAll();
        closeActionModal();
        notify('Akció végrehajtva!', 'success');
    }
}

function confirmAction() {
    closeConfirmModal();
    if (pendingForceAction) {
        pendingForceAction();
        pendingForceAction = null;
    }
}

// ----------------------------------------------------------------
// UI utilities
// ----------------------------------------------------------------
function fmt(val) {
    if (val === null || val === undefined) return '–';
    return Number(val).toFixed(2);
}

function pad(n) {
    return String(n).padStart(2, '0');
}

function setValue(id, val) {
    const el = document.getElementById(id);
    if (el) el.value = val;
}

function setContent(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}

let notificationTimer = null;
function notify(msg, type = 'info') {
    let el = document.getElementById('notification');
    if (!el) {
        el = document.createElement('div');
        el.id = 'notification';
        document.body.appendChild(el);
    }
    el.textContent = msg;
    el.className = type;
    el.style.opacity = '1';
    if (notificationTimer) clearTimeout(notificationTimer);
    notificationTimer = setTimeout(() => { el.style.opacity = '0'; }, 3000);
}
