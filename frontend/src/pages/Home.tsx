import { useState } from 'react';
import ReviewForm from '../components/ReviewForm';
import ReviewResult from '../components/ReviewResult';
import type { ReviewResponse } from '../types';

const Home = () => {
  const [review, setReview] = useState<ReviewResponse | null>(null);

  return (
    <div className="page">
      <ReviewForm onReview={setReview} />
      <ReviewResult review={review} />
    </div>
  );
};

export default Home;
