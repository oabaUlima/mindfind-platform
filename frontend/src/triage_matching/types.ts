export type EstadoEvaluacion = 'EN_PROGRESO' | 'COMPLETADA' | 'INTERRUMPIDA_POR_CRISIS';

export type NivelRiesgo = 'BAJO' | 'MODERADO' | 'ALTO' | 'CRITICO';

export type ModalidadPreferida = 'PRESENCIAL' | 'VIRTUAL';

export const SINTOMAS_SUGERIDOS = [
  'ansiedad',
  'depresión',
  'estrés laboral',
  'autoestima',
  'duelo',
  'pareja',
  'insomnio',
  'desesperanza',
];

export const DISPONIBILIDADES_SUGERIDAS = [
  'Mañana',
  'Tarde',
  'Noche',
  'Indiferente',
];

export interface SubmitTriageEvaluationRequest {
  nombrePaciente?: string | null;
  edadPaciente: number | null;
  motivoConsulta: string;
  sintomasDeclarados: string[];
  modalidadPreferida: string;
  presupuestoMaximo: number | null;
  disponibilidadPreferida: string;
  enfoquePreferido?: string | null;
  preferenciasProfesional?: string | null;
  consentimientoCifrado: boolean | null;
  indicadorRiesgoInminente: boolean;
}

export interface CrisisCheckRequest {
  motivoConsulta?: string;
  sintomasDeclarados?: string[];
  preferenciasProfesional?: string;
  indicadorRiesgoInminente: boolean;
}

export interface RecursoEmergenciaResource {
  nombreEntidad: string;
  lineaTelefonica: string;
  descripcion: string;
  disponibilidad: string;
}

export interface CrisisCheckResponseResource {
  crisisDetectada: boolean;
  nivelRiesgo: NivelRiesgo;
  palabraDetectada: string | null;
  emparejamientoBloqueado: boolean;
  mensajeAlerta: string | null;
  recursosEmergencia: RecursoEmergenciaResource[];
}

export interface CrisisProtocolResource {
  palabraDetectada: string | null;
  nivelRiesgo: NivelRiesgo;
  emparejamientoBloqueado: boolean;
  alertaCerrada: boolean;
  recursosEmergencia: RecursoEmergenciaResource[];
}

export interface MatchingResultResource {
  idPsicologo: number;
  nombrePsicologo: string;
  enfoqueTeorico: string;
  especialidades: string[];
  modalidadesAtencion: string[];
  tarifaPorSesion: number;
  moneda: string;
  disponibilidadHoraria: string;
  porcentajeCompatibilidad: number;
  posicionRanking: number;
  esMatchParcial: boolean;
  razonesCompatibilidad: string[];
}

export interface TriageEvaluationResource {
  id: number;
  nombrePaciente: string | null;
  edadPaciente: number;
  motivoConsulta: string;
  sintomasDeclarados: string[];
  modalidadPreferida: string;
  presupuestoMaximo: number;
  moneda: string;
  disponibilidadPreferida: string;
  enfoquePreferido: string | null;
  preferenciasProfesional: string | null;
  consentimientoCifrado: boolean;
  estado: EstadoEvaluacion;
  nivelRiesgo: NivelRiesgo;
  fechaInicio: string;
  fechaFinalizacion: string | null;
  protocoloCrisis: CrisisProtocolResource | null;
  resultadosCompatibilidad: MatchingResultResource[];
}

export interface TriageDraft {
  step: number;
  nombrePaciente: string;
  edadPaciente: string;
  motivoConsulta: string;
  sintomasDeclarados: string[];
  modalidadPreferida: ModalidadPreferida | '';
  presupuestoMaximo: string;
  disponibilidadPreferida: string;
  enfoquePreferido: string;
  preferenciasProfesional: string;
  consentimientoCifrado: boolean;
  indicadorRiesgoInminente: boolean;
}

export const TRIAGE_DRAFT_STORAGE_KEY = 'mindfind_triage_draft_v1';

export const EMPTY_TRIAGE_DRAFT: TriageDraft = {
  step: 1,
  nombrePaciente: '',
  edadPaciente: '',
  motivoConsulta: '',
  sintomasDeclarados: [],
  modalidadPreferida: '',
  presupuestoMaximo: '',
  disponibilidadPreferida: '',
  enfoquePreferido: '',
  preferenciasProfesional: '',
  consentimientoCifrado: false,
  indicadorRiesgoInminente: false,
};

export function loadTriageDraft(): TriageDraft {
  try {
    const raw = localStorage.getItem(TRIAGE_DRAFT_STORAGE_KEY);
    if (!raw) return EMPTY_TRIAGE_DRAFT;
    const parsed = JSON.parse(raw) as Partial<TriageDraft>;
    return {
      ...EMPTY_TRIAGE_DRAFT,
      ...parsed,
      sintomasDeclarados: Array.isArray(parsed.sintomasDeclarados) ? parsed.sintomasDeclarados : [],
      step: typeof parsed.step === 'number' && parsed.step >= 1 && parsed.step <= 4 ? parsed.step : 1,
    };
  } catch {
    return EMPTY_TRIAGE_DRAFT;
  }
}

export function saveTriageDraft(draft: TriageDraft): void {
  try {
    localStorage.setItem(TRIAGE_DRAFT_STORAGE_KEY, JSON.stringify(draft));
  } catch {
    // Ignorar fallo de cuota en almacenamiento local
  }
}

export function clearTriageDraft(): void {
  try {
    localStorage.removeItem(TRIAGE_DRAFT_STORAGE_KEY);
  } catch {
    // Ignorar error de acceso a localStorage
  }
}

export function draftFromEvaluation(evaluation: TriageEvaluationResource): TriageDraft {
  return {
    step: 3,
    nombrePaciente: evaluation.nombrePaciente ?? '',
    edadPaciente: String(evaluation.edadPaciente ?? ''),
    motivoConsulta: evaluation.motivoConsulta ?? '',
    sintomasDeclarados: evaluation.sintomasDeclarados ?? [],
    modalidadPreferida:
      evaluation.modalidadPreferida === 'PRESENCIAL' || evaluation.modalidadPreferida === 'VIRTUAL'
        ? evaluation.modalidadPreferida
        : '',
    presupuestoMaximo: String(evaluation.presupuestoMaximo ?? ''),
    disponibilidadPreferida: evaluation.disponibilidadPreferida ?? '',
    enfoquePreferido: evaluation.enfoquePreferido ?? '',
    preferenciasProfesional: evaluation.preferenciasProfesional ?? '',
    consentimientoCifrado: evaluation.consentimientoCifrado,
    indicadorRiesgoInminente: false,
  };
}
