import { useState } from 'react';
import { PhoneCall, ShieldAlert, X } from 'lucide-react';
import type { RecursoEmergenciaResource } from '../types';

interface CrisisAlertViewProps {
  recursos: RecursoEmergenciaResource[];
  alertaCerrada?: boolean;
  onCloseAlert: () => Promise<void> | void;
  closing?: boolean;
  errorMessage?: string | null;
}

function extractPhoneDigits(lineaTelefonica: string): string {
  const match = lineaTelefonica.match(/\d+/);
  return match ? match[0] : lineaTelefonica.replace(/\s+/g, '');
}

export function CrisisAlertView({
  recursos,
  alertaCerrada = false,
  onCloseAlert,
  closing = false,
  errorMessage = null,
}: CrisisAlertViewProps) {
  const [confirmingClose, setConfirmingClose] = useState(false);

  return (
    <section
      aria-live="assertive"
      className="mx-auto max-w-3xl space-y-6 rounded-3xl border border-rose-200 bg-white p-6 shadow-sm sm:p-10"
    >
      <div className="flex flex-col gap-4 rounded-2xl border border-rose-200 bg-rose-50/70 p-5 sm:flex-row sm:items-start">
        <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-rose-600 text-white">
          <ShieldAlert className="h-6 w-6" />
        </span>
        <div className="space-y-2">
          <span className="inline-block rounded-full bg-rose-100 px-3 py-0.5 text-xs font-semibold text-rose-800">
            Protocolo de Contención Inmediata · Prioridad Clínica
          </span>
          <h1 className="text-2xl font-bold text-slate-900">Tu bienestar es lo más importante en este momento</h1>
          <p className="text-sm leading-relaxed text-slate-700">
            Detectamos señales que requieren atención humana inmediata. Por tu seguridad, hemos detenido la búsqueda de
            psicólogos de rutina. Te recomendamos comunicarte ahora mismo con un servicio de emergencias o con una de
            las líneas gratuitas de orientación profesional disponibles 24/7 en Perú.
          </p>
        </div>
      </div>

      <div className="space-y-3">
        <h2 className="text-base font-bold text-slate-900">Líneas de ayuda inmediata y emergencias (Perú)</h2>
        <div className="grid gap-3 sm:grid-cols-2">
          {recursos.map((recurso) => {
            const telHref = `tel:${extractPhoneDigits(recurso.lineaTelefonica)}`;
            return (
              <article
                key={`${recurso.nombreEntidad}-${recurso.lineaTelefonica}`}
                className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-mindfind-gray p-5"
              >
                <div className="space-y-2">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="text-sm font-bold text-slate-900">{recurso.nombreEntidad}</h3>
                    <span className="shrink-0 rounded-full bg-mindfind-teal-soft px-2.5 py-0.5 text-[11px] font-semibold text-mindfind-teal">
                      {recurso.disponibilidad}
                    </span>
                  </div>
                  <p className="text-xs leading-relaxed text-slate-600">{recurso.descripcion}</p>
                </div>

                <a
                  href={telHref}
                  className="mt-4 inline-flex items-center justify-center gap-2 rounded-xl bg-mindfind-teal px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-teal-800"
                >
                  <PhoneCall className="h-4 w-4" />
                  <span>Llamar {recurso.lineaTelefonica}</span>
                </a>
              </article>
            );
          })}
        </div>
      </div>

      <div className="rounded-2xl border border-mindfind-lavender-border bg-mindfind-lavender-soft p-4 text-xs leading-relaxed text-slate-700">
        Si te encuentras en peligro físico inmediato, acude al establecimiento de salud más cercano o llama al{' '}
        <strong className="text-slate-900">106 (SAMU)</strong> o <strong className="text-slate-900">105 (PNP)</strong>.
        No estás solo; hay profesionales capacitados listos para escucharte y acompañarte.
      </div>

      {errorMessage && (
        <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3.5 text-sm text-red-800">
          {errorMessage}
        </div>
      )}

      <div className="flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-5">
        <p className="text-xs text-slate-500">
          El emparejamiento automático permanece bloqueado para proteger tu seguridad.
        </p>

        {!alertaCerrada && !confirmingClose && (
          <button
            type="button"
            onClick={() => setConfirmingClose(true)}
            className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
          >
            <X className="h-4 w-4" />
            <span>Cerrar alerta</span>
          </button>
        )}

        {!alertaCerrada && confirmingClose && (
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-xs font-medium text-slate-600">¿Confirmas cerrar esta pantalla?</span>
            <button
              type="button"
              onClick={() => setConfirmingClose(false)}
              disabled={closing}
              className="rounded-xl border border-slate-200 px-3.5 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-50"
            >
              Seguir aquí
            </button>
            <button
              type="button"
              onClick={() => void onCloseAlert()}
              disabled={closing}
              className="rounded-xl bg-slate-800 px-4 py-2 text-xs font-semibold text-white hover:bg-slate-900 disabled:opacity-60"
            >
              {closing ? 'Cerrando...' : 'Cerrar alerta y salir'}
            </button>
          </div>
        )}
      </div>
    </section>
  );
}
