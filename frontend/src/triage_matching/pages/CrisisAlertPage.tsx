import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { CheckCircle2 } from 'lucide-react';
import { ApiError } from '../../shared/api/httpClient';
import { CrisisAlertView } from '../components/CrisisAlertView';
import { closeCrisisAlert, getTriageEvaluationById } from '../services/triageApi';
import { clearTriageDraft, type RecursoEmergenciaResource, type TriageEvaluationResource } from '../types';

const FALLBACK_PERU_RESOURCES: RecursoEmergenciaResource[] = [
  {
    nombreEntidad: 'MINSA - Salud Mental (Línea 113 Opción 5)',
    lineaTelefonica: '113 Opción 5',
    descripcion: 'Orientación y consejería psicológica gratuita del Ministerio de Salud',
    disponibilidad: '24/7',
  },
  {
    nombreEntidad: 'SAMU',
    lineaTelefonica: '106',
    descripcion: 'Sistema de Atención Móvil de Urgencia para emergencias médicas en todo el Perú',
    disponibilidad: '24/7',
  },
  {
    nombreEntidad: 'MIMP - Línea 100',
    lineaTelefonica: '100',
    descripcion: 'Atención, contención y orientación gratuita ante violencia familiar o abuso',
    disponibilidad: '24/7',
  },
  {
    nombreEntidad: 'Policía Nacional del Perú',
    lineaTelefonica: '105',
    descripcion: 'Central de emergencias policiales ante riesgo físico inminente',
    disponibilidad: '24/7',
  },
];

export function CrisisAlertPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const stateEval = (location.state as { evaluation?: TriageEvaluationResource } | null)?.evaluation;

  const [evaluation, setEvaluation] = useState<TriageEvaluationResource | null>(stateEval ?? null);
  const [loading, setLoading] = useState(!stateEval);
  const [closing, setClosing] = useState(false);
  const [closedSuccess, setClosedSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    clearTriageDraft();
    const numericId = Number(id);
    if (!id || Number.isNaN(numericId)) {
      setLoading(false);
      return;
    }

    let active = true;
    void (async () => {
      try {
        const fetched = await getTriageEvaluationById(numericId);
        if (!active) return;
        if (fetched.estado !== 'INTERRUMPIDA_POR_CRISIS') {
          navigate(`/triage/results/${numericId}`, { replace: true });
          return;
        }
        setEvaluation(fetched);
        if (fetched.protocoloCrisis?.alertaCerrada) {
          setClosedSuccess(true);
        }
      } catch (err) {
        if (!active) return;
        if (err instanceof ApiError && err.status === 404) {
          setErrorMessage('No se encontró la evaluación solicitada.');
        }
      } finally {
        if (active) setLoading(false);
      }
    })();

    return () => {
      active = false;
    };
  }, [id, navigate]);

  const handleCloseAlert = async () => {
    const numericId = Number(id);
    if (!id || Number.isNaN(numericId)) {
      clearTriageDraft();
      navigate('/', { replace: true });
      return;
    }

    setClosing(true);
    setErrorMessage(null);
    try {
      const updated = await closeCrisisAlert(numericId);
      setEvaluation(updated);
      clearTriageDraft();
      setClosedSuccess(true);
    } catch (err) {
      setErrorMessage(
        err instanceof ApiError
          ? err.detail
          : 'No se pudo registrar el cierre de la alerta. Por favor, comunícate directamente con las líneas de ayuda.'
      );
    } finally {
      setClosing(false);
    }
  };

  if (loading) {
    return (
      <div className="mx-auto max-w-2xl rounded-3xl border border-slate-200 bg-white p-10 text-center text-sm text-slate-500">
        Cargando recursos de atención inmediata...
      </div>
    );
  }

  if (closedSuccess) {
    return (
      <section className="mx-auto max-w-xl space-y-5 rounded-3xl border border-mindfind-lavender-border bg-white p-8 text-center shadow-sm">
        <span className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-mindfind-teal-soft text-mindfind-teal">
          <CheckCircle2 className="h-6 w-6" />
        </span>
        <h1 className="text-xl font-bold text-slate-900">Alerta cerrada de forma segura</h1>
        <p className="text-sm leading-relaxed text-slate-600">
          Por precaución clínica, esta evaluación permanece cerrada sin continuar al emparejamiento automático. Recuerda
          que la Línea 113 Opción 5, el SAMU 106 y la Línea 100 están disponibles de forma gratuita las 24 horas.
        </p>
        <div className="pt-2">
          <Link
            to="/"
            className="inline-flex items-center justify-center rounded-xl bg-mindfind-teal px-6 py-3 text-sm font-semibold text-white hover:bg-teal-800"
          >
            Volver al inicio de MindFind
          </Link>
        </div>
      </section>
    );
  }

  const recursos =
    evaluation?.protocoloCrisis?.recursosEmergencia &&
    evaluation.protocoloCrisis.recursosEmergencia.length > 0
      ? evaluation.protocoloCrisis.recursosEmergencia
      : FALLBACK_PERU_RESOURCES;

  return (
    <CrisisAlertView
      recursos={recursos}
      alertaCerrada={Boolean(evaluation?.protocoloCrisis?.alertaCerrada)}
      onCloseAlert={handleCloseAlert}
      closing={closing}
      errorMessage={errorMessage}
    />
  );
}
