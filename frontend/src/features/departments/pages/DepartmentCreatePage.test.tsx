import {
  fireEvent,
  render,
  screen,
  waitFor,
  within,
} from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import {
  MemoryRouter,
  Route,
  Routes,
} from 'react-router-dom'

import {DepartmentCreatePage} from './DepartmentCreatePage'
import {createDepartment} from '../services/departmentService'
import {ApiError} from '../../../api/httpClient'

jest.mock(
    '../services/departmentService',
    () => ({
      createDepartment: jest.fn(),
    }),
)

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
                          'Calle Falsa 123',
                      latitude: -34.5,
                      longitude: -58.5,
                      city: 'BA',
                    })
                }
            >
              Seleccionar dirección
            </button>
          </div>
      ),
    }),
)

const mockCreateDepartment =
    createDepartment as jest.MockedFunction<
        typeof createDepartment
    >

function renderPage() {
  return render(
      <MemoryRouter
          initialEntries={[
            '/departamentos/nuevo',
          ]}
      >
        <Routes>
          <Route
              path="/departamentos/nuevo"
              element={
                <DepartmentCreatePage />
              }
          />

          <Route
              path="/departamentos/:id"
              element={
                <div>
                  Detalle creado
                </div>
              }
          />
        </Routes>
      </MemoryRouter>,
  )
}

function getControl(
    label: RegExp,
    role:
        | 'textbox'
        | 'spinbutton'
        | 'combobox',
) {
  const labelElement =
      screen.getByText(label)

  const field =
      labelElement.parentElement

  if (!field) {
    throw new Error(
        'Form field container not found',
    )
  }

  return within(field).getByRole(role)
}

function getFileInput() {
  const input =
      document.querySelector<HTMLInputElement>(
          'input[type="file"]',
      )

  if (!input) {
    throw new Error(
        'File input not found',
    )
  }

  return input
}

async function fillValidForm() {
  const user = userEvent.setup()

  await user.type(
      getControl(/título/i, 'textbox'),
      'Depto',
  )

  await user.type(
      getControl(/precio/i, 'spinbutton'),
      '1000',
  )

  await user.type(
      getControl(
          /superficie/i,
          'spinbutton',
      ),
      '50',
  )

  await user.type(
      getControl(
          /descripción/i,
          'textbox',
      ),
      'Descripción',
  )

  await user.click(
      screen.getByRole('button', {
        name: /seleccionar dirección/i,
      }),
  )

  return user
}

let objectUrlCounter = 0

