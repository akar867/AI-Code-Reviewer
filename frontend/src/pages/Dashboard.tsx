import { useEffect, useState } from 'react';
import { useReviewDetail, useReviews } from '../hooks/useReviews';
import type { RiskLevel } from '../types/review';
import { ReviewCard } from '../components/ReviewCard';
import { ReviewFilter } from '../components/ReviewFilter';
import { FindingList } from '../components/FindingList';
import { TestSuggestionList } from '../components/TestSuggestionList';
import { RiskBadge } from '../components/RiskBadge';

export function Dashboard() {
  const [risk, setRisk] = useState<RiskLevel>();
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const { data: reviews = [], isLoading } = useReviews(risk);
  const { data: detail } = useReviewDetail(selectedId);

  useEffect(() => {
    if (!selectedId && reviews.length) {
      setSelectedId(reviews[0].id);
    }
  }, [reviews, selectedId]);

  return (
    <div className="grid gap-6 lg:grid-cols-12">
      <div className="space-y-4 lg:col-span-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold text-white">Pull Requests</h2>
          <ReviewFilter value={risk} onChange={setRisk} />
        </div>
        {isLoading && <p className="text-sm text-slate-400">Loading reviews…</p>}
        <div className="space-y-3">
          {reviews.map((review) => (
            <ReviewCard
              key={review.id}
              review={review}
              selected={review.id === selectedId}
              onSelect={setSelectedId}
            />
          ))}
          {!isLoading && !reviews.length && (
            <p className="text-sm text-slate-400">No reviews yet. Trigger the GitHub webhook to run one.</p>
          )}
        </div>
      </div>
      <div className="lg:col-span-8">
        {detail ? (
          <section className="space-y-6">
            <div className="rounded-3xl border border-slate-800 bg-slate-900/70 p-6">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="text-xs uppercase text-slate-500">{detail.summary.repository}</p>
                  <h1 className="text-2xl font-semibold text-white">{detail.summary.title}</h1>
                </div>
                <RiskBadge level={detail.summary.riskLevel} score={detail.summary.riskScore} />
              </div>
              <p className="mt-4 text-slate-300">{detail.summary.summary}</p>
              <dl className="mt-6 grid gap-4 text-sm text-slate-200 sm:grid-cols-3">
                <div>
                  <dt className="text-slate-500">Findings</dt>
                  <dd className="text-2xl font-semibold">{detail.findings.length}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Tests Suggested</dt>
                  <dd className="text-2xl font-semibold">{detail.testSuggestions.length}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Author</dt>
                  <dd className="text-lg">{detail.summary.author}</dd>
                </div>
              </dl>
            </div>
            <div>
              <h3 className="mb-3 text-lg font-semibold text-white">Findings</h3>
              <FindingList items={detail.findings} />
            </div>
            <div>
              <h3 className="mb-3 text-lg font-semibold text-white">Missing Tests</h3>
              <TestSuggestionList items={detail.testSuggestions} />
            </div>
          </section>
        ) : (
          <div className="rounded-3xl border border-dashed border-slate-700 p-10 text-center text-slate-500">
            Select a pull request to view the AI review.
          </div>
        )}
      </div>
    </div>
  );
}
