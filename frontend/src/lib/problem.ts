import { AxiosError } from 'axios'

interface ProblemDetails {
  title?: string
  detail?: string
  errors?: Record<string, string[]>
}

/** Extracts a human-readable message from an API error (RFC 7807 ProblemDetails). */
export function getErrorMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    const data = error.response?.data as ProblemDetails | undefined

    if (data?.errors) {
      const firstField = Object.values(data.errors)[0]
      if (firstField?.length) {
        return firstField[0]
      }
    }

    return data?.detail ?? data?.title ?? error.message
  }

  return error instanceof Error ? error.message : 'Something went wrong.'
}
