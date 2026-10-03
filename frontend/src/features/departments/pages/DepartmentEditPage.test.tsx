import {
  render,
  screen,
  waitFor,
} from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import {
  MemoryRouter,
  Route,
  Routes,
} from 'react-router-dom'

import {DepartmentEditPage} from './DepartmentEditPage'
import {useDepartment} from '../hooks/useDepartment'
import {updateDepartment} from '../services/departmentService'
import {ApiError} from '../../../api/httpClient'

jest.mock('../hooks/useDepartment')

jest.mock('../services/departmentService')

jest.mock(
    '../components/AddressAutocomplete',
    () => ({
      AddressAutocomplete: ({
                              value,
                              onChange,
                              onSelect,
                            }: any) => (
          <div>
            <input
                aria-label="address-autocomplete"
                value={value}
                onChange={(event) =>
                    onChange(
                        event.target.value,
                    )
                }
            />

            <button
                type="button"
                onClick={() =>
                    onSelect({
                      formatted:
                          'Nueva Calle 456',
                      latitude: -34.6,
                      longitude: -58.4,
                      city: 'BA',
                    })
                }
            >
              Seleccionar sugerencia
            </button>
          </div>
      ),
    }),
)

const mockUseDepartment =
    useDepartment as jest.MockedFunction<
        typeof useDepartment
    >

const mockUpdateDepartment =
    updateDepartment as jest.MockedFunction<
        typeof updateDepartment
    >

const mockDept = {
  id: 1,
  titulo: 'Depto Original',
  descripcion: 'Desc',
  precio: 1000,
  moneda: 'USD' as const,
  metrosCuadrados: 50,
  direccion: 'Calle Original 123',
  latitud: -34.5,
  longitud: -58.5,
  disponible: true,
  version: 5,
  cantidadImagenes: 2,
  cantidadConsultas: 1,
}

function renderWithRouter() {
  return render(
      <MemoryRouter
          initialEntries={[
            '/departamentos/1/editar',
          ]}
      >
        <Routes>
          <Route
              path="/departamentos/:id/editar"
              element={
                <DepartmentEditPage />
              }
          />

          <Route
              path="/departamentos/:id"
              element={
                <div>
                  Detalle destino
                </div>
              }
          />
        </Routes>
      </MemoryRouter>,
  )
}

