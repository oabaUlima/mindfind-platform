import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  AlertCircle,
  ArrowLeft,
  ArrowRight,
  Lock,
  Plus,
  Send,
  ShieldCheck,
} from 'lucide-react';
import { ENFOQUES_TEORICOS } from '../../identidad_acreditacion/types';
import { ApiError } from '../../shared/api/httpClient';
import { CrisisAlertView } from '../components/CrisisAlertView';
import { checkCrisisKeywords, submitTriageEvaluation } from '../services/triageApi';
import {
  clearTriageDraft,
  DISPONIBILIDADES_SUGERIDAS,
  EMPTY_TRIAGE_DRAFT,
  loadTriageDraft,
  saveTriageDraft,
  SINTOMAS_SUGERIDOS,
  type ModalidadPreferida,
  type RecursoEmergenciaResource,
  type TriageDraft,
} from '../types';

const REQUIRED_QUESTION_MSG = 'Debe responder esta pregunta para continuar';
const SERVER_ERROR_MSG = 'No se pudo generar tu lista de psicólogos. Por favor, reintente en unos momentos';

export function TriageQuestionnairePage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [draft, setDraft] = useState<TriageDraft>(() => {
    if (searchParams.get('nuevo') === '1') {
      clearTriageDraft();
      return EMPTY_TRIAGE_DRAFT;
    }
    return loadTriageDraft();
  });

  const [customSymptom, setCustomSymptom] = useState('');
  const [stepError, setStepError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [inlineCrisisResources, setInlineCrisisResources] = useState<RecursoEmergenciaResource[] | null>(null);

  useEffect(() => {
    if (searchParams.get('nuevo') === '1') {
      clearTriageDraft();
      setDraft(EMPTY_TRIAGE_DRAFT);
      setSearchParams({}, { replace: true });
    }
  }, [searchParams, setSearchParams]);

  useEffect(() => {
    saveTriageDraft(draft);
  }, [draft]);

  const updateDraft = <K extends keyof TriageDraft>(field: K, value: TriageDraft[K]) => {
    setStepError(null);
    setDraft((prev) => ({ ...prev, [field]: value }));
  };

  const toggleSymptom = (symptom: string) => {
    setStepError(null);
    setDraft((prev) => ({
      ...prev,
      sintomasDeclarados: prev.sintomasDeclarados.includes(symptom)
        ? prev.sintomasDeclarados.filter((s) => s !== symptom)
        : [...prev.sintomasDeclarados, symptom],
    }));
  };

  const addCustomSymptom = () => {
    const clean = customSymptom.trim();
    if (!clean) return;
    if (!draft.sintomasDeclarados.includes(clean)) {
      updateDraft('sintomasDeclarados', [...draft.sintomasDeclarados, clean]);
    }
    setCustomSymptom('');
  };

  const validateCurrentStep = (currentStep: number): string | null => {
    if (currentStep === 1) {
      const edad = Number(draft.edadPaciente);
      if (!draft.edadPaciente.trim() || Number.isNaN(edad) || !draft.motivoConsulta.trim()) {
        return REQUIRED_QUESTION_MSG;
      }
      if (edad <= 0 || edad > 120) {
        return 'La edad del paciente debe estar entre 1 y 120 años';
      }
    }
    if (currentStep === 2) {
      if (draft.sintomasDeclarados.length === 0) {
        return REQUIRED_QUESTION_MSG;
      }
    }
    if (currentStep === 3) {
      const presupuesto = Number(draft.presupuestoMaximo);
      if (
        !draft.modalidadPreferida ||
        !draft.presupuestoMaximo.trim() ||
        Number.isNaN(presupuesto) ||
        !draft.disponibilidadPreferida.trim()
      ) {
        return REQUIRED_QUESTION_MSG;
      }
      if (presupuesto <= 0) {
        return 'El presupuesto máximo debe ser mayor a 0 PEN';
      }
    }
    if (currentStep === 4) {
      if (!draft.consentimientoCifrado) {
        return REQUIRED_QUESTION_MSG;
      }
    }
    return null;
  };

  /**
   * Registra inmediatamente la evaluación en estado INTERRUMPIDA_POR_CRISIS cuando
   * /triage/crisis-check detecta riesgo en pasos tempranos (ajuste preventivo 2)
   * y limpia localStorage para evitar bucles al reingresar (ajuste preventivo 3).
   */
  const registerImmediateCrisisEvaluation = async (fallbackResources: RecursoEmergenciaResource[]) => {
    const parsedEdad = Number(draft.edadPaciente);
    const parsedBudget = Number(draft.presupuestoMaximo);

    try {
      const evaluation = await submitTriageEvaluation({
        nombrePaciente: draft.nombrePaciente.trim() || null,
        edadPaciente: !Number.isNaN(parsedEdad) && parsedEdad > 0 && parsedEdad <= 120 ? parsedEdad : 18,
        motivoConsulta: draft.motivoConsulta.trim() || 'Evaluación interrumpida por protocolo de crisis',
        sintomasDeclarados:
          draft.sintomasDeclarados.length > 0 ? draft.sintomasDeclarados : ['malestar emocional'],
        modalidadPreferida: draft.modalidadPreferida || 'VIRTUAL',
        presupuestoMaximo: !Number.isNaN(parsedBudget) && parsedBudget > 0 ? parsedBudget : 100,
        disponibilidadPreferida: draft.disponibilidadPreferida.trim() || 'Inmediata',
        enfoquePreferido: draft.enfoquePreferido || null,
        preferenciasProfesional: draft.preferenciasProfesional.trim() || null,
        consentimientoCifrado: true,
        indicadorRiesgoInminente: draft.indicadorRiesgoInminente,
      });
      clearTriageDraft();
      navigate(`/triage/crisis/${evaluation.id}`, {
        replace: true,
        state: { evaluation },
      });
    } catch {
      clearTriageDraft();
      setInlineCrisisResources(fallbackResources);
    }
  };

  const handleNextStep = async () => {
    const validationError = validateCurrentStep(draft.step);
    if (validationError) {
      setStepError(validationError);
      return;
    }

    setStepError(null);

    if (draft.step === 1 || draft.step === 2) {
      setLoading(true);
      try {
        const crisisCheck = await checkCrisisKeywords({
          motivoConsulta: draft.motivoConsulta,
          sintomasDeclarados: draft.sintomasDeclarados,
          preferenciasProfesional: draft.preferenciasProfesional,
          indicadorRiesgoInminente: draft.indicadorRiesgoInminente,
        });

        if (crisisCheck.crisisDetectada) {
          await registerImmediateCrisisEvaluation(crisisCheck.recursosEmergencia);
          return;
        }
      } catch (err) {
        // Si no hay conexión durante el check preventivo, no bloqueamos el avance; el backend evaluará al final
        if (err instanceof ApiError && !err.isNetworkError && err.status >= 400 && err.status < 500) {
          setStepError(err.detail);
          setLoading(false);
          return;
        }
      } finally {
        setLoading(false);
      }
    }

    updateDraft('step', Math.min(4, draft.step + 1));
  };

  const handleSubmitFinal = async () => {
    const validationError = validateCurrentStep(4);
    if (validationError) {
      setStepError(validationError);
      return;
    }

    setStepError(null);
    setLoading(true);

    try {
      const crisisCheck = await checkCrisisKeywords({
        motivoConsulta: draft.motivoConsulta,
        sintomasDeclarados: draft.sintomasDeclarados,
        preferenciasProfesional: draft.preferenciasProfesional,
        indicadorRiesgoInminente: draft.indicadorRiesgoInminente,
      });

      if (crisisCheck.crisisDetectada) {
        await registerImmediateCrisisEvaluation(crisisCheck.recursosEmergencia);
        return;
      }

      const evaluation = await submitTriageEvaluation({
        nombrePaciente: draft.nombrePaciente.trim() || null,
        edadPaciente: Number(draft.edadPaciente),
        motivoConsulta: draft.motivoConsulta.trim(),
        sintomasDeclarados: draft.sintomasDeclarados,
        modalidadPreferida: draft.modalidadPreferida,
        presupuestoMaximo: Number(draft.presupuestoMaximo),
        disponibilidadPreferida: draft.disponibilidadPreferida.trim(),
        enfoquePreferido: draft.enfoquePreferido || null,
        preferenciasProfesional: draft.preferenciasProfesional.trim() || null,
        consentimientoCifrado: draft.consentimientoCifrado,
        indicadorRiesgoInminente: draft.indicadorRiesgoInminente,
      });

      if (evaluation.estado === 'INTERRUMPIDA_POR_CRISIS') {
        clearTriageDraft();
        navigate(`/triage/crisis/${evaluation.id}`, {
          replace: true,
          state: { evaluation },
        });
        return;
      }

      if (evaluation.resultadosCompatibilidad.length > 0) {
        clearTriageDraft();
      }
      navigate(`/triage/results/${evaluation.id}`, {
        state: { evaluation },
      });
    } catch (err) {
      if (err instanceof ApiError && !err.isNetworkError && err.status >= 400 && err.status < 500) {
        setStepError(err.detail);
      } else {
        setStepError(SERVER_ERROR_MSG);
      }
    } finally {
      setLoading(false);
    }
  };

  if (inlineCrisisResources) {
    return (
      <CrisisAlertView
        recursos={inlineCrisisResources}
        onCloseAlert={() => {
          clearTriageDraft();
          navigate('/', { replace: true });
        }}
      />
    );
  }

  const progressPercent = (draft.step / 4) * 100;

  return (
    <section className="mx-auto max-w-3xl space-y-6">
      {/* Encabezado y barra de progreso */}
      <div className="rounded-3xl border border-mindfind-lavender-border bg-white p-6 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div>
            <span className="text-xs font-semibold uppercase tracking-wide text-mindfind-teal">
              Cuestionario de Triage · Paso {draft.step} de 4
            </span>
            <h1 className="mt-0.5 text-2xl font-bold text-slate-900">Evaluación inicial confidencial</h1>
          </div>
          <span className="inline-flex items-center gap-1.5 rounded-full bg-mindfind-lavender-soft px-3 py-1 text-xs font-medium text-mindfind-teal ring-1 ring-mindfind-lavender-border">
            <Lock className="h-3.5 w-3.5" />
            <span>Borrador protegido localmente</span>
          </span>
        </div>

        <div className="mt-4 h-2.5 w-full overflow-hidden rounded-full bg-mindfind-gray-muted">
          <div
            className="h-full rounded-full bg-mindfind-teal transition-all duration-300"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>

      {/* Cuerpo del paso */}
      <div className="space-y-6 rounded-3xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
        {stepError && (
          <div
            role="alert"
            className="flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-800"
          >
            <AlertCircle className="mt-0.5 h-5 w-5 shrink-0 text-red-600" />
            <span>{stepError}</span>
          </div>
        )}

        {draft.step === 1 && (
          <div className="space-y-5">
            <div>
              <h2 className="text-lg font-bold text-slate-900">Cuéntanos brevemente sobre ti</h2>
              <p className="text-sm text-slate-600">
                Tu nombre es opcional si prefieres mantener tu consulta reservada.
              </p>
            </div>

            <div className="grid gap-5 sm:grid-cols-2">
              <div>
                <label htmlFor="nombrePaciente" className="block text-sm font-semibold text-slate-700">
                  Nombre referencial (opcional)
                </label>
                <input
                  id="nombrePaciente"
                  type="text"
                  value={draft.nombrePaciente}
                  onChange={(e) => updateDraft('nombrePaciente', e.target.value)}
                  placeholder="Ej. Lucía o anónimo"
                  className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
                />
              </div>

              <div>
                <label htmlFor="edadPaciente" className="block text-sm font-semibold text-slate-700">
                  Edad <span className="text-red-500">*</span>
                </label>
                <input
                  id="edadPaciente"
                  type="number"
                  min={1}
                  max={120}
                  value={draft.edadPaciente}
                  onChange={(e) => updateDraft('edadPaciente', e.target.value)}
                  placeholder="Ej. 22"
                  className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
                />
              </div>
            </div>

            <div>
              <label htmlFor="motivoConsulta" className="block text-sm font-semibold text-slate-700">
                ¿Cuál es tu motivo principal de consulta? <span className="text-red-500">*</span>
              </label>
              <textarea
                id="motivoConsulta"
                rows={4}
                value={draft.motivoConsulta}
                onChange={(e) => updateDraft('motivoConsulta', e.target.value)}
                placeholder="Describe con tus palabras qué te trae hoy y cómo te has sentido últimamente..."
                className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
              />
            </div>
          </div>
        )}

        {draft.step === 2 && (
          <div className="space-y-5">
            <div>
              <h2 className="text-lg font-bold text-slate-900">Síntomas o áreas que deseas trabajar</h2>
              <p className="text-sm text-slate-600">
                Selecciona al menos una opción o escribe cómo se manifiesta lo que sientes.
              </p>
            </div>

            <div className="flex flex-wrap gap-2">
              {Array.from(new Set([...SINTOMAS_SUGERIDOS, ...draft.sintomasDeclarados])).map((symptom) => {
                const selected = draft.sintomasDeclarados.includes(symptom);
                return (
                  <button
                    key={symptom}
                    type="button"
                    onClick={() => toggleSymptom(symptom)}
                    className={`rounded-full px-4 py-2 text-xs font-semibold transition ${
                      selected
                        ? 'bg-mindfind-teal text-white shadow-sm'
                        : 'border border-slate-200 bg-mindfind-gray text-slate-700 hover:bg-mindfind-lavender'
                    }`}
                  >
                    {symptom}
                  </button>
                );
              })}
            </div>

            <div className="flex gap-2">
              <input
                type="text"
                value={customSymptom}
                onChange={(e) => setCustomSymptom(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    addCustomSymptom();
                  }
                }}
                placeholder="Otro síntoma o dificultad..."
                className="flex-1 rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
              />
              <button
                type="button"
                onClick={addCustomSymptom}
                className="inline-flex items-center gap-1 rounded-xl border border-mindfind-lavender-border bg-mindfind-lavender-soft px-4 py-2 text-xs font-semibold text-mindfind-teal hover:bg-mindfind-lavender"
              >
                <Plus className="h-4 w-4" />
                <span>Agregar</span>
              </button>
            </div>

            <label className="flex cursor-pointer items-start gap-3 rounded-2xl border border-amber-200 bg-amber-50/70 p-4 text-sm text-slate-800">
              <input
                type="checkbox"
                checked={draft.indicadorRiesgoInminente}
                onChange={(e) => updateDraft('indicadorRiesgoInminente', e.target.checked)}
                className="mt-1 h-4 w-4 rounded border-amber-400 text-rose-600 focus:ring-rose-500"
              />
              <span>
                <strong className="block font-semibold text-slate-900">
                  Estoy atravesando una crisis o siento riesgo inmediato de hacerme daño / sufrir violencia
                </strong>
                <span className="text-xs text-slate-600">
                  Opcional: marca esta casilla si necesitas contención de emergencia inmediata.
                </span>
              </span>
            </label>
          </div>
        )}

        {draft.step === 3 && (
          <div className="space-y-5">
            <div>
              <h2 className="text-lg font-bold text-slate-900">Modalidad, presupuesto y horarios</h2>
              <p className="text-sm text-slate-600">
                Usamos estas preferencias para priorizar especialistas compatibles contigo.
              </p>
            </div>

            <div>
              <span className="block text-sm font-semibold text-slate-700">
                Modalidad preferida <span className="text-red-500">*</span>
              </span>
              <div className="mt-2 grid grid-cols-2 gap-3">
                {(['VIRTUAL', 'PRESENCIAL'] as ModalidadPreferida[]).map((mod) => {
                  const selected = draft.modalidadPreferida === mod;
                  return (
                    <button
                      key={mod}
                      type="button"
                      onClick={() => updateDraft('modalidadPreferida', mod)}
                      className={`rounded-2xl border p-4 text-left transition ${
                        selected
                          ? 'border-mindfind-teal bg-mindfind-teal-soft/40 text-mindfind-teal shadow-sm'
                          : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                      }`}
                    >
                      <span className="block text-sm font-bold">
                        {mod === 'VIRTUAL' ? 'Virtual (Videollamada)' : 'Presencial (Consultorio)'}
                      </span>
                      <span className="mt-0.5 block text-xs text-slate-500">
                        {mod === 'VIRTUAL' ? 'Desde cualquier lugar' : 'Atención en sede del especialista'}
                      </span>
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="grid gap-5 sm:grid-cols-2">
              <div>
                <label htmlFor="presupuestoMaximo" className="block text-sm font-semibold text-slate-700">
                  Presupuesto máximo por sesión (PEN) <span className="text-red-500">*</span>
                </label>
                <input
                  id="presupuestoMaximo"
                  type="number"
                  min={1}
                  step="1"
                  value={draft.presupuestoMaximo}
                  onChange={(e) => updateDraft('presupuestoMaximo', e.target.value)}
                  placeholder="Ej. 130"
                  className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
                />
              </div>

              <div>
                <label htmlFor="disponibilidadPreferida" className="block text-sm font-semibold text-slate-700">
                  Disponibilidad horaria <span className="text-red-500">*</span>
                </label>
                <input
                  id="disponibilidadPreferida"
                  type="text"
                  value={draft.disponibilidadPreferida}
                  onChange={(e) => updateDraft('disponibilidadPreferida', e.target.value)}
                  placeholder="Ej. Mañana, Tarde o Noche"
                  className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
                />
                <div className="mt-2 flex flex-wrap gap-1.5">
                  {DISPONIBILIDADES_SUGERIDAS.map((slot) => (
                    <button
                      key={slot}
                      type="button"
                      onClick={() => updateDraft('disponibilidadPreferida', slot)}
                      className={`rounded-lg px-2.5 py-1 text-xs font-medium ${
                        draft.disponibilidadPreferida === slot
                          ? 'bg-mindfind-teal text-white'
                          : 'bg-mindfind-gray-muted text-slate-600 hover:bg-mindfind-lavender'
                      }`}
                    >
                      {slot}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {draft.step === 4 && (
          <div className="space-y-5">
            <div>
              <h2 className="text-lg font-bold text-slate-900">Preferencias opcionales y confidencialidad</h2>
              <p className="text-sm text-slate-600">
                Si no tienes preferencia de enfoque terapéutico, nuestro motor asignará un puntaje neutro favorable.
              </p>
            </div>

            <div>
              <label htmlFor="enfoquePreferido" className="block text-sm font-semibold text-slate-700">
                Enfoque teórico preferido (opcional)
              </label>
              <select
                id="enfoquePreferido"
                value={draft.enfoquePreferido}
                onChange={(e) => updateDraft('enfoquePreferido', e.target.value)}
                className="mt-1.5 w-full rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
              >
                <option value="">Sin preferencia</option>
                {ENFOQUES_TEORICOS.map((item) => (
                  <option key={item.value} value={item.value}>
                    {item.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label htmlFor="preferenciasProfesional" className="block text-sm font-semibold text-slate-700">
                Preferencias adicionales sobre tu psicólogo (opcional)
              </label>
              <textarea
                id="preferenciasProfesional"
                rows={3}
                value={draft.preferenciasProfesional}
                onChange={(e) => updateDraft('preferenciasProfesional', e.target.value)}
                placeholder="Ej. Trato cálido, experiencia con estudiantes universitarios..."
                className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
              />
            </div>

            <label className="flex cursor-pointer items-start gap-3 rounded-2xl border border-mindfind-lavender-border bg-mindfind-lavender-soft p-4 text-sm">
              <input
                type="checkbox"
                checked={draft.consentimientoCifrado}
                onChange={(e) => updateDraft('consentimientoCifrado', e.target.checked)}
                className="mt-1 h-4 w-4 rounded border-slate-300 text-mindfind-teal focus:ring-mindfind-teal"
              />
              <span className="flex-1 text-slate-700">
                <strong className="flex items-center gap-1.5 font-semibold text-slate-900">
                  <ShieldCheck className="h-4 w-4 text-mindfind-teal" />
                  Acepto el consentimiento de confidencialidad y cifrado clínico <span className="text-red-500">*</span>
                </strong>
                <span className="mt-0.5 block text-xs text-slate-600">
                  Autorizo que MindFind procese mis respuestas exclusivamente para evaluar riesgo y recomendar
                  especialistas acreditados.
                </span>
              </span>
            </label>
          </div>
        )}

        {/* Navegación del asistente */}
        <div className="flex items-center justify-between border-t border-slate-100 pt-5">
          <button
            type="button"
            onClick={() => updateDraft('step', Math.max(1, draft.step - 1))}
            disabled={draft.step === 1 || loading}
            className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
          >
            <ArrowLeft className="h-4 w-4" />
            <span>Anterior</span>
          </button>

          {draft.step < 4 ? (
            <button
              type="button"
              onClick={() => void handleNextStep()}
              disabled={loading}
              className="inline-flex items-center gap-2 rounded-xl bg-mindfind-teal px-6 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-teal-800 disabled:opacity-60"
            >
              <span>{loading ? 'Validando...' : 'Siguiente'}</span>
              <ArrowRight className="h-4 w-4" />
            </button>
          ) : (
            <button
              type="button"
              onClick={() => void handleSubmitFinal()}
              disabled={loading}
              className="inline-flex items-center gap-2 rounded-xl bg-mindfind-teal px-6 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-teal-800 disabled:opacity-60"
            >
              <Send className="h-4 w-4" />
              <span>{loading ? 'Calculando compatibilidad...' : 'Enviar evaluación'}</span>
            </button>
          )}
        </div>
      </div>
    </section>
  );
}
