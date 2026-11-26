import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import {
  AlertCircle,
  BadgeCheck,
  CalendarClock,
  CheckCircle2,
  RefreshCw,
  SlidersHorizontal,
  Sparkles,
} from 'lucide-react';
import { ENFOQUE_LABELS } from '../../identidad_acreditacion/types';
import { ApiError } from '../../shared/api/httpClient';
import { getMatchingResultsByEvaluationId, getTriageEvaluationById } from '../services/triageApi';
import {
  draftFromEvaluation,
  saveTriageDraft,
  type MatchingResultResource,
  type TriageEvaluationResource,
} from '../types';

const NO_MATCHES_MESSAGE = 'No encontramos psicólogos que coincidan con todas tus preferencias';
const CONNECTION_ERROR_MESSAGE =
  'No se pudo generar tu lista de psicólogos. Por favor, reintente en unos momentos';

export function MatchingResultsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const stateEval = (location.state as { evaluation?: TriageEvaluationResource } | null)?.evaluation;

  const [evaluation, setEvaluation] = useState<TriageEvaluationResource | null>(stateEval ?? null);
  const [matches, setMatches] = useState<MatchingResultResource[]>(
    stateEval?.resultadosCompatibilidad ?? []
  );
  const [loading, setLoading] = useState(!stateEval);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    const numericId = Number(id);
    if (!id || Number.isNaN(numericId)) {
      setErrorMessage('Identificador de evaluación inválido.');
      setLoading(false);
      return;
    }

    setLoading(true);
    setErrorMessage(null);
    try {
      const [fetchedEval, fetchedMatches] = await Promise.all([
        getTriageEvaluationById(numericId),
        getMatchingResultsByEvaluationId(numericId),
      ]);

      if (fetchedEval.estado === 'INTERRUMPIDA_POR_CRISIS') {
        navigate(`/triage/crisis/${numericId}`, { replace: true, state: { evaluation: fetchedEval } });
        return;
      }

      setEvaluation(fetchedEval);
      setMatches(fetchedMatches);
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        navigate(`/triage/crisis/${numericId}`, { replace: true });
        return;
      }
      if (err instanceof ApiError && err.status === 404) {
        setErrorMessage(err.detail);
      } else {
        setErrorMessage(CONNECTION_ERROR_MESSAGE);
      }
    } finally {
      setLoading(false);
    }
  }, [id, navigate]);

  useEffect(() => {
    if (stateEval && stateEval.estado === 'INTERRUMPIDA_POR_CRISIS') {
      navigate(`/triage/crisis/${stateEval.id}`, { replace: true, state: { evaluation: stateEval } });
      return;
    }
    void loadData();
  }, [loadData, navigate, stateEval]);

  const handleAdjustAnswers = () => {
    if (evaluation) {
      saveTriageDraft(draftFromEvaluation(evaluation));
    }
    navigate('/triage');
  };

  if (loading) {
    return (
      <div className="mx-auto max-w-4xl rounded-3xl border border-slate-200 bg-white p-12 text-center text-sm text-slate-500">
        Calculando tu ranking Top 5 de psicólogos verificados...
      </div>
    );
  }

  if (errorMessage) {
    return (
      <section className="mx-auto max-w-2xl space-y-5 rounded-3xl border border-red-200 bg-white p-8 text-center shadow-sm">
        <AlertCircle className="mx-auto h-10 w-10 text-red-500" />
        <h1 className="text-xl font-bold text-slate-900">Hubo un problema al cargar tus resultados</h1>
        <p className="text-sm text-slate-600">{errorMessage}</p>
        <div className="flex flex-wrap justify-center gap-3">
          <button
            type="button"
            onClick={() => void loadData()}
            className="inline-flex items-center gap-2 rounded-xl bg-mindfind-teal px-5 py-2.5 text-sm font-semibold text-white hover:bg-teal-800"
          >
            <RefreshCw className="h-4 w-4" />
            <span>Reintentar</span>
          </button>
          <button
            type="button"
            onClick={handleAdjustAnswers}
            className="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            Volver al cuestionario
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="mx-auto max-w-4xl space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4 rounded-3xl border border-mindfind-lavender-border bg-gradient-to-r from-white to-mindfind-lavender-soft p-6 shadow-sm">
        <div className="space-y-1">
          <span className="inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide text-mindfind-teal">
            <Sparkles className="h-3.5 w-3.5" />
            Resultados de Emparejamiento · Evaluación #{evaluation?.id ?? id}
          </span>
          <h1 className="text-2xl font-bold text-slate-900">Especialistas recomendados para ti</h1>
          {evaluation && (
            <p className="text-xs text-slate-600">
              Síntomas: <span className="font-semibold">{evaluation.sintomasDeclarados.join(', ')}</span> · Modalidad:{' '}
              <span className="font-semibold">{evaluation.modalidadPreferida}</span> · Presupuesto máx.:{' '}
              <span className="font-semibold">
                S/ {Number(evaluation.presupuestoMaximo).toFixed(2)} {evaluation.moneda}
              </span>
            </p>
          )}
        </div>

        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={handleAdjustAnswers}
            className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50"
          >
            <SlidersHorizontal className="h-3.5 w-3.5" />
            <span>Ajustar respuestas</span>
          </button>
          <Link
            to="/triage?nuevo=1"
            className="rounded-xl bg-mindfind-teal px-4 py-2 text-xs font-semibold text-white hover:bg-teal-800"
          >
            Nueva evaluación
          </Link>
        </div>
      </div>

      {matches.length === 0 ? (
        <div className="space-y-4 rounded-3xl border border-slate-200 bg-white p-10 text-center shadow-sm">
          <p className="text-lg font-semibold text-slate-900">{NO_MATCHES_MESSAGE}</p>
          <p className="text-sm text-slate-600">
            Puedes ampliar tu presupuesto máximo, probar otra modalidad o ajustar tus horarios para encontrar más
            especialistas verificados.
          </p>
          <button
            type="button"
            onClick={handleAdjustAnswers}
            className="inline-flex items-center gap-2 rounded-xl bg-mindfind-teal px-6 py-3 text-sm font-semibold text-white hover:bg-teal-800"
          >
            <SlidersHorizontal className="h-4 w-4" />
            <span>Ajustar mis respuestas</span>
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {matches.map((match) => (
            <article
              key={match.idPsicologo}
              className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm transition hover:border-mindfind-teal/40"
            >
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div className="space-y-1.5">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="inline-flex h-7 w-7 items-center justify-center rounded-lg bg-mindfind-teal text-xs font-bold text-white">
                      #{match.posicionRanking}
                    </span>
                    <h2 className="text-lg font-bold text-slate-900">{match.nombrePsicologo}</h2>
                    <span className="inline-flex items-center gap-1 rounded-full bg-mindfind-teal-soft px-2.5 py-0.5 text-[11px] font-semibold text-mindfind-teal">
                      <BadgeCheck className="h-3.5 w-3.5" />
                      Verificado
                    </span>
                    {match.esMatchParcial && (
                      <span className="rounded-full bg-mindfind-lavender px-2.5 py-0.5 text-[11px] font-semibold text-slate-800 ring-1 ring-mindfind-lavender-border">
                        Coincidencia parcial
                      </span>
                    )}
                  </div>
                  <p className="text-sm font-medium text-mindfind-teal">
                    Enfoque {ENFOQUE_LABELS[match.enfoqueTeorico] ?? match.enfoqueTeorico}
                  </p>
                </div>

                {/* Porcentaje de compatibilidad */}
                <div className="min-w-[170px] rounded-2xl bg-mindfind-gray p-3 text-right">
                  <span className="block text-[11px] font-medium text-slate-500">Compatibilidad</span>
                  <span className="text-2xl font-extrabold text-mindfind-teal">
                    {match.porcentajeCompatibilidad.toFixed(1)}%
                  </span>
                  <div className="mt-1.5 h-2 w-full overflow-hidden rounded-full bg-slate-200">
                    <div
                      className="h-full rounded-full bg-mindfind-teal"
                      style={{ width: `${Math.min(100, Math.max(0, match.porcentajeCompatibilidad))}%` }}
                    />
                  </div>
                </div>
              </div>

              <div className="mt-4 grid gap-3 rounded-2xl bg-mindfind-gray p-4 text-xs sm:grid-cols-3">
                <div>
                  <span className="block text-slate-500">Tarifa por sesión</span>
                  <span className="font-bold text-slate-900">
                    S/ {Number(match.tarifaPorSesion).toFixed(2)} {match.moneda}
                  </span>
                </div>
                <div>
                  <span className="block text-slate-500">Modalidades</span>
                  <span className="font-semibold text-slate-800">{match.modalidadesAtencion.join(' · ')}</span>
                </div>
                <div>
                  <span className="flex items-center gap-1 text-slate-500">
                    <CalendarClock className="h-3.5 w-3.5" />
                    Disponibilidad
                  </span>
                  <span className="font-semibold text-slate-800">{match.disponibilidadHoraria}</span>
                </div>
              </div>

              <div className="mt-4 flex flex-wrap gap-1.5">
                {match.especialidades.map((esp) => (
                  <span
                    key={esp}
                    className="rounded-full bg-mindfind-lavender-soft px-3 py-1 text-xs font-medium text-slate-700 ring-1 ring-mindfind-lavender-border"
                  >
                    {esp}
                  </span>
                ))}
              </div>

              {match.razonesCompatibilidad.length > 0 && (
                <ul className="mt-4 space-y-1.5 border-t border-slate-100 pt-4 text-xs text-slate-600">
                  {match.razonesCompatibilidad.map((razon) => (
                    <li key={razon} className="flex items-start gap-2">
                      <CheckCircle2 className="mt-0.5 h-3.5 w-3.5 shrink-0 text-mindfind-teal" />
                      <span>{razon}</span>
                    </li>
                  ))}
                </ul>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
