const test = require('node:test');
const assert = require('node:assert/strict');

test('Firestore emulator status test', async () => {
  const res = await fetch('http://127.0.0.1:8085/');
  assert.ok(res.status === 404 || res.status === 200);
});
