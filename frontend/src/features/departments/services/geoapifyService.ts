import type {AddressSuggestion, GeoapifyResponse} from "../types/geoapify.ts";

const GEOAPIFY_API_URL = 'https://api.geoapify.com/v1/geocode/autocomplete'

const API_KEY = import.meta.env.VITE_GEOAPIFY_API_KEY

export async function searchAddresses(query: string, signal?: AbortSignal): Promise<AddressSuggestion[]> {

    if (!API_KEY) {
        throw new Error('Geoapify API key is not configured')
    }

    const params = new URLSearchParams({
        text: query,
        format: 'json',
        apiKey: API_KEY,
        limit: '5',
        lang: 'es',
        filter: 'countrycode:ar',
    })

    const response = await fetch(`${GEOAPIFY_API_URL}?${params.toString()}`, { signal })

    if (!response.ok) {
        throw new Error(`Geoapify request failed with status ${response.status}`)
    }

    const data = await response.json() as GeoapifyResponse

    return data.results.map((result) => ({
        formatted: result.formatted,
        latitude: result.lat,
        longitude: result.lon,
        city: result.city,
        state: result.state,
        postcode: result.postcode
    }))
}