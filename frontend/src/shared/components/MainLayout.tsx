import type { ReactNode } from 'react';
import { Lock } from 'lucide-react';
import { Navbar } from './Navbar';

interface MainLayoutProps {
  children: ReactNode;
}

export function MainLayout({ children }: MainLayoutProps) {
  return (
    <div className="flex min-h-screen flex-col bg-mindfind-gray text-slate-800">
      <Navbar />
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8 sm:px-6 sm:py-10">{children}</main>
      <footer className="border-t border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-2 px-4 py-5 text-xs text-slate-500 sm:flex-row sm:px-6">
          <p className="font-medium text-slate-700">MindFind — Orientación clínica y emparejamiento ético</p>
          <p className="flex items-center gap-1.5">
            <Lock className="h-3.5 w-3.5 text-mindfind-teal" />
            <span>Tus respuestas están protegidas bajo confidencialidad y cifrado.</span>
          </p>
        </div>
      </footer>
    </div>
  );
}
