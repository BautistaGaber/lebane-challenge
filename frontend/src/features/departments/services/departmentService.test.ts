import {
  createDepartment,
  getDepartmentById,
  updateDepartment,
} from './departmentService'

import * as httpClient
  from '../../../api/httpClient'

jest.mock('../../../api/httpClient')

const mockApiRequest =
    httpClient.apiRequest as jest.MockedFunction<
        typeof httpClient.apiRequest
    >

const createData = {
  titulo: 'Departamento',
  descripcion: 'Descripción',
  precio: 1000,
  moneda: 'USD' as const,
  metrosCuadrados: 50,
  direccion: 'Dirección',
  latitud: -34,
  longitud: -58,
  disponible: true,
}

describe('departmentService', () => {
  beforeEach(() => {
    mockApiRequest.mockReset()
  })

  it('getDepartmentById usa endpoint correcto', async () => {
    mockApiRequest.mockResolvedValue({
      id: 5,
    } as any)

    await getDepartmentById(5)

    expect(
        mockApiRequest,
    ).toHaveBeenCalledWith(
        '/api/departamentos/5',
    )
  })

  it('updateDepartment usa PUT JSON', async () => {
    const request = {
      ...createData,
      version: 1,
    }

    mockApiRequest.mockResolvedValue({
      id: 1,
    } as any)

    await updateDepartment(
        1,
        request,
    )

    expect(
        mockApiRequest,
    ).toHaveBeenCalledWith(
        '/api/departamentos/1',
        {
          method: 'PUT',
          headers: {
            'Content-Type':
                'application/json',
          },
          body:
              JSON.stringify(request),
        },
    )
  })

  it('createDepartment genera multipart correctamente', async () => {
    mockApiRequest.mockResolvedValue({
      id: 1,
    } as any)

    const images = [
      new File(
          ['a'],
          'a.png',
          {
            type: 'image/png',
          },
      ),
      new File(
          ['b'],
          'b.png',
          {
            type: 'image/png',
          },
      ),
    ]

    await createDepartment(
        createData,
        images,
    )

    const [, options] =
        mockApiRequest.mock.calls[0]

    expect(options?.method).toBe(
        'POST',
    )

    expect(
        options?.body,
    ).toBeInstanceOf(FormData)

    expect(
        options?.headers,
    ).toBeUndefined()

    const formData =
        options?.body as FormData

    expect(
        formData.has('departamento'),
    ).toBe(true)

    expect(
        formData.getAll('imagenes'),
    ).toHaveLength(2)
  })
})