
import { OrderItem } from '@/types/models';

export const adaptOrderItem = (data: any): OrderItem => ({
  id: data.odt_id?.toString() || '',
  productId: data.product?.prd_id?.toString() || '',
  quantity: data.quantity || 0,
  subtotal: data.subtotal || 0
});
