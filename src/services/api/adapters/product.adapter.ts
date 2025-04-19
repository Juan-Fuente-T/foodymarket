
import { Product } from '@/types/models';
import { GroupedProduct } from '@/types/models';

export const adaptProduct = (data: any): Product => ({
  id: data.prd_id?.toString() || '',
  name: data.name || '',
  description: data.description || '',
  price: data.price || 0,
  image: data.image || '',
  isActive: data.isActive === true,
  available: data.isActive === true, // Set available field to same value as isActive for compatibility
  quantity: data.quantity || 0,
  restaurantId: data.restaurantId?.toString() || '',
  categoryId: data.categoryId?.toString() || '',
  createdAt: data.prd_fecha_alta?.toString() || new Date().toISOString(),
  updatedAt: data.prd_fecha_actualizacion?.toString() || new Date().toISOString(),
  categoryName: data.categoryName
});

export const adaptGroupedProduct = (data: any): GroupedProduct => ({
  categoryName: data.categoryName,
  categoryId: data.categoryId,
  restaurantName: data.restaurantName,
  restaurantId: data.restaurantId,
  products: data.products
});
