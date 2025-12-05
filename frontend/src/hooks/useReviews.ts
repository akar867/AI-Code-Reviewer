import { useQuery } from '@tanstack/react-query';
import { fetchReviewDetail, fetchReviews } from '../services/api';
import type { RiskLevel } from '../types/review';

export const useReviews = (risk?: RiskLevel) =>
  useQuery({
    queryKey: ['reviews', risk],
    queryFn: () => fetchReviews(risk),
    staleTime: 30_000
  });

export const useReviewDetail = (id: number | null) =>
  useQuery({
    queryKey: ['review', id],
    queryFn: () => (id ? fetchReviewDetail(id) : Promise.reject('No review id')),
    enabled: !!id
  });
