import { RiskBadge } from './RiskBadge';
import type { ReviewSummary } from '../types/review';
import clsx from 'clsx';

interface Props {
  review: ReviewSummary;
  selected?: boolean;
  onSelect?: (id: number) => void;
}

export function ReviewCard({ review, selected, onSelect }: Props) {
  return (
    <button
      className={clsx(
        'w-full rounded-2xl border border-slate-800 bg-slate-900/70 p-4 text-left transition hover:border-slate-600',
        selected && 'border-sky-400 bg-slate-900'
      )}
      onClick={() => onSelect?.(review.id)}
    >
      <div className="flex items-center justify-between text-sm text-slate-400">
        <span>
          {review.repository} · #{review.prNumber}
        </span>
        <span>{new Date(review.createdAt).toLocaleString()}</span>
      </div>
      <div className="mt-2 flex items-center justify-between">
        <h3 className="text-lg font-semibold text-slate-100">{review.title}</h3>
        <RiskBadge level={review.riskLevel} score={review.riskScore} />
      </div>
      <p className="mt-3 text-sm text-slate-300">{review.summary}</p>
      <div className="mt-3 text-xs text-slate-500">
        {review.findings} findings · Author {review.author}
      </div>
    </button>
  );
}
