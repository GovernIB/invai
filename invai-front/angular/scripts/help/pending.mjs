/**
 * Which help topics may have fallen behind the code: the commits of src/app after the last one the
 * help was checked against (revision.json), grouped by the topic each changed file belongs to.
 * Files that belong to no topic are listed apart: they may be a screen the help does not cover.
 *
 *   npm run help:pending              commits still to review
 *   npm run help:pending -- --mark    marks the help as reviewed up to HEAD
 *
 * A commit needs a change in the help when it changes something a user sees: a screen or a menu
 * option, a column or a filter, a field or whether it is required, an action, a tab or the
 * permissions. Services, types, styles and tests do not.
 */
import { execFileSync } from 'node:child_process';
import { readFileSync, writeFileSync } from 'node:fs';

const file = new URL('./revision.json', import.meta.url);
const revision = JSON.parse(readFileSync(file, 'utf8'));
const git = (...args) => execFileSync('git', args, { encoding: 'utf8' }).trim();

if (process.argv.includes('--mark')) {
  revision.commit = git('rev-parse', '--short', 'HEAD');
  revision.date = git('log', '-1', '--format=%ad', '--date=short');
  writeFileSync(file, `${JSON.stringify(revision, null, 2)}\n`);
  console.log(`Ayuda revisada hasta ${revision.commit} (${revision.date}).`);
  process.exit(0);
}

const owners = (path) =>
  Object.entries(revision.documents)
    .filter(([, sources]) => sources.some((source) => path.startsWith(source)))
    .map(([document]) => document);

// Specs and their test helpers (`*.testing.ts`) never change what a user sees.
const isRelevant = (path) =>
  !/\.(spec|testing)\.ts$/.test(path) &&
  !revision.ignore.some((prefix) => path.startsWith(prefix));

const log = git(
  'log',
  '--format=@@%h %ad %s',
  '--date=short',
  '--name-only',
  `${revision.commit}..HEAD`,
  '--',
  'src/app',
);

/** Commits by topic, and files without a topic by commit. */
const byDocument = new Map();
const unowned = new Map();
const add = (map, key, value) => map.set(key, [...new Set([...(map.get(key) ?? []), value])]);

let commit = '';
for (const line of log.split('\n').filter(Boolean)) {
  if (line.startsWith('@@')) {
    commit = line.slice(2);
  } else if (isRelevant(line)) {
    const documents = owners(line);
    documents.forEach((document) => add(byDocument, document, commit));
    if (!documents.length) add(unowned, commit, line);
  }
}

console.log(`Ayuda revisada hasta ${revision.commit} (${revision.date}).`);
if (!byDocument.size && !unowned.size) console.log('No hay cambios en src/app que revisar.');
for (const [document, commits] of [...byDocument].sort(([a], [b]) => a.localeCompare(b))) {
  console.log(`\n${document}`);
  commits.forEach((line) => console.log(`  ${line}`));
}
if (unowned.size) {
  console.log('\nSin tema (¿pantalla nueva o sin documentar?)');
  for (const [line, files] of unowned) {
    console.log(`  ${line}`);
    files.forEach((path) => console.log(`    ${path}`));
  }
}
