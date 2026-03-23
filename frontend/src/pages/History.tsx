import { useEffect, useState } from 'react';
import { fetchReview, fetchReviews } from '../api/client';
import type { ReviewResponse, ReviewSummary } from '../types';

const History = () => {
  const [reviews, setReviews] = useState<ReviewSummary[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [selectedReview, setSelectedReview] = useState<ReviewResponse | null>(null);
  const [loadingList, setLoadingList] = useState(true);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        setLoadingList(true);
        const data = await fetchReviews();
        setReviews(data);
        if (data.length && !selectedId) {
          selectReview(data[0].id);
        }
      } catch (err) {
        setError('Unable to fetch history.');
      } finally {
        setLoadingList(false);
      }
    };
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const selectReview = async (id: number) => {
    setSelectedId(id);
    setLoadingDetail(true);
    setError(null);
    try {
      const data = await fetchReview(id);
      setSelectedReview(data);
    } catch (err) {
      setError('Unable to fetch review details.');
    } finally {
      setLoadingDetail(false);
    }
  };

  return (
    <div className="history-grid">
      <section className="card" style={{ minHeight: '400px' }}>
        <h2 className="section-title">Past reviews</h2>
        {loadingList && <p>Loading...</p>}
        {error && <p style={{ color: '#b91c1c' }}>{error}</p>}
        <div className="list">
          {reviews.map((review) => (
            <div
              key={review.id}
              className="list-item"
              style={{ borderColor: selectedId === review.id ? '#2563eb' : undefined }}
              onClick={() => selectReview(review.id)}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <strong>{review.language}</strong>
                <span className="badge">Score {review.qualityScore ?? '—'}</span>
              </div>
              <small>{new Date(review.createdAt).toLocaleString()}</small>
            </div>
          ))}
          {!loadingList && reviews.length === 0 && <p>No history yet. Run a review first.</p>}
        </div>
      </section>
      <section className="card" style={{ minHeight: '400px' }}>
        <h2 className="section-title">Details</h2>
        {loadingDetail && <p>Loading details...</p>}
        {!loadingDetail && selectedReview && (
          <div>
            <p>
              <strong>Language:</strong> {selectedReview.language}
            </p>
            <p>
              <strong>Score:</strong> {selectedReview.reviewResult.qualityScore}/10
            </p>
            <p>
              <strong>Issues</strong>
            </p>
            <ul>
              {(selectedReview.reviewResult.issues ?? []).map((issue, index) => (
                <li key={index}>{issue}</li>
              ))}
            </ul>
            <p>
              <strong>Suggestions</strong>
            </p>
            <ul>
              {(selectedReview.reviewResult.suggestions ?? []).map((suggestion, index) => (
                <li key={index}>{suggestion}</li>
              ))}
            </ul>
            <p>
              <strong>Best practices</strong>
            </p>
            <ul>
              {(selectedReview.reviewResult.bestPractices ?? []).map((practice, index) => (
                <li key={index}>{practice}</li>
              ))}
            </ul>
            <p>
              <strong>Submitted code</strong>
            </p>
            <pre
              style={{
                background: '#0f172a',
                color: '#e2e8f0',
                padding: '1rem',
                borderRadius: '0.75rem',
                overflowX: 'auto',
                whiteSpace: 'pre-wrap'
              }}
            >
              {selectedReview.submittedCode}
            </pre>
          </div>
        )}
        {!loadingDetail && !selectedReview && <p>Select a review to inspect findings.</p>}
      </section>
    </div>
  );
};

export default History;