describe('DepartmentEditPage', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('muestra loading', () => {
    mockUseDepartment.mockReturnValue({
      department: null,
      loading: true,
      error: null,
    })

    renderWithRouter()

    expect(
        screen.getByText(
            /cargando departamento/i,
        ),
    ).toBeInTheDocument()
  })

  it('muestra error', () => {
    mockUseDepartment.mockReturnValue({
      department: null,
      loading: false,
      error: 'Error',
    })

    renderWithRouter()

    expect(
        screen.getByText(/error/i),
    ).toBeInTheDocument()
  })

  it('carga datos existentes', () => {
    mockUseDepartment.mockReturnValue({
      department: mockDept as any,
      loading: false,
      error: null,
    })

    renderWithRouter()

    expect(
        screen.getByDisplayValue(
            'Depto Original',
        ),
    ).toBeInTheDocument()
  })

  it('permite editar título', async () => {
    const user = userEvent.setup()

    mockUseDepartment.mockReturnValue({
      department: mockDept as any,
      loading: false,
      error: null,
    })

    renderWithRouter()

    const input =
        screen.getByDisplayValue(
            'Depto Original',
        )

    await user.clear(input)
    await user.type(
        input,
        'Nuevo Título',
    )

    expect(
        screen.getByDisplayValue(
            'Nuevo Título',
        ),
    ).toBeInTheDocument()
  })

  it('conserva latitud/longitud si no cambia la dirección', async () => {
    const user = userEvent.setup()

    mockUseDepartment.mockReturnValue({
      department: mockDept as any,
      loading: false,
      error: null,
    })

    mockUpdateDepartment.mockResolvedValue({
      id: 1,
    } as any)

    renderWithRouter()

    await user.click(
        screen.getByRole('button', {
          name: /guardar cambios/i,
        }),
    )

    await waitFor(() => {
      expect(
          mockUpdateDepartment,
      ).toHaveBeenCalled()
    })

    const request =
        mockUpdateDepartment.mock.calls[0][1]

    expect(request.latitud).toBe(
        mockDept.latitud,
    )

    expect(request.longitud).toBe(
        mockDept.longitud,
    )
  })

  it('si cambia la dirección manualmente sin seleccionar sugerencia muestra error', async () => {
    const user = userEvent.setup()

    mockUseDepartment.mockReturnValue({
      department: mockDept as any,
      loading: false,
      error: null,
    })

    renderWithRouter()

    const autocomplete =
        screen.getByLabelText(
            'address-autocomplete',
        )

    await user.clear(autocomplete)

    await user.type(
        autocomplete,
        'Nueva dirección manual',
    )

    await user.click(
        screen.getByRole('button', {
          name: /guardar cambios/i,
        }),
    )

    expect(
        screen.getByText(
            /seleccioná una dirección de las sugerencias/i,
        ),
    ).toBeInTheDocument()

    expect(
        mockUpdateDepartment,
    ).not.toHaveBeenCalled()
  })

  it('si selecciona sugerencia actualiza dirección y coordenadas', async () => {
    const user = userEvent.setup()

    mockUseDepartment.mockReturnValue({
      department: mockDept as any,
      loading: false,
      error: null,
    })

    mockUpdateDepartment.mockResolvedValue({
      id: 1,
    } as any)

    renderWithRouter()

    await user.click(
        screen.getByRole('button', {
          name: /seleccionar sugerencia/i,
        }),
    )

    await user.click(
        screen.getByRole('button', {
          name: /guardar cambios/i,
        }),
    )

    await waitFor(() => {
      expect(
          mockUpdateDepartment,
      ).toHaveBeenCalled()
    })

    const request =
        mockUpdateDepartment.mock.calls[0][1]

    expect(request.direccion).toBe(
        'Nueva Calle 456',
    )

    expect(request.latitud).toBe(-34.6)
    expect(request.longitud).toBe(-58.4)
  })

  it('envía version', async () => {
    const user = userEvent.setup()

    mockUseDepartment.mockReturnValue({
      department: mockDept as any,
      loading: false,
      error: null,
    })

    mockUpdateDepartment.mockResolvedValue({
      id: 1,
    } as any)

    renderWithRouter()

    await user.click(
        screen.getByRole('button', {
          name: /guardar cambios/i,
        }),
    )

    await waitFor(() =>
        expect(
            mockUpdateDepartment,
        ).toHaveBeenCalled(),
    )

    expect(
        mockUpdateDepartment
            .mock.calls[0][1].version,
    ).toBe(5)
  })

  it('navega al detalle después de guardar', async () => {
    const user = userEvent.setup()

    mockUseDepartment.mockReturnValue({
      department: mockDept as any,
      loading: false,
      error: null,
    })

    mockUpdateDepartment.mockResolvedValue({
      id: 1,
    } as any)

    renderWithRouter()

    await user.click(
        screen.getByRole('button', {
          name: /guardar cambios/i,
        }),
    )

    expect(
        await screen.findByText(
            'Detalle destino',
        ),
    ).toBeInTheDocument()
  })

  it.each([
    [
      400,
      /datos inválidos/i,
    ],
    [
      404,
      /ya no existe/i,
    ],
    [
      409,
      /modificado por otra operación/i,
    ],
    [
      500,
      /no se pudieron guardar/i,
    ],
  ])(
      'muestra mensaje correspondiente para status %s',
      async (status, message) => {
        const user = userEvent.setup()

        mockUseDepartment.mockReturnValue({
          department: mockDept as any,
          loading: false,
          error: null,
        })

        mockUpdateDepartment.mockRejectedValue(
            new ApiError(
                status,
                'Error',
            ),
        )

        renderWithRouter()

        await user.click(
            screen.getByRole('button', {
              name: /guardar cambios/i,
            }),
        )

        expect(
            await screen.findByText(
                message,
            ),
        ).toBeInTheDocument()
      },
  )
})