import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { AlertCircle, BadgeCheck, CheckCircle2, Clock, Plus, UserPlus } from 'lucide-react';
import { ApiError } from '../../shared/api/httpClient';
import { registerPsychologist } from '../services/psychologistsApi';
import {
  ENFOQUE_LABELS,
  ENFOQUES_TEORICOS,
  ESPECIALIDADES_SUGERIDAS,
  type ModalidadAtencion,
  type RegisterPsychologistResponse,
} from '../types';

interface FormState {
  nombreCompleto: string;
  correo: string;
  telefono: string;
  numeroColegiatura: string;
  enfoqueTeorico: string;
  especialidades: string[];
  modalidadesAtencion: ModalidadAtencion[];
  tarifaPorSesion: string;
  disponibilidadHoraria: string;
}

const INITIAL_FORM: FormState = {
  nombreCompleto: '',
  correo: '',
  telefono: '',
  numeroColegiatura: '',
  enfoqueTeorico: '',
  especialidades: [],
  modalidadesAtencion: ['VIRTUAL'],
  tarifaPorSesion: '',
  disponibilidadHoraria: '',
};

const HORARIOS_SUGERIDOS = [
  'Lunes a Viernes 09:00 - 14:00 (Mañana)',
  'Lunes a Viernes 15:00 - 20:00 (Tarde/Noche)',
  'Martes a Sábado 10:00 - 18:00 (Mañana/Tarde)',
  'Lunes a Viernes 18:00 - 22:00 (Noche)',
];

