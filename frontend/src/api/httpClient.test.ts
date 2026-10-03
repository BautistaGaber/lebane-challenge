import {
  apiRequest,
  ApiError,
} from './httpClient'

const originalFetch = global.fetch

const fetchMock =
    jest.fn() as jest.MockedFunction<typeof fetch>

describe('httpClient', () => {
  beforeEach(() => {
    fetchMock.mockReset()
    global.fetch = fetchMock
  })

  afterAll(() => {
    global.fetch = originalFetch
  })

  it('devuelve JSON para response exitosa', async () => {
    fetchMock.mockResolvedValue({
      ok: true,
      status: 200,
      headers: {
        get: () => 'application/json',
      },
      json: async () => ({
        data: 'test',
      }),
      text: async () => 'test',
    } as unknown as Response)

    const result =
        await apiRequest<{data: string}>('/test')

    expect(result).toEqual({
      data: 'test',
    })

    expect(fetchMock).toHaveBeenCalledWith(
        'http://localhost:8080/test',
        undefined,
    )
  })

  it('lanza ApiError con status correcto', async () => {
    fetchMock.mockResolvedValue({
      ok: false,
      status: 404,
      headers: {
        get: () => 'application/json',
      },
      json: async () => ({
        message: 'Not found',
      }),
    } as unknown as Response)

    await expect(
        apiRequest('/test'),
    ).rejects.toMatchObject({
      name: 'ApiError',
      status: 404,
    })
  })

  it('conserva body', async () => {
    const responseBody = {
      message: 'Bad',
      errors: [],
    }

    fetchMock.mockResolvedValue({
      ok: false,
      status: 400,
      headers: {
        get: () => 'application/json',
      },
      json: async () => responseBody,
    } as unknown as Response)

    try {
      await apiRequest('/test')

      throw new Error(
          'apiRequest should have failed',
      )
    } catch (error) {
      expect(error).toBeInstanceOf(ApiError)

      expect(
          (error as ApiError).body,
      ).toEqual(responseBody)
    }
  })

  it('usa message del backend si existe', async () => {
    fetchMock.mockResolvedValue({
      ok: false,
      status: 400,
      headers: {
        get: () => 'application/json',
      },
      json: async () => ({
        message: 'Error personalizado',
      }),
    } as unknown as Response)

    await expect(
        apiRequest('/test'),
    ).rejects.toThrow(
        'Error personalizado',
    )
  })

  it('maneja respuesta de texto', async () => {
    fetchMock.mockResolvedValue({
      ok: false,
      status: 500,
      headers: {
        get: () => 'text/plain',
      },
      text: async () => 'Internal error',
    } as unknown as Response)

    await expect(
        apiRequest('/test'),
    ).rejects.toThrow(
        'Internal error',
    )
  })
})