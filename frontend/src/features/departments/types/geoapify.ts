export interface AddressSuggestion {
    formatted: string
    latitude: number
    longitude: number
    city?: string
    state?: string
    postcode?: string
}

export interface GeoapifyResult {
    formatted: string
    lat: number
    lon: number
    city?: string
    state?: string
    postcode?: string
}

export interface GeoapifyResponse {
    results: GeoapifyResult[]
}