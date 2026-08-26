import { expect } from '@playwright/test';
import type { Locator, Page } from '@playwright/test';

const DEFAULT_ROW_ACTIONS_BUTTON = 'Obrir les accions del registre';

export async function selectOption(
  page: Page,
  inputId: string,
  optionName?: string,
): Promise<void> {
  await page.locator(`#${inputId}`).click();

  const option = optionName
    ? page.getByRole('option', { name: optionName, exact: true })
    : page.getByRole('option').first();

  await expect(option, `No options are available for #${inputId}`).toBeVisible();
  await option.click();
}

export async function selectFilteredOption(
  page: Page,
  inputId: string,
  filterValue: string,
  optionName: string,
): Promise<void> {
  await page.locator(`#${inputId}`).click();

  const overlay = page.locator('.p-select-overlay:visible');
  const filter = overlay.locator('input.p-select-filter');
  await expect(filter, `The filter for #${inputId} is not available`).toBeVisible();
  await filter.fill(filterValue);

  const option = overlay.getByRole('option', { name: optionName, exact: true });
  await expect(option, `${optionName} is not available for #${inputId}`).toBeVisible();
  await option.click();
}

export async function selectRowAction(
  page: Page,
  row: Locator,
  action: string,
  rowActionsButton = DEFAULT_ROW_ACTIONS_BUTTON,
): Promise<void> {
  await row.getByRole('button', { name: rowActionsButton }).click();
  await page.getByRole('menuitem', { name: action, exact: true }).click();
}
