import type { ReviewResponse } from '../types';

const formatList = (items?: string[] | null) =>
  items && items.length > 0 ? (
    <ul>
      {items.map((item, index) => (
        <li key={index}>{item}</li>
      ))}
    </ul>
  ) : (
    <p>No items recorded.</p>
  );

type Props = {
  review: ReviewResponse | null;
};

const ReviewResult = ({ review }: Props) => {
  if (!review) {
    return (
      <section className="card">
        <h2 className="section-title">Results</h2>
        <p>Run a review to see findings, suggestions, scoring, and best practices.</p>
      </section>
    );
  }

  const { reviewResult } = review;

  const downloadJson = () => {
    const blob = new Blob([JSON.stringify(review, null, 2)], { type: 'application/json' });
    triggerDownload(blob, `review-${review.id}.json`);
  };

  const downloadMarkdown = () => {
    const markdown = `# AI Code Review Report\n\n` +
      `- **Language:** ${review.language}\n` +
      `- **Score:** ${reviewResult.qualityScore ?? '—'}/10\n` +
      `- **Created:** ${new Date(review.createdAt).toLocaleString()}\n\n` +
      `## Issues\n${reviewResult.issues.map((issue) => `- ${issue}`).join('\n') || '- None'}\n\n` +
      `## Suggestions\n${reviewResult.suggestions.map((s) => `- ${s}`).join('\n') || '- None'}\n\n` +
      `## Best Practices\n${reviewResult.bestPractices.map((b) => `- ${b}`).join('\n') || '- None'}\n`;

    const blob = new Blob([markdown], { type: 'text/markdown' });
    triggerDownload(blob, `review-${review.id}.md`);
  };

  return (
    <section className="card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2 className="section-title">Results</h2>
          <p className="subtitle">Score: {reviewResult.qualityScore ?? '—'}/10</p>
        </div>
        <div className="actions">
          <button type="button" onClick={downloadMarkdown}>
            Download Markdown
          </button>
          <button type="button" onClick={downloadJson}>
            Download JSON
          </button>
        </div>
      </div>
      <div className="result-grid">
        <section>
          <h3>Issues</h3>
          {formatList(reviewResult.issues)}
        </section>
        <section>
          <h3>Suggestions</h3>
          {formatList(reviewResult.suggestions)}
        </section>
        <section>
          <h3>Best practices</h3>
          {formatList(reviewResult.bestPractices)}
        </section>
      </div>
    </section>
  );
};

const triggerDownload = (blob: Blob, filename: string) => {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.append(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
};

export default ReviewResult;
