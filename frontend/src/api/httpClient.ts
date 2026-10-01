const API_URL =
    import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export async function apiRequest<T>(
    path: string,
    options?: RequestInit,)
    : Promise<T> {
    const response = await fetch(`${API_URL}${path}`, options)

    if (!response.ok) {
        throw new Error(`HTTP error ${response.status}`)
    }

    return response.json() as Promise<T>
}