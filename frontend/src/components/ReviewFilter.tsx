import type { RiskLevel } from '../types/review';

interface Props {
  value?: RiskLevel;
  onChange: (next?: RiskLevel) => void;
}

const options: Array<{ label: string; value?: RiskLevel }> = [
  { label: 'All' },
  { label: 'Low', value: 'LOW' },
  { label: 'Medium', value: 'MEDIUM' },
  { label: 'High', value: 'HIGH' }
];

export function ReviewFilter({ value, onChange }: Props) {
  return (
    <div className="inline-flex rounded-full border border-slate-800 bg-slate-900/60 p-1 text-xs font-semibold text-slate-400">
      {options.map((option) => (
        <button
          key={option.label}
          className={`rounded-full px-4 py-1 transition ${
            value === option.value || (!value && !option.value)
              ? 'bg-slate-800 text-white'
              : 'text-slate-400 hover:text-white'
          }`}
          onClick={() => onChange(option.value)}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}
