import test from 'node:test';
import assert from 'node:assert/strict';
import { createMockSvg, checkPrompt, makeSeedItems } from '../shared.mjs';
test('prompt validation',()=>{assert.equal(checkPrompt('').ok,false);assert.equal(checkPrompt('オリジナルの動物グッズ').ok,true);});
test('mock SVG output',()=>{const u=createMockSvg('かわいい動物','acrylic','soft');assert.ok(u.startsWith('data:image/svg+xml'));});
test('seed lineage integrity',()=>{const a=makeSeedItems();assert.ok(a.every(x=>!x.parentId||a.some(y=>y.id===x.parentId)));});
