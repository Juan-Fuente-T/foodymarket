
import { Product } from "@/types/models";

export const adaptProduct = (data: any): Product => ({
  id: data.prd_id.toString() || "",
  name: data.name || "",
  description: data.description || "",
  price: data.price || '0',
  image: data.image || "",
  isActive: data.isActive === undefined ? true : Boolean(data.isActive),
  available: data.available === undefined ? true : Boolean(data.available),
  quantity: parseInt(data.quantity) || 1,
  restaurantId: data.restaurantId?.toString() || "",
  categoryId: data.categoryId?.toString() || "",
  createdAt: data.createdAt?.toString() || 'N/D',
  updatedAt: data.updatedAt?.toString() || 'N/D',
  categoryName: data.categoryName || "",
});

export const adaptProducts = (data: any[]): Product[] => {
  return data.map(adaptProduct);
};
