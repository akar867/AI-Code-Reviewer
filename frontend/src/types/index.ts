export interface ReviewRequestPayload {
  code: string;
  language: string;
}

export interface ReviewResult {
  issues: string[];
  suggestions: string[];
  qualityScore: number;
  bestPractices: string[];
  rawResponse?: string;
}

export interface ReviewResponse {
  id: number;
  language: string;
  submittedCode: string;
  createdAt: string;
  reviewResult: ReviewResult;
}

export interface ReviewSummary {
  id: number;
  language: string;
  qualityScore: number | null;
  createdAt: string;
}
