import type { RiskLevel } from '../types/review';
import clsx from 'clsx';

interface Props {
  level: RiskLevel;
  score: number;
}

const palette: Record<RiskLevel, string> = {
  LOW: 'bg-risk-low/20 text-risk-low border-risk-low/40',
  MEDIUM: 'bg-risk-medium/20 text-risk-medium border-risk-medium/40',
  HIGH: 'bg-risk-high/20 text-risk-high border-risk-high/40'
};

export function RiskBadge({ level, score }: Props) {
  return (
    <span
      className={clsx(
        'inline-flex items-center gap-1 rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-wide',
        palette[level]
      )}
    >
      <span>{level}</span>
      <span className="text-slate-400">{score.toFixed(0)}%</span>
    </span>
  );
}
