
import { Order } from '@/types/models';
import { adaptOrderItem } from './order-details.adapter.ts';

export const adaptOrder = (data: any): Order => ({
  id: data.ord_Id?.toString() || '',
  clientId: data.clientId?.id?.toString() || '',
  restaurantId: data.restaurantId?.id?.toString() || '',
  status: data.status || '',
  total: data.total || 0,
  comments: data.comments || '',
  createdAt: data.createdAt?.toString() || new Date().toISOString(),
  updatedAt: data.updatedAt?.toString() || new Date().toISOString(),
  details: Array.isArray(data.details) ? data.details.map((item: any) => ({
    ...adaptOrderItem(item),
    productName: item.product?.name || 'Unknown Product'
  })) : []
});
