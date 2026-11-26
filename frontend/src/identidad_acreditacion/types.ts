export type EnfoqueTeorico =
  | 'COGNITIVO_CONDUCTUAL'
  | 'HUMANISTA'
  | 'SISTEMICO'
  | 'PSICODINAMICO'
  | 'INTEGRATIVO';

export type ModalidadAtencion = 'PRESENCIAL' | 'VIRTUAL';

export type EstadoVerificacion = 'PENDIENTE_VERIFICACION' | 'VERIFICADO';

export const ENFOQUES_TEORICOS: { value: EnfoqueTeorico; label: string }[] = [
  { value: 'COGNITIVO_CONDUCTUAL', label: 'Cognitivo-Conductual' },
  { value: 'HUMANISTA', label: 'Humanista' },
  { value: 'SISTEMICO', label: 'Sistémico' },
  { value: 'PSICODINAMICO', label: 'Psicodinámico' },
  { value: 'INTEGRATIVO', label: 'Integrativo' },
];

export const ENFOQUE_LABELS: Record<string, string> = {
  COGNITIVO_CONDUCTUAL: 'Cognitivo-Conductual',
  HUMANISTA: 'Humanista',
  SISTEMICO: 'Sistémico',
  PSICODINAMICO: 'Psicodinámico',
  INTEGRATIVO: 'Integrativo',
};

export const ESPECIALIDADES_SUGERIDAS = [
  'ansiedad',
  'depresión',
  'estrés laboral',
  'autoestima',
  'pareja',
  'duelo',
  'fobias',
  'trauma',
];

export interface RegisterPsychologistRequest {
  nombreCompleto: string;
  correo: string;
  telefono: string;
  numeroColegiatura: string;
  enfoqueTeorico: string;
  especialidades: string[];
  modalidadesAtencion: ModalidadAtencion[];
  tarifaPorSesion: number | null;
  disponibilidadHoraria: string;
}

export interface PsychologistResource {
  id: number;
  nombreCompleto: string;
  correo: string;
  telefono: string;
  numeroColegiatura: string;
  enfoqueTeorico: string;
  especialidades: string[];
  modalidadesAtencion: string[];
  tarifaPorSesion: number;
  moneda: string;
  disponibilidadHoraria: string;
  estadoVerificacion: EstadoVerificacion;
}

export interface RegisterPsychologistResponse {
  mensaje: string;
  psicologo: PsychologistResource;
}
