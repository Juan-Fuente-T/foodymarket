import { Restaurant, } from "@/types/models";

export const adaptRestaurant = (data: any): Restaurant => ({
  id: data.rst_id || 0,
  name: data.name || '',
  description: data.description || '',
  category: data.category || '',
  phone: data.phone || '',
  address: data.address || '',
  logo: data.logo || '',
  ownerId: data.userEntity?.id?.toString() || '',
  createdAt: data.createdAt?.toString() || new Date().toISOString(),
  updatedAt: data.updatedAt?.toString() || new Date().toISOString()
//   coverImage: '', // Valor por defecto
//   rating: data.reviews?.reduce((acc: number, r: any) => acc + r.rvw_puntaje, 0) / data.reviews?.length || 0,
//   reviewCount: data.reviews?.length || 0,
});

