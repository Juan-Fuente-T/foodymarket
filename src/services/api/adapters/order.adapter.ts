
import { Order } from '@/types/models';
import { adaptOrderItem } from './order-details.adapter.ts';

export const adaptOrder = (data: any): Order => ({
  id: data.ord_Id?.toString() || '',
  clientId: data.clientId?.id?.toString() || '',
  restaurantId: data.restaurantId?.id?.toString() || '',
  restaurantName: data.restaurantName || data.restaurantId.Name || '',
  status: data.status || '',
  total: data.total || 0,
  comments: data.comments || '',
  createdAt: data.createdAt?.toString() || 'N/D',
  updatedAt: data.updatedAt?.toString() || 'N/D',
  details: data.details?.map(adaptOrderItem) || []
});
