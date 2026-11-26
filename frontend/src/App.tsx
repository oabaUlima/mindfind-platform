import { Navigate, Route, Routes } from 'react-router-dom';
import { PsychologistsValidationPage } from './identidad_acreditacion/pages/PsychologistsValidationPage';
import { RegisterPsychologistPage } from './identidad_acreditacion/pages/RegisterPsychologistPage';
import { MainLayout } from './shared/components/MainLayout';
import { LandingPage } from './shared/pages/LandingPage';
import { CrisisAlertPage } from './triage_matching/pages/CrisisAlertPage';
import { MatchingResultsPage } from './triage_matching/pages/MatchingResultsPage';
import { TriageQuestionnairePage } from './triage_matching/pages/TriageQuestionnairePage';

export function App() {
  return (
    <MainLayout>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/psychologists/register" element={<RegisterPsychologistPage />} />
        <Route path="/psychologists/validation" element={<PsychologistsValidationPage />} />
        <Route path="/triage" element={<TriageQuestionnairePage />} />
        <Route path="/triage/crisis/:id" element={<CrisisAlertPage />} />
        <Route path="/triage/results/:id" element={<MatchingResultsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </MainLayout>
  );
}

export default App;
