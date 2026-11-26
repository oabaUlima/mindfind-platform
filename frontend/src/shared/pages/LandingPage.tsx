import { Link } from 'react-router-dom';
import {
  ArrowRight,
  BadgeCheck,
  ClipboardCheck,
  ShieldAlert,
  Sparkles,
  UserPlus,
} from 'lucide-react';

export function LandingPage() {
  return (
    <div className="space-y-12">
      {/* Hero */}
      <section className="relative overflow-hidden rounded-3xl border border-mindfind-lavender-border bg-gradient-to-br from-white via-mindfind-lavender-soft to-mindfind-teal-soft/50 p-8 shadow-sm sm:p-12">
        <div className="max-w-3xl space-y-6">
          <span className="inline-flex items-center gap-2 rounded-full border border-mindfind-lavender-border bg-white px-3.5 py-1 text-xs font-semibold text-mindfind-teal shadow-sm">
            <Sparkles className="h-3.5 w-3.5 text-mindfind-teal-light" />
            MindFind · Plataforma de Triage y Emparejamiento Psicológico
          </span>

          <h1 className="text-3xl font-bold tracking-tight text-slate-900 sm:text-5xl sm:leading-tight">
            Encuentra al psicólogo clínico ideal para lo que estás atravesando hoy
          </h1>

          <p className="text-base leading-relaxed text-slate-600 sm:text-lg">
            Completa una evaluación breve y confidencial de tus síntomas, modalidad y presupuesto. MindFind prioriza tu
            seguridad ante cualquier señal de crisis y calcula tu ranking personalizado con especialistas colegiados y
            verificados.
          </p>

          <div className="flex flex-wrap items-center gap-3 pt-2">
            <Link
              to="/triage?nuevo=1"
              className="inline-flex items-center gap-2 rounded-xl bg-mindfind-teal px-6 py-3.5 text-sm font-semibold text-white shadow-sm transition hover:bg-teal-800"
            >
              <span>Iniciar evaluación (Paciente)</span>
              <ArrowRight className="h-4 w-4" />
            </Link>

            <Link
              to="/psychologists/register"
              className="inline-flex items-center gap-2 rounded-xl border border-mindfind-teal/30 bg-white px-5 py-3.5 text-sm font-semibold text-mindfind-teal shadow-sm transition hover:bg-mindfind-teal-soft/40"
            >
              <UserPlus className="h-4 w-4" />
              <span>Registrarme como psicólogo</span>
            </Link>

            <Link
              to="/psychologists/validation"
              className="inline-flex items-center gap-2 rounded-xl border border-mindfind-lavender-border bg-mindfind-lavender px-5 py-3.5 text-sm font-semibold text-slate-800 transition hover:bg-violet-200/70"
            >
              <BadgeCheck className="h-4 w-4 text-mindfind-teal" />
              <span>Panel de validación (Demo)</span>
            </Link>
          </div>
        </div>
      </section>

      {/* Beneficios y flujo */}
      <section className="grid gap-6 md:grid-cols-3">
        <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-mindfind-teal-soft text-mindfind-teal">
            <ClipboardCheck className="h-6 w-6" />
          </div>
          <h2 className="text-lg font-semibold text-slate-900">1. Cuestionario de ingreso</h2>
          <p className="mt-2 text-sm leading-relaxed text-slate-600">
            Cuatro pasos guiados para registrar tu motivo de consulta, síntomas, modalidad presencial o virtual,
            presupuesto en soles (PEN) y disponibilidad horaria.
          </p>
        </article>

        <article className="rounded-2xl border border-mindfind-lavender-border bg-mindfind-lavender-soft p-6 shadow-sm">
          <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-white text-mindfind-teal shadow-sm">
            <ShieldAlert className="h-6 w-6" />
          </div>
          <h2 className="text-lg font-semibold text-slate-900">2. Protocolo preventivo de crisis</h2>
          <p className="mt-2 text-sm leading-relaxed text-slate-600">
            Si detectamos indicadores de riesgo crítico, detenemos el flujo de emparejamiento y te brindamos acceso
            inmediato a líneas de emergencia gratuitas 24/7 en Perú.
          </p>
        </article>

        <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-mindfind-lavender text-mindfind-teal">
            <BadgeCheck className="h-6 w-6" />
          </div>
          <h2 className="text-lg font-semibold text-slate-900">3. Top 5 compatible y acreditado</h2>
          <p className="mt-2 text-sm leading-relaxed text-slate-600">
            Nuestro motor calcula tu compatibilidad ponderada únicamente con psicólogos verificados institucionalmente
            y explica claramente las razones de cada coincidencia.
          </p>
        </article>
      </section>
    </div>
  );
}
