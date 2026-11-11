/**
 * Checks that the help in public/help follows docs/help-center/TEMPLATE.md:
 *
 * - both languages have the same index (same documents, groups and icons, in the same order);
 * - no sibling repeats an icon;
 * - every document of the index exists and starts with `###` and its label;
 * - no file is left out of the index and no image is left out of its documents;
 * - every image has alternative text, exists and weighs 70 KB at most (40 KB or less is the aim);
 * - every link to another topic or heading points to one that exists;
 * - there are no HTML comments, `#`/`##` headings, `---` separators or emojis.
 *
 *   npm run help:check              checks public/help
 *   npm run help:check -- --root d  checks another copy (to try the checker on broken content)
 *
 * Errors make it exit with 1; warnings are printed but do not fail.
 */
import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import path from 'node:path';

const LOCALES = ['ca', 'es'];
const MAX_IMAGE_BYTES = 70 * 1024;
const TARGET_IMAGE_BYTES = 40 * 1024;

const rootIndex = process.argv.indexOf('--root');
const ROOT = path.resolve(rootIndex === -1 ? 'public/help' : process.argv[rootIndex + 1]);

const errors = [];
const warnings = [];
const error = (where, message) => errors.push(`${where}: ${message}`);
const warn = (where, message) => warnings.push(`${where}: ${message}`);

/** Anchor of a heading as GitHub and the help (`headingAnchor`) write it. */
const anchorOf = (text) =>
  text
    .normalize('NFD')
    .replace(/\p{M}/gu, '')
    .toLowerCase()
    .replace(/[^\p{L}\p{N}\s-]/gu, '')
    .trim()
    .replace(/\s+/g, '-');

const relative = (file) => path.relative(ROOT, file).replaceAll('\\', '/');

function filesUnder(dir) {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const full = path.join(dir, entry.name);
    return entry.isDirectory() ? filesUnder(full) : [full];
  });
}

function readIndex(locale) {
  const file = path.join(ROOT, locale, 'index.json');
  try {
    return JSON.parse(readFileSync(file, 'utf8'));
  } catch (cause) {
    error(relative(file), `no es un JSON válido (${cause.message})`);
    return [];
  }
}

/** Documents of an index with their label, in order. */
function documentsOf(topics) {
  return topics.flatMap((topic) => [
    ...(topic.document ? [{ document: topic.document, label: topic.label }] : []),
    ...documentsOf(topic.items ?? []),
  ]);
}

function checkIndexShape(ca, es, where = 'index.json') {
  if (ca.length !== es.length) {
    error(where, `ca tiene ${ca.length} entradas y es ${es.length}`);
    return;
  }
  ca.forEach((left, position) => {
    const right = es[position];
    const at = `${where} › ${left.label}`;
    for (const key of ['document', 'icon']) {
      if (left[key] !== right[key])
        error(at, `${key} distinto en ca («${left[key]}») y es («${right[key]}»)`);
    }
    if (!!left.items !== !!right.items) error(at, 'en un idioma es un grupo y en el otro no');
    if (left.items && right.items) checkIndexShape(left.items, right.items, at);
  });
}

function checkIcons(topics, locale, where = `${locale}/index.json`) {
  const seen = new Map();
  for (const topic of topics) {
    const at = `${where} › ${topic.label}`;
    for (const key of Object.keys(topic)) {
      if (!['label', 'icon', 'document', 'items'].includes(key))
        error(at, `clave desconocida «${key}»`);
    }
    if (!topic.document && !topic.items) error(at, 'no tiene ni documento ni temas');
    if (topic.icon && !/^pi pi-[a-z0-9-]+$/.test(topic.icon))
      error(at, `icono no válido «${topic.icon}»`);
    if (topic.icon && seen.has(topic.icon)) {
      error(at, `repite el icono ${topic.icon} de «${seen.get(topic.icon)}»`);
    }
    if (topic.icon) seen.set(topic.icon, topic.label);
    if (topic.items) checkIcons(topic.items, locale, at);
  }
}

/** Headings of a Markdown text, with their level, text and anchor. */
function headingsOf(markdown) {
  return [...markdown.matchAll(/^(#{1,6})[ \t]+(.+?)[ \t]*#*[ \t]*$/gm)].map(([, marks, text]) => ({
    level: marks.length,
    text,
    anchor: anchorOf(text),
  }));
}