export function RegisterPsychologistPage() {
  const [form, setForm] = useState<FormState>(INITIAL_FORM);
  const [customSpecialty, setCustomSpecialty] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [errorStatus, setErrorStatus] = useState<number | null>(null);
  const [createdResult, setCreatedResult] = useState<RegisterPsychologistResponse | null>(null);

  const hasLicenseError =
    errorMessage !== null &&
    (errorMessage.toLowerCase().includes('colegiatura') || errorStatus === 409);

  const toggleSpecialty = (item: string) => {
    setForm((prev) => ({
      ...prev,
      especialidades: prev.especialidades.includes(item)
        ? prev.especialidades.filter((e) => e !== item)
        : [...prev.especialidades, item],
    }));
  };

  const addCustomSpecialty = () => {
    const clean = customSpecialty.trim().toLowerCase();
    if (!clean) return;
    if (!form.especialidades.includes(clean)) {
      setForm((prev) => ({ ...prev, especialidades: [...prev.especialidades, clean] }));
    }
    setCustomSpecialty('');
  };

  const toggleModality = (mod: ModalidadAtencion) => {
    setForm((prev) => ({
      ...prev,
      modalidadesAtencion: prev.modalidadesAtencion.includes(mod)
        ? prev.modalidadesAtencion.filter((m) => m !== mod)
        : [...prev.modalidadesAtencion, mod],
    }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setErrorStatus(null);
    setSubmitting(true);

    const parsedTarifa = form.tarifaPorSesion.trim() ? Number(form.tarifaPorSesion) : null;

    try {
      const response = await registerPsychologist({
        nombreCompleto: form.nombreCompleto,
        correo: form.correo,
        telefono: form.telefono,
        numeroColegiatura: form.numeroColegiatura,
        enfoqueTeorico: form.enfoqueTeorico,
        especialidades: form.especialidades,
        modalidadesAtencion: form.modalidadesAtencion,
        tarifaPorSesion: parsedTarifa !== null && !Number.isNaN(parsedTarifa) ? parsedTarifa : null,
        disponibilidadHoraria: form.disponibilidadHoraria,
      });
      setCreatedResult(response);
    } catch (err) {
      if (err instanceof ApiError) {
        setErrorMessage(err.detail);
        setErrorStatus(err.status);
      } else {
        setErrorMessage('Ocurrió un error inesperado al registrar tu perfil.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (createdResult) {
    const { mensaje, psicologo } = createdResult;
    return (
      <section className="mx-auto max-w-2xl rounded-3xl border border-mindfind-lavender-border bg-white p-8 shadow-sm">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="flex items-center gap-3">
            <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-mindfind-teal-soft text-mindfind-teal">
              <CheckCircle2 className="h-7 w-7" />
            </span>
            <div>
              <h1 className="text-xl font-bold text-slate-900">Perfil profesional registrado</h1>
              <span className="mt-1 inline-flex items-center gap-1.5 rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-800 ring-1 ring-amber-200">
                <Clock className="h-3.5 w-3.5" />
                Pendiente de Verificación
              </span>
            </div>
          </div>
        </div>

        <div className="mt-6 rounded-2xl border border-mindfind-teal/20 bg-mindfind-teal-soft/30 p-4 text-sm font-medium text-teal-950">
          {mensaje}
        </div>

        <dl className="mt-6 grid gap-3 rounded-2xl bg-mindfind-gray p-5 text-sm sm:grid-cols-2">
          <div>
            <dt className="text-xs font-medium text-slate-500">Nombre completo</dt>
            <dd className="font-semibold text-slate-900">{psicologo.nombreCompleto}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-slate-500">N.° de Colegiatura</dt>
            <dd className="font-semibold text-slate-900">{psicologo.numeroColegiatura}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-slate-500">Enfoque teórico</dt>
            <dd className="font-medium text-slate-800">
              {ENFOQUE_LABELS[psicologo.enfoqueTeorico] ?? psicologo.enfoqueTeorico}
            </dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-slate-500">Tarifa por sesión</dt>
            <dd className="font-medium text-slate-800">
              S/ {Number(psicologo.tarifaPorSesion).toFixed(2)} {psicologo.moneda}
            </dd>
          </div>
          <div className="sm:col-span-2">
            <dt className="text-xs font-medium text-slate-500">Especialidades</dt>
            <dd className="mt-1 flex flex-wrap gap-1.5">
              {psicologo.especialidades.map((esp) => (
                <span
                  key={esp}
                  className="rounded-full bg-mindfind-lavender px-2.5 py-0.5 text-xs font-medium text-slate-800"
                >
                  {esp}
                </span>
              ))}
            </dd>
          </div>
        </dl>

        <div className="mt-6 flex flex-wrap gap-3">
          <Link
            to="/psychologists/validation"
            className="inline-flex items-center gap-2 rounded-xl bg-mindfind-teal px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-teal-800"
          >
            <BadgeCheck className="h-4 w-4" />
            <span>Ir al Panel de validación (Demo)</span>
          </Link>
          <button
            type="button"
            onClick={() => {
              setCreatedResult(null);
              setForm(INITIAL_FORM);
            }}
            className="rounded-xl border border-slate-200 bg-white px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
          >
            Registrar otro perfil
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="mx-auto max-w-3xl space-y-6">
      <div className="rounded-2xl border border-mindfind-lavender-border bg-mindfind-lavender-soft p-6">
        <div className="flex items-center gap-3">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-mindfind-teal text-white">
            <UserPlus className="h-5 w-5" />
          </span>
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Registro y Acreditación de Psicólogos</h1>
            <p className="text-sm text-slate-600">
              Únete a MindFind. Tu perfil pasará por validación institucional de colegiatura antes de ingresar al ranking.
            </p>
          </div>
        </div>
      </div>

      <form
        onSubmit={handleSubmit}
        noValidate
        className="space-y-6 rounded-3xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8"
      >
        {errorMessage && (
          <div
            role="alert"
            className="flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-800"
          >
            <AlertCircle className="mt-0.5 h-5 w-5 shrink-0 text-red-600" />
            <div>
              <p className="font-semibold">No se pudo completar el registro</p>
              <p className="mt-0.5">{errorMessage}</p>
            </div>
          </div>
        )}

        <div className="grid gap-5 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <label htmlFor="nombreCompleto" className="block text-sm font-semibold text-slate-700">
              Nombre completo <span className="text-red-500">*</span>
            </label>
            <input
              id="nombreCompleto"
              type="text"
              value={form.nombreCompleto}
              onChange={(e) => setForm((prev) => ({ ...prev, nombreCompleto: e.target.value }))}
              placeholder="Ej. Dra. Sofía Ramírez León"
              className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
            />
          </div>

          <div>
            <label htmlFor="correo" className="block text-sm font-semibold text-slate-700">
              Correo profesional <span className="text-red-500">*</span>
            </label>
            <input
              id="correo"
              type="email"
              value={form.correo}
              onChange={(e) => setForm((prev) => ({ ...prev, correo: e.target.value }))}
              placeholder="sofia.ramirez@mindfind.pe"
              className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
            />
          </div>

          <div>
            <label htmlFor="telefono" className="block text-sm font-semibold text-slate-700">
              Teléfono de contacto <span className="text-red-500">*</span>
            </label>
            <input
              id="telefono"
              type="tel"
              value={form.telefono}
              onChange={(e) => setForm((prev) => ({ ...prev, telefono: e.target.value }))}
              placeholder="991234567"
              className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
            />
          </div>

          <div>
            <label
              htmlFor="numeroColegiatura"
              className={`block text-sm font-semibold ${hasLicenseError ? 'text-red-700' : 'text-slate-700'}`}
            >
              N.° de Colegiatura (CPsP) <span className="text-red-500">*</span>
            </label>
            <input
              id="numeroColegiatura"
              type="text"
              value={form.numeroColegiatura}
              onChange={(e) => setForm((prev) => ({ ...prev, numeroColegiatura: e.target.value }))}
              placeholder="4 a 6 dígitos (ej. 45678)"
              aria-invalid={hasLicenseError}
              className={`mt-1.5 w-full rounded-xl border px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 ${
                hasLicenseError
                  ? 'border-red-500 bg-red-50/40 text-red-900 focus:border-red-600 focus:ring-red-200'
                  : 'border-slate-300 focus:border-mindfind-teal focus:ring-mindfind-teal/20'
              }`}
            />
            <p className={`mt-1 text-xs ${hasLicenseError ? 'font-medium text-red-600' : 'text-slate-500'}`}>
              Formato numérico válido de 4 a 6 dígitos.
            </p>
          </div>

          <div>
            <label htmlFor="enfoqueTeorico" className="block text-sm font-semibold text-slate-700">
              Enfoque teórico principal <span className="text-red-500">*</span>
            </label>
            <select
              id="enfoqueTeorico"
              value={form.enfoqueTeorico}
              onChange={(e) => setForm((prev) => ({ ...prev, enfoqueTeorico: e.target.value }))}
              className="mt-1.5 w-full rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
            >
              <option value="">Selecciona un enfoque...</option>
              {ENFOQUES_TEORICOS.map((item) => (
                <option key={item.value} value={item.value}>
                  {item.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Especialidades */}
        <div>
          <span className="block text-sm font-semibold text-slate-700">
            Áreas de especialidad <span className="text-red-500">*</span>
          </span>
          <p className="mt-0.5 text-xs text-slate-500">Selecciona una o más áreas clínicas o agrega otra.</p>

          <div className="mt-2.5 flex flex-wrap gap-2">
            {Array.from(new Set([...ESPECIALIDADES_SUGERIDAS, ...form.especialidades])).map((esp) => {
              const selected = form.especialidades.includes(esp);
              return (
                <button
                  key={esp}
                  type="button"
                  onClick={() => toggleSpecialty(esp)}
                  className={`rounded-full px-3.5 py-1.5 text-xs font-semibold transition ${
                    selected
                      ? 'bg-mindfind-teal text-white shadow-sm'
                      : 'border border-slate-200 bg-mindfind-gray text-slate-700 hover:bg-mindfind-lavender'
                  }`}
                >
                  {esp}
                </button>
              );
            })}
          </div>

          <div className="mt-3 flex gap-2">
            <input
              type="text"
              value={customSpecialty}
              onChange={(e) => setCustomSpecialty(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  addCustomSpecialty();
                }
              }}
              placeholder="Otra especialidad (ej. trastornos del sueño)"
              className="flex-1 rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
            />
            <button
              type="button"
              onClick={addCustomSpecialty}
              className="inline-flex items-center gap-1 rounded-xl border border-mindfind-lavender-border bg-mindfind-lavender-soft px-3.5 py-2 text-xs font-semibold text-mindfind-teal hover:bg-mindfind-lavender"
            >
              <Plus className="h-4 w-4" />
              <span>Agregar</span>
            </button>
          </div>
        </div>

        {/* Modalidades y Tarifa */}
        <div className="grid gap-5 sm:grid-cols-2">
          <div>
            <span className="block text-sm font-semibold text-slate-700">
              Modalidades de atención <span className="text-red-500">*</span>
            </span>
            <div className="mt-2 flex gap-3">
              {(['VIRTUAL', 'PRESENCIAL'] as ModalidadAtencion[]).map((mod) => {
                const active = form.modalidadesAtencion.includes(mod);
                return (
                  <button
                    key={mod}
                    type="button"
                    onClick={() => toggleModality(mod)}
                    className={`flex-1 rounded-xl border px-4 py-2.5 text-sm font-semibold transition ${
                      active
                        ? 'border-mindfind-teal bg-mindfind-teal-soft/50 text-mindfind-teal'
                        : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
                    }`}
                  >
                    {mod === 'VIRTUAL' ? 'Virtual' : 'Presencial'}
                  </button>
                );
              })}
            </div>
          </div>

          <div>
            <label htmlFor="tarifaPorSesion" className="block text-sm font-semibold text-slate-700">
              Tarifa por sesión (PEN) <span className="text-red-500">*</span>
            </label>
            <input
              id="tarifaPorSesion"
              type="number"
              min="1"
              step="0.01"
              value={form.tarifaPorSesion}
              onChange={(e) => setForm((prev) => ({ ...prev, tarifaPorSesion: e.target.value }))}
              placeholder="Ej. 120.00"
              className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
            />
          </div>
        </div>

        {/* Disponibilidad */}
        <div>
          <label htmlFor="disponibilidadHoraria" className="block text-sm font-semibold text-slate-700">
            Disponibilidad horaria <span className="text-red-500">*</span>
          </label>
          <input
            id="disponibilidadHoraria"
            type="text"
            value={form.disponibilidadHoraria}
            onChange={(e) => setForm((prev) => ({ ...prev, disponibilidadHoraria: e.target.value }))}
            placeholder="Ej. Lunes a Viernes 15:00 - 20:00 (Tarde/Noche)"
            className="mt-1.5 w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:border-mindfind-teal focus:outline-none focus:ring-2 focus:ring-mindfind-teal/20"
          />
          <div className="mt-2 flex flex-wrap gap-1.5">
            {HORARIOS_SUGERIDOS.map((horario) => (
              <button
                key={horario}
                type="button"
                onClick={() => setForm((prev) => ({ ...prev, disponibilidadHoraria: horario }))}
                className="rounded-lg bg-mindfind-gray-muted px-2.5 py-1 text-[11px] font-medium text-slate-600 hover:bg-mindfind-lavender"
              >
                {horario}
              </button>
            ))}
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 border-t border-slate-100 pt-5">
          <button
            type="submit"
            disabled={submitting}
            className="inline-flex items-center gap-2 rounded-xl bg-mindfind-teal px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {submitting ? 'Registrando perfil...' : 'Enviar registro profesional'}
          </button>
        </div>
      </form>
    </section>
  );
}
