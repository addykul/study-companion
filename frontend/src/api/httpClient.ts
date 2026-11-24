import { HttpError } from './HttpError'

type FetchLike = typeof fetch

async function readErrorMessage(response: Response): Promise<string> {
  const fallback = `Request failed (${response.status}).`
  try {
    const body: unknown = await response.json()
    if (typeof body === 'object' && body !== null && 'message' in body && typeof body.message === 'string') return body.message
    if (typeof body === 'object' && body !== null && 'detail' in body && typeof body.detail === 'string') return body.detail
  } catch {
    // The response may have no JSON body; keep the status-based message.
  }
  return fallback
}

export function createHttpClient(baseUrl: string, fetcher: FetchLike = fetch) {
  const normalizedBaseUrl = baseUrl.replace(/\/$/, '')

  return async function request<T>(path: string, init?: RequestInit): Promise<T> {
    let response: Response
    try {
      response = await fetcher(`${normalizedBaseUrl}${path}`, init)
    } catch {
      throw new Error('Could not connect to the API. Make sure the backend is running on port 8080.')
    }

    if (!response.ok) throw new HttpError(await readErrorMessage(response), response.status)
    if (response.status === 204) return undefined as T
    return await response.json() as T
  }
}
