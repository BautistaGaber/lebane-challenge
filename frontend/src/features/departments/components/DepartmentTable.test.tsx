import {
  render,
  screen,
} from '@testing-library/react'
import {MemoryRouter} from 'react-router-dom'

import {DepartmentTable} from './DepartmentTable'
import type {DepartmentListItem} from '../types/department'

const departments: DepartmentListItem[] = [
  {
    id: 1,
    titulo: 'Depto 1',
    precio: 100000,
    moneda: 'USD',
    metrosCuadrados: 70,
    disponible: true,
    cantidadImagenes: 2,
    cantidadConsultas: 5,
    imagenPrincipal: 'img.jpg',
  },
]

function renderTable() {
  return render(
      <MemoryRouter>
        <DepartmentTable
            departments={departments}
        />
      </MemoryRouter>,
  )
}

describe('DepartmentTable', () => {
  it('renderiza datos del departamento', () => {
    renderTable()

    expect(
        screen.getAllByText('Depto 1').length,
    ).toBeGreaterThan(0)

    expect(
        screen.getAllByText(/100\.000/).length,
    ).toBeGreaterThan(0)

    expect(
        screen.getAllByText(/disponible/i)
            .length,
    ).toBeGreaterThan(0)
  })

  it('renderiza links correctos', () => {
    renderTable()

    const links =
        screen.getAllByRole('link', {
          name: /ver detalle/i,
        })

    expect(
        links.some(
            (link) =>
                link.getAttribute('href') ===
                '/departamentos/1',
        ),
    ).toBe(true)
  })
})