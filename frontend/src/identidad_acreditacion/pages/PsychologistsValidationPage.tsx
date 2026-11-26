import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { AlertCircle, BadgeCheck, CheckCircle2, Clock, RefreshCw, ShieldCheck, UserPlus } from 'lucide-react';
import { ApiError } from '../../shared/api/httpClient';
import { getPsychologists, verifyPsychologist } from '../services/psychologistsApi';
import { ENFOQUE_LABELS, type PsychologistResource } from '../types';

type FilterTab = 'ALL' | 'PENDIENTE_VERIFICACION' | 'VERIFICADO';

export function PsychologistsValidationPage() {
  const [psychologists, setPsychologists] = useState<PsychologistResource[]>([]);
  const [filter, setFilter] = useState<FilterTab>('ALL');
  const [loading, setLoading] = useState(true);
  const [verifyingId, setVerifyingId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null);

  const loadPsychologists = useCallback(async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await getPsychologists(false);
      setPsychologists(data);
    } catch (err) {
      setErrorMessage(
        err instanceof ApiError
          ? err.detail
          : 'No se pudo cargar la lista de psicólogos desde el servidor.'
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadPsychologists();
  }, [loadPsychologists]);

  const handleVerify = async (id: number) => {
    setVerifyingId(id);
    setErrorMessage(null);
    setFeedbackMessage(null);
    try {
      const updated = await verifyPsychologist(id);
      setPsychologists((prev) => prev.map((item) => (item.id === id ? updated : item)));
      setFeedbackMessage(
        `${updated.nombreCompleto} fue acreditado (VERIFICADO) y ya es elegible en el motor de matching.`
      );
    } catch (err) {
      setErrorMessage(err instanceof ApiError ? err.detail : 'No se pudo verificar el perfil.');
    } finally {
      setVerifyingId(null);
    }
  };

  const filtered = psychologists.filter((p) => (filter === 'ALL' ? true : p.estadoVerificacion === filter));
  const pendingCount = psychologists.filter((p) => p.estadoVerificacion === 'PENDIENTE_VERIFICACION').length;
  const verifiedCount = psychologists.filter((p) => p.estadoVerificacion === 'VERIFICADO').length;

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-mindfind-lavender-border bg-white p-6 shadow-sm">
        <div className="flex items-center gap-3">
          <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-mindfind-lavender text-mindfind-teal">
            <ShieldCheck className="h-6 w-6" />
          </span>
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Panel de Validación Institucional (Demo)</h1>
            <p className="text-sm text-slate-600">
              Solo los perfiles en estado <span className="font-semibold text-mindfind-teal">VERIFICADO</span> ingresan
              al ranking Top 5 mediante el ACL de MindFind.
            </p>
          </div>
        </div>

        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={() => void loadPsychologists()}
            disabled={loading}
            className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
            <span>Actualizar</span>
          </button>
          <Link
            to="/psychologists/register"
            className="inline-flex items-center gap-2 rounded-xl bg-mindfind-teal px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
          >
            <UserPlus className="h-4 w-4" />
            <span>Registrar psicólogo</span>
          </Link>
        </div>
      </div>

      {/* Filtros */}
      <div className="flex flex-wrap gap-2">
        {[
          { key: 'ALL' as FilterTab, label: `Todos (${psychologists.length})` },
          { key: 'PENDIENTE_VERIFICACION' as FilterTab, label: `Pendientes (${pendingCount})` },
          { key: 'VERIFICADO' as FilterTab, label: `Verificados (${verifiedCount})` },
        ].map((tab) => (
          <button
            key={tab.key}
            type="button"
            onClick={() => setFilter(tab.key)}
            className={`rounded-xl px-4 py-2 text-xs font-semibold transition ${
              filter === tab.key
                ? 'bg-mindfind-teal text-white shadow-sm'
                : 'border border-slate-200 bg-white text-slate-600 hover:bg-mindfind-gray-muted'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {feedbackMessage && (
        <div className="flex items-center gap-2.5 rounded-2xl border border-teal-200 bg-mindfind-teal-soft/40 p-4 text-sm font-medium text-teal-900">
          <CheckCircle2 className="h-5 w-5 shrink-0 text-mindfind-teal" />
          <span>{feedbackMessage}</span>
        </div>
      )}

      {errorMessage && (
        <div
          role="alert"
          className="flex items-center gap-2.5 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-800"
        >
          <AlertCircle className="h-5 w-5 shrink-0 text-red-600" />
          <span>{errorMessage}</span>
        </div>
      )}

      {loading ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-10 text-center text-sm text-slate-500">
          Cargando psicólogos registrados...
        </div>
      ) : filtered.length === 0 ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-10 text-center text-sm text-slate-500">
          No hay perfiles para el filtro seleccionado.
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {filtered.map((psicologo) => {
            const isVerified = psicologo.estadoVerificacion === 'VERIFICADO';
            return (
              <article
                key={psicologo.id}
                className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
              >
                <div className="space-y-3">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h2 className="text-lg font-bold text-slate-900">{psicologo.nombreCompleto}</h2>
                      <p className="text-xs text-slate-500">
                        CPsP {psicologo.numeroColegiatura} · {psicologo.correo}
                      </p>
                    </div>
                    {isVerified ? (
                      <span className="inline-flex items-center gap-1 rounded-full bg-mindfind-teal-soft px-3 py-1 text-xs font-semibold text-mindfind-teal">
                        <BadgeCheck className="h-3.5 w-3.5" />
                        VERIFICADO
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-800 ring-1 ring-amber-200">
                        <Clock className="h-3.5 w-3.5" />
                        PENDIENTE_VERIFICACION
                      </span>
                    )}
                  </div>

                  <div className="grid grid-cols-2 gap-2 rounded-xl bg-mindfind-gray p-3 text-xs">
                    <div>
                      <span className="text-slate-500">Enfoque:</span>{' '}
                      <span className="font-semibold text-slate-800">
                        {ENFOQUE_LABELS[psicologo.enfoqueTeorico] ?? psicologo.enfoqueTeorico}
                      </span>
                    </div>
                    <div>
                      <span className="text-slate-500">Tarifa:</span>{' '}
                      <span className="font-semibold text-slate-800">
                        S/ {Number(psicologo.tarifaPorSesion).toFixed(2)} {psicologo.moneda}
                      </span>
                    </div>
                    <div className="col-span-2">
                      <span className="text-slate-500">Horario:</span>{' '}
                      <span className="font-medium text-slate-700">{psicologo.disponibilidadHoraria}</span>
                    </div>
                    <div className="col-span-2">
                      <span className="text-slate-500">Modalidad:</span>{' '}
                      <span className="font-medium text-slate-700">
                        {psicologo.modalidadesAtencion.join(' · ')}
                      </span>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-1.5">
                    {psicologo.especialidades.map((esp) => (
                      <span
                        key={esp}
                        className="rounded-full bg-mindfind-lavender-soft px-2.5 py-0.5 text-xs font-medium text-slate-700 ring-1 ring-mindfind-lavender-border"
                      >
                        {esp}
                      </span>
                    ))}
                  </div>
                </div>

                {!isVerified && (
                  <div className="mt-5 border-t border-slate-100 pt-4">
                    <button
                      type="button"
                      onClick={() => void handleVerify(psicologo.id)}
                      disabled={verifyingId === psicologo.id}
                      className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-mindfind-teal px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-teal-800 disabled:opacity-60"
                    >
                      <BadgeCheck className="h-4 w-4" />
                      <span>
                        {verifyingId === psicologo.id ? 'Verificando...' : 'Verificar perfil'}
                      </span>
                    </button>
                  </div>
                )}
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}
