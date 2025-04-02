import { Review } from '@/types/models';

export const adaptReview = (data: any): Review => ({
  id: data.id?.toString() || '',
  restaurantId: data.restaurant?.id?.toString() || '',
  userId: data.userEntity?.id?.toString() || '',
  score: data.score || 0,
  comments: data.comments || '',
  createdAt: data.createdAt?.toString() || new Date().toISOString()
});