
import { Restaurant } from "@/types/models";

export const adaptRestaurant = (data: any): Restaurant => ({
  id: data.rst_id || 0,
  name: data.name || '',
  description: data.description || '',
  phone: data.phone || '',
  email: data.email || '',
  address: data.address || '',
  openingHours: data.openingHours || '',
  logo: data.logo || '',
  coverImage: data.photo || data.coverImage || '',
  // cuisine: data.cuisine || null,
  cuisineId: data.cuisineId || 0,
  cuisineName: data.cuisineName || '',
  category: data.cuisineName || '', // Map cuisineName to category for compatibility
  ownerId: data.rst_user_id?.toString() || '',
  createdAt: data.createdAt?.toString() || 'N/D',
  updatedAt: data.updatedAt?.toString() || 'N/D',
  rating: data.reviews?.reduce((acc: number, r: any) => acc + r.rvw_puntaje, 0) / data.reviews?.length || 0,
  reviewCount: data.reviews?.length || 0,
  minOrderAmount: data.minOrderAmount || 0,
  deliveryFee: data.deliveryFee || 0,
});