describe('DepartmentCreatePage', () => {
  beforeEach(() => {
    jest.clearAllMocks()

    objectUrlCounter = 0

    URL.createObjectURL = jest.fn(
        () => `blob:test-${++objectUrlCounter}`,
    )

    URL.revokeObjectURL =
        jest.fn()
  })

  it('renderiza formulario', () => {
    renderPage()

    expect(
        screen.getByRole('heading', {
          name: /crear departamento/i,
        }),
    ).toBeInTheDocument()
  })

  it('exige seleccionar una dirección', async () => {
    const user = userEvent.setup()

    renderPage()

    await user.type(
        getControl(/título/i, 'textbox'),
        'Depto',
    )

    await user.type(
        getControl(/precio/i, 'spinbutton'),
        '1000',
    )

    await user.type(
        getControl(/superficie/i, 'spinbutton'),
        '50',
    )

    await user.type(
        getControl(/descripción/i, 'textbox'),
        'Descripción',
    )

    await user.click(
        screen.getByRole('button', {
          name: /^crear departamento$/i,
        }),
    )

    expect(
        screen.getByText(
            /seleccioná una dirección/i,
        ),
    ).toBeInTheDocument()
  })

  it('permite acumular imágenes', async () => {
    renderPage()

    const user = userEvent.setup()

    const first = new File(
        ['a'],
        'a.png',
        {
          type: 'image/png',
        },
    )

    const second = new File(
        ['b'],
        'b.png',
        {
          type: 'image/png',
        },
    )

    const input = getFileInput()

    await user.upload(input, first)
    await user.upload(input, second)

    expect(
        screen.getAllByAltText(
            /vista previa/i,
        ),
    ).toHaveLength(2)
  })

  it('evita imágenes duplicadas', async () => {
    renderPage()

    const user = userEvent.setup()

    const file = new File(
        ['a'],
        'a.png',
        {
          type: 'image/png',
        },
    )

    const input = getFileInput()

    await user.upload(input, file)
    await user.upload(input, file)

    expect(
        screen.getAllByAltText(
            /vista previa/i,
        ),
    ).toHaveLength(1)
  })

  it('rechaza más de 5 imágenes', () => {
    renderPage()

    const files = Array.from(
        {length: 6},
        (_, index) =>
            new File(
                [`${index}`],
                `${index}.png`,
                {
                  type: 'image/png',
                },
            ),
    )

    fireEvent.change(
        getFileInput(),
        {
          target: {
            files,
          },
        },
    )

    // Tu implementación conserva el estado
    // anterior, que inicialmente está vacío.
    expect(
        screen.queryAllByAltText(
            /vista previa/i,
        ),
    ).toHaveLength(0)

    expect(
        screen.getByText(
            /máximo 5 imágenes/i,
        ),
    ).toBeInTheDocument()
  })

  it('rechaza formato no soportado', () => {
    renderPage()

    const file = new File(
        ['gif'],
        'image.gif',
        {
          type: 'image/gif',
        },
    )

    fireEvent.change(
        getFileInput(),
        {
          target: {
            files: [file],
          },
        },
    )

    expect(
        screen.getByText(
            /las imágenes deben ser jpg, png o webp/i,
        ),
    ).toBeInTheDocument()
  })

  it('rechaza archivos mayores a 10 MB', () => {
    renderPage()

    const file = new File(
        [
          new Uint8Array(
              10 * 1024 * 1024 + 1,
          ),
        ],
        'large.png',
        {
          type: 'image/png',
        },
    )

    fireEvent.change(
        getFileInput(),
        {
          target: {
            files: [file],
          },
        },
    )

    expect(
        screen.getByText(
            /máximo 10 MB/i,
        ),
    ).toBeInTheDocument()
  })

  it('permite quitar una imagen', async () => {
    renderPage()

    const user = userEvent.setup()

    await user.upload(
        getFileInput(),
        new File(
            ['a'],
            'a.png',
            {
              type: 'image/png',
            },
        ),
    )

    await user.click(
        screen.getByRole('button', {
          name: /quitar/i,
        }),
    )

    expect(
        screen.queryByAltText(
            /vista previa/i,
        ),
    ).not.toBeInTheDocument()
  })

  it('llama createDepartment con request e imágenes', async () => {
    renderPage()

    mockCreateDepartment.mockResolvedValue(
        {
          id: 123,
        } as any,
    )

    const user =
        await fillValidForm()

    const file = new File(
        ['image'],
        'image.png',
        {
          type: 'image/png',
        },
    )

    await user.upload(
        getFileInput(),
        file,
    )

    await user.click(
        screen.getByRole('button', {
          name: /^crear departamento$/i,
        }),
    )

    await waitFor(() =>
        expect(
            mockCreateDepartment,
        ).toHaveBeenCalled(),
    )

    const [request, files] =
        mockCreateDepartment.mock.calls[0]

    expect(request).toMatchObject({
      titulo: 'Depto',
      precio: 1000,
      metrosCuadrados: 50,
      direccion:
          'Calle Falsa 123',
      latitud: -34.5,
      longitud: -58.5,
    })

    expect(files).toEqual([file])
  })

  it('navega al detalle al crear correctamente', async () => {
    mockCreateDepartment.mockResolvedValue(
        {
          id: 123,
        } as any,
    )

    renderPage()

    const user =
        await fillValidForm()

    await user.click(
        screen.getByRole('button', {
          name: /^crear departamento$/i,
        }),
    )

    expect(
        await screen.findByText(
            'Detalle creado',
        ),
    ).toBeInTheDocument()
  })

  it.each([
    [
      400,
      /datos inválidos/i,
    ],
    [
      413,
      /superan el tamaño permitido/i,
    ],
    [
      415,
      /formato no soportado/i,
    ],
    [
      503,
      /almacenamiento de imágenes no está disponible/i,
    ],
  ])(
      'muestra error correspondiente para status %s',
      async (status, message) => {
        mockCreateDepartment.mockRejectedValue(
            new ApiError(
                status,
                'Error',
            ),
        )

        renderPage()

        const user =
            await fillValidForm()

        await user.click(
            screen.getByRole('button', {
              name: /^crear departamento$/i,
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