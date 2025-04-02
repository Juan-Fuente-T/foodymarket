import { Order } from '@/types/models';
import { adaptOrderItem } from './order-details.adapter.ts';

export const adaptOrder = (data: any): Order => ({
  id: data.ord_id?.toString() || '',
  clientId: data.clientId?.id?.toString() || '',
  restaurantId: data.restaurantId?.id?.toString() || '',
  status: data.state as Order['status'],
  total: data.total || 0,
  comments: data.comments || '',
  createdAt: data.createdAt?.toString() || new Date().toISOString(),
  updatedAt: data.updatedAt?.toString() || new Date().toISOString(),
  items: data.details?.map(adaptOrderItem) || []
});