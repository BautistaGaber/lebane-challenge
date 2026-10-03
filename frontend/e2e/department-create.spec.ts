import {test, expect} from '@playwright/test'

test('create department with mocked geoapify', async ({page}) => {
  await page.route(
      /api\.geoapify\.com/,
      async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            results: [
              {
                formatted:
                    'Calle Falsa 123, Buenos Aires',
                lat: -34.5,
                lon: -58.5,
                city: 'Buenos Aires',
                state: 'Buenos Aires',
              },
            ],
          }),
        })
      },
  )

  await page.goto('/departamentos/nuevo')

  await expect(
      page.getByRole('heading', {
        name: /crear departamento/i,
      }),
  ).toBeVisible()

  await page.getByRole('textbox', {
    name: /título/i,
  }).fill('Departamento Test E2E')

  await page.getByRole('spinbutton', {
    name: /precio/i,
  }).fill('1000')

  await page.getByLabel(/moneda/i)
      .selectOption('USD')

  await page.getByRole('spinbutton', {
    name: /superficie/i,
  }).fill('50')

  await page.getByLabel(/estado/i)
      .selectOption('true')

  await page.getByRole('textbox', {
    name: /descripción/i,
  }).fill(
      'Descripción de prueba E2E',
  )

  const addressInput =
      page.getByPlaceholder(
          /ingresá una dirección/i,
      )

  await addressInput.fill(
      'Calle Falsa',
  )

  const suggestion =
      page.getByText(
          'Calle Falsa 123, Buenos Aires',
      )

  await expect(
      suggestion,
  ).toBeVisible()

  await suggestion.click()

  await page.getByRole('button', {
    name: /^crear departamento$/i,
  }).click()

  await expect(page).toHaveURL(
      /\/departamentos\/\d+$/,
  )

  await expect(
      page.getByText(
          'Departamento Test E2E',
      ),
  ).toBeVisible()
})