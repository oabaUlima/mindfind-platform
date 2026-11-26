const rawBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1').trim();

export const API_BASE_URL = rawBaseUrl.replace(/\/+$/, '');

export interface ProblemDetailPayload {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
}

export class ApiError extends Error {
  public readonly status: number;
  public readonly detail: string;
  public readonly isNetworkError: boolean;

  constructor(status: number, detail: string, isNetworkError = false) {
    super(detail);
    this.name = 'ApiError';
    this.status = status;
    this.detail = detail;
    this.isNetworkError = isNetworkError;
  }
}

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const url = `${API_BASE_URL}${normalizedPath}`;

  let response: Response;
  try {
    response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
      },
    });
  } catch {
    throw new ApiError(
      0,
      'No se pudo conectar con el servidor de MindFind. Verifica tu conexión e inténtalo nuevamente.',
      true
    );
  }

  if (!response.ok) {
    let detail = `Error HTTP ${response.status}`;
    try {
      const payload = (await response.json()) as ProblemDetailPayload;
      if (payload.detail && payload.detail.trim().length > 0) {
        detail = payload.detail;
      } else if (payload.title && payload.title.trim().length > 0) {
        detail = payload.title;
      }
    } catch {
      // Sin cuerpo JSON válido; se mantiene el mensaje por defecto
    }
    throw new ApiError(response.status, detail, response.status >= 500);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
