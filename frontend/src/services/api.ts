import axios from 'axios';
import type { ReviewDetail, ReviewSummary, RiskLevel, TestSuggestion } from '../types/review';

const apiOrigin = ((import.meta.env.VITE_API_URL as string | undefined) ?? '').replace(/\/$/, '');
const normalizedBase =
  !apiOrigin || apiOrigin.endsWith('/api') ? apiOrigin || '/api' : `${apiOrigin}/api`;

const client = axios.create({ baseURL: normalizedBase });

export const fetchReviews = async (risk?: RiskLevel) => {
  const params = risk ? { risk } : undefined;
  const { data } = await client.get<ReviewSummary[]>('/reviews', { params });
  return data;
};

export const fetchReviewDetail = async (id: number) => {
  const { data } = await client.get<ReviewDetail>(`/reviews/${id}`);
  return data;
};

export const fetchTests = async (id: number) => {
  const { data } = await client.get<TestSuggestion[]>(`/reviews/${id}/tests`);
  return data;
};
