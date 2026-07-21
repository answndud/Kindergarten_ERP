import { test, expect } from '@playwright/test';

test('principal can log in and reach the operations dashboard', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('이메일 주소').fill('principal@test.com');
  await page.getByRole('textbox', { name: '비밀번호' }).fill('test1234!');
  await page.getByRole('button', { name: '로그인', exact: true }).click();

  await expect(page).toHaveURL(/\/$|\/dashboard/);
  await expect(page.getByRole('link', { name: '대시보드', exact: true }).first()).toBeVisible();
});

test('mobile viewport keeps the login action usable', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/login');

  const loginButton = page.getByRole('button', { name: '로그인', exact: true });
  await expect(loginButton).toBeVisible();
  await expect(loginButton).toHaveCSS('min-height', '44px');
  await expect(page.locator('body')).toHaveJSProperty('scrollWidth', 390);
});
