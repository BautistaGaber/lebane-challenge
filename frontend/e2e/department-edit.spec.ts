import {test, expect} from '@playwright/test'

test('edit department', async ({page}) => {
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

  const editLink = page.getByRole('link', {
    name: /editar departamento/i,
  })

  await expect(editLink).toBeVisible()

  await editLink.click()

  await expect(page).toHaveURL(
      /\/departamentos\/\d+\/editar$/,
  )

  const titleInput = page.getByRole(
      'textbox',
      {
        name: /título/i,
      },
  )

  await expect(titleInput).toBeVisible()

  const originalTitle =
      await titleInput.inputValue()

  const updatedTitle =
      `${originalTitle} E2E`

  await titleInput.fill(updatedTitle)

  await page.getByRole('button', {
    name: /guardar cambios/i,
  }).click()

  await expect(page).toHaveURL(
      /\/departamentos\/\d+$/,
  )

  await expect(
      page.getByText(updatedTitle),
  ).toBeVisible()
})