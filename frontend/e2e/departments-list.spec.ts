import {test, expect} from '@playwright/test'

test('departments list flow', async ({page}) => {
  await page.goto('/departamentos')

  await expect(
      page.getByRole('heading', {
        name: 'Departamentos',
      }),
  ).toBeVisible()

  await expect(
      page.getByRole('link', {
        name: /nuevo departamento/i,
      }),
  ).toBeVisible()

  await page.getByRole('button', {
    name: /aplicar filtros/i,
  }).click()

  await page.getByRole('button', {
    name: /limpiar/i,
  }).click()

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
})