import { Dashboard } from './pages/Dashboard';

export default function App() {
  return (
    <main className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 px-4 py-10 text-slate-50">
      <div className="mx-auto max-w-6xl space-y-10">
        <header className="space-y-3 text-center">
          <p className="text-sm uppercase tracking-[0.4em] text-slate-500">AI code reviewer</p>
          <h1 className="text-4xl font-semibold text-white">Automated GitHub PR Insights</h1>
          <p className="text-slate-400">
            Streamline pull request reviews with LLM-powered findings, refactors, tests and risk scoring.
          </p>
        </header>
        <Dashboard />
      </div>
    </main>
  );
}
