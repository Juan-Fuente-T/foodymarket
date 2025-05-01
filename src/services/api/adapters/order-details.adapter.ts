
import { OrderItem } from '@/types/models';

export const adaptOrderItem = (data: any): OrderItem => ({
  id: data.odt_id?.toString() || '',
  productId: data.product?.prd_id?.toString() || '',
  productName: data.product?.prd_name || data.productName || '',
  quantity: data.quantity || 0,
  productPrice: data.product?.prd_price || data.productPrice || 0,
  subtotal: data.subtotal || 0
});
