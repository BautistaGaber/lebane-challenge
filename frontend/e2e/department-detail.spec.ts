import {test, expect} from '@playwright/test'

test('department detail navigation', async ({page}) => {
  await page.goto('/departamentos')

  const detailLink = page
      .getByRole('link', {
        name: /ver detalle/i,
      })
      .first()

  await expect(detailLink).toBeVisible()

  await detailLink.click()

  await expect(page).toHaveURL(
      /\/departamentos\/\d+$/,
  )

  await expect(
      page.getByRole('link', {
        name: /editar departamento/i,
      }),
  ).toBeVisible()

  await expect(
      page.getByRole('heading', {
        name: /información/i,
      }),
  ).toBeVisible()

  await expect(
      page.getByRole('heading', {
        name: /galería/i,
      }),
  ).toBeVisible()

  await expect(
      page.getByRole('heading', {
        name: /consultas/i,
      }),
  ).toBeVisible()
})