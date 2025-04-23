
import { Product } from "@/types/models";

export const adaptProduct = (data: any): Product => ({
  id: data.id.toString() || "",
  name: data.name || "",
  description: data.description || "",
  price: parseFloat(data.price) || 0,
  image: data.image || "",
  isActive: data.isActive === undefined ? true : Boolean(data.isActive),
  available: data.available === undefined ? true : Boolean(data.available),
  quantity: parseInt(data.quantity) || 1,
  restaurantId: data.restaurantId?.toString() || "",
  categoryId: data.categoryId?.toString() || "",
  createdAt: data.createdAt?.toString() || new Date().toISOString(),
  updatedAt: data.updatedAt?.toString() || new Date().toISOString(),
  categoryName: data.categoryName || "",
});

export const adaptProducts = (data: any[]): Product[] => {
  return data.map(adaptProduct);
};
