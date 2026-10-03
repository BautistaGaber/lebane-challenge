export class ApiError extends Error {
    public readonly status: number
    public readonly body?: unknown

    constructor(
        status: number,
        message: string,
        body?: unknown,
    ) {
        super(message)
        this.name = 'ApiError'
        this.name = 'ApiError'
        this.status = status
        this.body = body
    }
}

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export async function apiRequest<T>(path: string, options?: RequestInit): Promise<T> {
    const response = await fetch(`${API_URL}${path}`, options)

    const contentType = response.headers.get('content-type')

    let body: unknown = null

    if (contentType?.includes('application/json'))
    {
        body = await response.json()
    } else {
        const text = await response.text()
        body = text || null
    }

    if (!response.ok) {
        throw new ApiError(response.status, getErrorMessage(body, response.status), body)
    }

    return body as T
}

function getErrorMessage(body: unknown, status: number): string
{
    if (typeof body === 'object' && body !== null && 'message' in body && typeof body.message === 'string') {
        return body.message
    }

    if (typeof body === 'string' && body.trim()) {
        return body
    }

    return `HTTP error ${status}`
}