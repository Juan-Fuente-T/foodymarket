import { Product } from '@/types/models';

export const adaptProduct = (data: any): Product => ({
  id: data.prd_id?.toString() || '',
  name: data.prd_nombre || '',
  description: data.prd_descripcion || '',
  price: data.prd_precio || 0,
  image: data.prd_imagen || '',
  available: data.prd_activo || false,
  quantity: data.prd_cantidad || 0,
  restaurantId: data.restaurant?.id?.toString() || '',
  categoryId: data.category?.ctg_id?.toString() || '',
  createdAt: data.prd_fecha_alta?.toString() || new Date().toISOString(),
  updatedAt: data.prd_fecha_actualizacion?.toString() || new Date().toISOString()
});