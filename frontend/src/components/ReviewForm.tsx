import { FormEvent, useState } from 'react';
import { submitReview } from '../api/client';
import type { ReviewRequestPayload, ReviewResponse } from '../types';

const LANGUAGES = ['Java', 'JavaScript', 'TypeScript', 'Python', 'Go', 'Rust'];

type Props = {
  onReview: (response: ReviewResponse) => void;
};

const ReviewForm = ({ onReview }: Props) => {
  const [form, setForm] = useState<ReviewRequestPayload>({ code: '', language: LANGUAGES[0] });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!form.code.trim()) {
      setError('Paste or type code before requesting a review.');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await submitReview(form);
      onReview(response);
    } catch (err) {
      setError('Review failed. Ensure the backend is running and reachable.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card">
      <h2 className="section-title">Submit code for review</h2>
      <p>Paste a snippet or drop in whole files. The reviewer returns structured JSON, suggestions, and a score.</p>
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
          <label style={{ flex: '1 1 220px' }}>
            <span>Language</span>
            <select
              value={form.language}
              onChange={(event) => setForm((prev) => ({ ...prev, language: event.target.value }))}
            >
              {LANGUAGES.map((language) => (
                <option key={language} value={language}>
                  {language}
                </option>
              ))}
            </select>
          </label>
        </div>
        <label style={{ display: 'block', marginBottom: '1rem' }}>
          <span>Code</span>
          <textarea
            value={form.code}
            onChange={(event) => setForm((prev) => ({ ...prev, code: event.target.value }))}
            placeholder={'public class Demo {\n    public static void main(String[] args) {\n        System.out.println("Hello reviewer");\n    }\n}'}
          />
        </label>
        {error && <p style={{ color: '#b91c1c' }}>{error}</p>}
        <button type="submit" >
          {loading ? 'Reviewing…' : 'Submit for review'}
        </button>
      </form>
    </section>
  );
};

export default ReviewForm;
