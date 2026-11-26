import { apiRequest } from '../../shared/api/httpClient';
import type {
  PsychologistResource,
  RegisterPsychologistRequest,
  RegisterPsychologistResponse,
} from '../types';

export function registerPsychologist(payload: RegisterPsychologistRequest): Promise<RegisterPsychologistResponse> {
  return apiRequest<RegisterPsychologistResponse>('/psychologists', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getPsychologists(soloVerificados = false): Promise<PsychologistResource[]> {
  const query = soloVerificados ? '?soloVerificados=true' : '';
  return apiRequest<PsychologistResource[]>(`/psychologists${query}`);
}

export function getPsychologistById(id: number): Promise<PsychologistResource> {
  return apiRequest<PsychologistResource>(`/psychologists/${id}`);
}

export function verifyPsychologist(id: number): Promise<PsychologistResource> {
  return apiRequest<PsychologistResource>(`/psychologists/${id}/verify`, {
    method: 'PATCH',
  });
}
