import { useState } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { HeartHandshake, Menu, X } from 'lucide-react';

const navLinks = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/triage?nuevo=1', label: 'Iniciar evaluación', end: false },
  { to: '/psychologists/register', label: 'Soy psicólogo', end: false },
  { to: '/psychologists/validation', label: 'Panel de validación (Demo)', end: false },
];

export function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <header className="sticky top-0 z-40 border-b border-mindfind-lavender-border/80 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3.5 sm:px-6">
        <Link to="/" className="flex items-center gap-2.5 text-mindfind-teal transition-opacity hover:opacity-90">
          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-mindfind-teal text-white shadow-sm">
            <HeartHandshake className="h-5 w-5" />
          </span>
          <div className="leading-tight">
            <span className="block text-lg font-bold tracking-tight text-slate-900">MindFind</span>
            <span className="block text-[11px] font-medium text-mindfind-teal">Salud Mental Basada en Evidencia</span>
          </div>
        </Link>

        <nav className="hidden items-center gap-2 md:flex" aria-label="Navegación principal">
          {navLinks.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `rounded-lg px-3.5 py-2 text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-mindfind-lavender text-mindfind-teal'
                    : 'text-slate-600 hover:bg-mindfind-gray-muted hover:text-slate-900'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <button
          type="button"
          onClick={() => setMenuOpen((prev) => !prev)}
          className="inline-flex items-center justify-center rounded-lg p-2 text-slate-600 hover:bg-mindfind-gray-muted md:hidden"
          aria-label={menuOpen ? 'Cerrar menú' : 'Abrir menú'}
        >
          {menuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </button>
      </div>

      {menuOpen && (
        <nav className="border-t border-slate-100 bg-white px-4 py-3 md:hidden" aria-label="Menú móvil">
          <div className="flex flex-col gap-1.5">
            {navLinks.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                onClick={() => setMenuOpen(false)}
                className={({ isActive }) =>
                  `rounded-lg px-3 py-2.5 text-sm font-medium ${
                    isActive
                      ? 'bg-mindfind-lavender text-mindfind-teal'
                      : 'text-slate-700 hover:bg-mindfind-gray-muted'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </div>
        </nav>
      )}
    </header>
  );
}
