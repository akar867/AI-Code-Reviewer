import type { Finding } from '../types/review';
import clsx from 'clsx';

interface Props {
  items: Finding[];
}

const severityStyles = {
  LOW: 'border-l-green-500/60',
  MEDIUM: 'border-l-amber-500/60',
  HIGH: 'border-l-red-500/60',
  CRITICAL: 'border-l-red-600'
};

export function FindingList({ items }: Props) {
  if (!items.length) {
    return <p className="text-sm text-slate-400">No findings were generated for this review.</p>;
  }

  return (
    <div className="space-y-4">
      {items.map((finding) => (
        <div
          key={finding.id + finding.message}
          className={clsx(
            'rounded-xl border border-slate-800 bg-slate-900/70 p-4 text-sm text-slate-200',
            'border-l-4',
            severityStyles[finding.severity]
          )}
        >
          <div className="flex items-center justify-between text-xs uppercase">
            <span className="font-semibold tracking-wide text-slate-400">{finding.category}</span>
            <span className="font-semibold text-slate-100">{finding.severity}</span>
          </div>
          <p className="mt-2 font-medium text-slate-100">{finding.message}</p>
          <p className="mt-2 text-slate-400">{finding.suggestion}</p>
          {finding.filePath && (
            <p className="mt-2 text-xs text-slate-500">
              {finding.filePath}
              {finding.lineNumber ? `:${finding.lineNumber}` : ''}
            </p>
          )}
        </div>
      ))}
    </div>
  );
}
