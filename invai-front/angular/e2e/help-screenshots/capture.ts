import AxeBuilder from '@axe-core/playwright';
import { expect } from '@playwright/test';
import type { Locator, Page } from '@playwright/test';
import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import path from 'node:path';

export type HelpLocale = 'ca' | 'es';

interface HelpTopic {
  label: string;
  document?: string;
  items?: HelpTopic[];
}

const HELP_ROOT = path.resolve(__dirname, '../../public/help');
const SPANISH_MESSAGES = path.resolve(__dirname, '../../src/locale/messages.es.xml');
const INDEX_REQUEST = /\/help\/(ca|es)\/index\.json$/;
const CROP_MARGIN = 24;

export const helpDialog = (page: Page): Locator =>
  page.getByRole('dialog').filter({ has: page.locator('#help-modal-search') });

/**
 * Opens the help centre with Ctrl + K and returns the language of the running server: with
 * `npm run local` the page does not declare it, so it is read from the help folder the app asks
 * for, which follows the same rule as the app.
 */
export async function openHelp(page: Page): Promise<HelpLocale> {
  await page.goto('/');
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  const indexRequest = page.waitForRequest(INDEX_REQUEST);
  await page.keyboard.press('Control+k');
  const locale = INDEX_REQUEST.exec((await indexRequest).url())![1] as HelpLocale;
  await expect(helpDialog(page)).toBeVisible();
  return locale;
}

/** Opens the home page (the applications list) and returns the language of the running server. */
export async function openApp(page: Page): Promise<HelpLocale> {
  const locale = await openHelp(page);
  await page.keyboard.press('Escape');
  await expect(helpDialog(page)).toBeHidden();
  return locale;
}

let spanishTexts: Map<string, string> | undefined;

/**
 * The text the interface shows in `locale` for a Catalan source text, read from the same
 * translation file the Spanish build uses, so the specs name buttons once, in Catalan.
 */
export function uiText(locale: HelpLocale, catalan: string): string {
  if (locale === 'ca') return catalan;
  spanishTexts ??= readSpanishTexts();
  const spanish = spanishTexts.get(catalan);
  if (!spanish) throw new Error(`"${catalan}" has no Spanish text in messages.es.xml`);
  return spanish;
}

function readSpanishTexts(): Map<string, string> {
  const decode = (text: string) =>
    text
      .replace(/&apos;/g, "'")
      .replace(/&quot;/g, '"')
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&amp;/g, '&');
  const texts = new Map<string, string>();
  const xml = readFileSync(SPANISH_MESSAGES, 'utf-8');
  // Units with placeholders contain markup and are not button or field names.
  for (const [, source, target] of xml.matchAll(
    /<source>([^<]*)<\/source>\s*<target>([^<]*)<\/target>/g,
  )) {
    const key = decode(source);
    if (!texts.has(key)) texts.set(key, decode(target));
  }
  return texts;
}

/** Labels of the group and the topic that open `document`, read from that language's index. */
export function topicPath(locale: HelpLocale, document: string): string[] {
  const topics = JSON.parse(
    readFileSync(path.join(HELP_ROOT, locale, 'index.json'), 'utf-8'),
  ) as HelpTopic[];
  const find = (items: HelpTopic[], trail: string[]): string[] | null => {
    for (const item of items) {
      if (item.document === document) return [...trail, item.label];
      const found = find(item.items ?? [], [...trail, item.label]);
      if (found) return found;
    }
    return null;
  };
  const labels = find(topics, []);
  if (!labels) throw new Error(`${document} is not in the ${locale} help index`);
  return labels;
}

/** Checks a screen's own localized help, its images, keyboard focus and responsive content. */
export async function expectScreenHelp(page: Page, locale: HelpLocale, document: string): Promise<void> {
  const title = topicPath(locale, document).at(-1)!;
  await page.keyboard.press('Control+k');
  const help = helpDialog(page);
  await expect(help.getByRole('heading', { level: 3, name: title, exact: true })).toBeVisible();
  await expect(help.locator('#help-modal-search')).toBeFocused();
  const images = help.locator('markdown img');
  await expect(images).not.toHaveCount(0);
  for (const img of await images.all()) {
    await expect(img).toHaveAttribute('src', new RegExp(`/help/${locale}/`));
    await expect.poll(() => img.evaluate((image) => (image as HTMLImageElement).naturalWidth))
      .toBeGreaterThan(0);
  }

  const desktop = page.viewportSize()!;
  try {
    await page.setViewportSize({ width: 320, height: 800 });
    const overflow = () => help.locator('.p-dialog-content')
      .evaluate((content) => content.scrollWidth - content.clientWidth);
    await expect.poll(overflow).toBeLessThanOrEqual(1);
    const results = await new AxeBuilder({ page })
      .include('.p-dialog:has(#help-modal-search)')
      .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
      .analyze();
    expect(results.violations, `${locale}/${document} help at 320 px`).toEqual([]);
    await page.setViewportSize(desktop);
    const zoom = await page.addStyleTag({ content: 'html { font-size: 200% !important; }' });
    try {
      await expect.poll(overflow).toBeLessThanOrEqual(1);
    } finally {
      await zoom.evaluate((style) => { style.parentNode?.removeChild(style); });
    }
  } finally {
    await page.setViewportSize(desktop);
  }
  await page.keyboard.press('Escape');
  await expect(help).toBeHidden();
}

