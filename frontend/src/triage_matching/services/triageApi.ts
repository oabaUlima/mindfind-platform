import { apiRequest } from '../../shared/api/httpClient';
import type {
  CrisisCheckRequest,
  CrisisCheckResponseResource,
  MatchingResultResource,
  SubmitTriageEvaluationRequest,
  TriageEvaluationResource,
} from '../types';

export function submitTriageEvaluation(
  payload: SubmitTriageEvaluationRequest
): Promise<TriageEvaluationResource> {
  return apiRequest<TriageEvaluationResource>('/triage/evaluations', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function checkCrisisKeywords(payload: CrisisCheckRequest): Promise<CrisisCheckResponseResource> {
  return apiRequest<CrisisCheckResponseResource>('/triage/crisis-check', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getTriageEvaluationById(id: number): Promise<TriageEvaluationResource> {
  return apiRequest<TriageEvaluationResource>(`/triage/evaluations/${id}`);
}

export function getMatchingResultsByEvaluationId(id: number): Promise<MatchingResultResource[]> {
  return apiRequest<MatchingResultResource[]>(`/triage/evaluations/${id}/matches`);
}

export function closeCrisisAlert(id: number): Promise<TriageEvaluationResource> {
  return apiRequest<TriageEvaluationResource>(`/triage/evaluations/${id}/crisis/close`, {
    method: 'PATCH',
  });
}
