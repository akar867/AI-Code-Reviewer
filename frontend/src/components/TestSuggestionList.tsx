import type { TestSuggestion } from '../types/review';

interface Props {
  items: TestSuggestion[];
}

export function TestSuggestionList({ items }: Props) {
  if (!items.length) {
    return <p className="text-sm text-slate-400">No automated test suggestions yet.</p>;
  }

  return (
    <div className="grid gap-4 md:grid-cols-2">
      {items.map((test) => (
        <div key={test.id + test.filePath} className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4">
          <p className="text-xs uppercase text-slate-500">{test.framework}</p>
          <h4 className="mt-1 text-base font-semibold text-slate-100">{test.filePath}</h4>
          <p className="mt-2 text-sm text-slate-300">{test.description}</p>
          <pre className="mt-3 whitespace-pre-wrap rounded-xl bg-slate-950/70 p-3 text-xs text-slate-400">
            {test.example}
          </pre>
        </div>
      ))}
    </div>
  );
}