/**
 * Replaces the text of every element `locator` matches with example data, so no real person shows
 * in a capture: the help images are static files anyone can download.
 */
export async function maskText(locator: Locator, text: string): Promise<void> {
  await locator.evaluateAll((elements, value) => {
    for (const element of elements) element.textContent = value;
  }, text);
}

/**
 * Takes a screenshot of `target` and writes it as WebP under public/help/<locale>/. With `cropTo`,
 * the image ends just below the lowest of those elements, so it does not carry the empty part of
 * a tall dialog. With `area`, it covers exactly those elements (a menu and its submenu), plus a
 * margin. Chromium does the encoding through a canvas, so no image library is needed.
 */
export async function saveHelpImage(
  page: Page,
  target: Locator,
  locale: HelpLocale,
  file: string,
  {
    cropTo = [],
    area = [],
    margin = CROP_MARGIN,
  }: { cropTo?: Locator[]; area?: Locator[]; margin?: number } = {},
): Promise<void> {
  // The pointer would leave hover styles on whatever it rests on.
  await page.mouse.move(0, 0);
  const options = { animations: 'disabled', caret: 'hide', scale: 'css' } as const;
  let png: Buffer;
  if (area.length) {
    const boxes = await Promise.all(area.map((element) => element.boundingBox()));
    const shown = boxes.filter((box) => box !== null);
    if (shown.length !== area.length) throw new Error(`Part of ${file} is not on the page`);
    const left = Math.max(0, Math.min(...shown.map((box) => box.x)) - margin);
    const top = Math.max(0, Math.min(...shown.map((box) => box.y)) - margin);
    const right = Math.max(...shown.map((box) => box.x + box.width)) + margin;
    const bottom = Math.max(...shown.map((box) => box.y + box.height)) + margin;
    png = await page.screenshot({
      ...options,
      clip: { x: left, y: top, width: right - left, height: bottom - top },
    });
  } else if (cropTo.length) {
    // Only used for the fixed help dialog, which never scrolls, so viewport coordinates work.
    const box = await target.boundingBox();
    if (!box) throw new Error(`Nothing to capture for ${file}`);
    const bottoms = await Promise.all(
      cropTo.map(async (element) => {
        const area = await element.boundingBox();
        return area ? area.y + area.height : 0;
      }),
    );
    const bottom = Math.min(box.y + box.height, Math.max(...bottoms) + margin);
    png = await page.screenshot({
      ...options,
      clip: { x: box.x, y: box.y, width: box.width, height: bottom - box.y },
    });
  } else {
    // The app scrolls inside a height-limited shell. A locator screenshot can extend past that
    // shell and leave the lower half blank; make room before taking it, then restore the viewport.
    const viewport = page.viewportSize();
    const box = await target.boundingBox();
    const expand = viewport && box && box.height > viewport.height - 240;
    try {
      if (expand) {
        await page.setViewportSize({
          width: viewport.width,
          height: Math.ceil(box.height) + viewport.height,
        });
      }
      png = await target.screenshot(options);
    } finally {
      if (expand) await page.setViewportSize(viewport);
    }
  }

  const converter = await page.context().newPage();
  try {
    const dataUrl = await converter.evaluate(async (base64) => {
      const image = new Image();
      image.src = `data:image/png;base64,${base64}`;
      await image.decode();
      const canvas = document.createElement('canvas');
      canvas.width = image.naturalWidth;
      canvas.height = image.naturalHeight;
      canvas.getContext('2d')!.drawImage(image, 0, 0);
      let encoded = '';
      for (const quality of [0.85, 0.8, 0.75, 0.7, 0.65, 0.6, 0.55, 0.5]) {
        encoded = canvas.toDataURL('image/webp', quality);
        if (atob(encoded.split(',')[1]).length <= 40 * 1024) break;
      }
      return encoded;
    }, png.toString('base64'));
    const prefix = 'data:image/webp;base64,';
    if (!dataUrl.startsWith(prefix)) throw new Error('Chromium did not encode the image as WebP');

    const bytes = Buffer.from(dataUrl.slice(prefix.length), 'base64');
    if (bytes.length > 70 * 1024) throw new Error(`${file} exceeds 70 KB; crop a smaller area`);
    const output = path.join(HELP_ROOT, locale, file);
    mkdirSync(path.dirname(output), { recursive: true });
    writeFileSync(output, bytes);
  } finally {
    await converter.close();
  }
}
