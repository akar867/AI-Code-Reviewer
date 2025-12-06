import axios from 'axios';
import type { ReviewRequestPayload, ReviewResponse, ReviewSummary } from '../types';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
});

export const submitReview = async (payload: ReviewRequestPayload): Promise<ReviewResponse> => {
  const { data } = await api.post<ReviewResponse>('/review', payload);
  return data;
};

export const fetchReviews = async (): Promise<ReviewSummary[]> => {
  const { data } = await api.get<ReviewSummary[]>('/review');
  return data;
};

export const fetchReview = async (id: number): Promise<ReviewResponse> => {
  const { data } = await api.get<ReviewResponse>(`/review/${id}`);
  return data;
};

export default api;