function checkDocument(locale, { document, label }, imagesUsed) {
  const file = path.join(ROOT, locale, document);
  const where = `${locale}/${document}`;
  if (!existsSync(file)) {
    error(where, 'el índice lo cita pero no existe');
    return;
  }
  const markdown = readFileSync(file, 'utf8');
  // Code is literal text, not structure.
  const prose = markdown.replace(/```[\s\S]*?```/g, '').replace(/`[^`\n]*`/g, '');
  const headings = headingsOf(prose);

  const first = prose.split('\n').find((line) => line.trim());
  if (first !== `### ${label}`)
    error(where, `debe empezar con «### ${label}», empieza con «${first}»`);
  for (const heading of headings.filter(({ level }) => level < 3)) {
    error(where, `encabezado de nivel ${heading.level} («${heading.text}»): el título ya es ###`);
  }
  if (/<!--/.test(prose)) error(where, 'tiene comentarios HTML; las dudas van a PLAN.md (D-16)');
  if (/^\s*([-*_])(\s*\1){2,}\s*$/m.test(prose)) error(where, 'tiene separadores `---` (D-15)');
  // ©, ® and ™ are pictographic for Unicode, but ordinary text («© Govern Illes Balears»).
  const emoji = prose.match(/(?![©®™])\p{Extended_Pictographic}/u);
  if (emoji) error(where, `tiene el emoji «${emoji[0]}» (D-15)`);

  for (const [, alt, target] of prose.matchAll(/!\[([^\]]*)\]\(([^)\s]+)[^)]*\)/g)) {
    const image = path.resolve(path.dirname(file), decodeURI(target));
    if (!alt.trim()) error(where, `la imagen ${target} no tiene texto alternativo`);
    if (/^captura/i.test(alt.trim()))
      warn(where, `el texto alternativo de ${target} empieza por «captura»`);
    if (!existsSync(image)) {
      error(where, `la imagen ${target} no existe`);
      continue;
    }
    imagesUsed.add(image);
    const bytes = statSync(image).size;
    const size = `${Math.round(bytes / 1024)} KB`;
    if (bytes > MAX_IMAGE_BYTES) error(where, `${target} pesa ${size}; el máximo es 70 KB (D-22)`);
    else if (bytes > TARGET_IMAGE_BYTES)
      warn(where, `${target} pesa ${size}; lo deseable es 40 KB o menos`);
  }

  return { file, prose, headings };
}

function checkLinks(locale, documents) {
  const byFile = new Map(documents.filter(Boolean).map((doc) => [doc.file, doc]));
  for (const { file, prose } of byFile.values()) {
    const where = relative(file);
    for (const [, text, target] of prose.matchAll(/(?<!!)\[([^\]]*)\]\(([^)\s]+)[^)]*\)/g)) {
      if (/^[a-z]+:/i.test(target)) continue;
      const [pathPart, anchor = ''] = target.split('#');
      const linked = pathPart ? path.resolve(path.dirname(file), decodeURI(pathPart)) : file;
      const linkedDoc = byFile.get(linked);
      if (!linkedDoc) {
        error(
          where,
          `el enlace «${text}» lleva a ${target}, que no es un tema del índice de ${locale}`,
        );
      } else if (anchor && !linkedDoc.headings.some((heading) => heading.anchor === anchor)) {
        error(
          where,
          `el enlace «${text}» lleva a #${anchor}, que no es un apartado de ${relative(linked)}`,
        );
      }
    }
  }
}

const indexes = Object.fromEntries(LOCALES.map((locale) => [locale, readIndex(locale)]));
checkIndexShape(indexes.ca, indexes.es);

for (const locale of LOCALES) {
  checkIcons(indexes[locale], locale);
  const listed = documentsOf(indexes[locale]);
  const imagesUsed = new Set();
  const documents = listed.map((entry) => checkDocument(locale, entry, imagesUsed));
  checkLinks(locale, documents);

  const files = filesUnder(path.join(ROOT, locale));
  const listedFiles = new Set(listed.map(({ document }) => path.join(ROOT, locale, document)));
  for (const file of files) {
    if (file.endsWith('.md') && !listedFiles.has(file))
      error(relative(file), 'no está en el índice');
    if (/\.(webp|png|jpe?g|gif|svg)$/i.test(file) && !imagesUsed.has(file)) {
      error(relative(file), 'ningún tema usa esta imagen');
    }
    if (/\.(png|jpe?g|gif)$/i.test(file))
      warn(relative(file), 'las imágenes de la ayuda van en WebP (D-06)');
  }
}

// Each language has the same captures, so a screen changed in one is captured again in the other.
const imagesOf = (locale) =>
  new Set(
    filesUnder(path.join(ROOT, locale))
      .filter((file) => /\.webp$/i.test(file))
      .map((file) => path.relative(path.join(ROOT, locale), file).replaceAll('\\', '/')),
  );
const [caImages, esImages] = LOCALES.map(imagesOf);
for (const image of caImages) if (!esImages.has(image)) error(`es/${image}`, 'falta (está en ca)');
for (const image of esImages) if (!caImages.has(image)) error(`ca/${image}`, 'falta (está en es)');

for (const message of warnings) console.warn(`aviso  ${message}`);
for (const message of errors) console.error(`error  ${message}`);
console.log(
  errors.length
    ? `\nLa ayuda tiene ${errors.length} errores y ${warnings.length} avisos.`
    : `La ayuda está bien (${warnings.length} avisos).`,
);
process.exit(errors.length ? 1 : 0);
