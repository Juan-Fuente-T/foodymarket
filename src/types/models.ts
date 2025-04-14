
// export type UserRole = 'cliente' | 'restaurante' | 'admin';
export type UserRole = 'cliente' | 'restaurante';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  phone?: string;
  address?: string;
  createdAt: string;
}

export interface Category {
  id: string;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
  // image?: string;
}

export interface Restaurant {
  id: number;
  name: string;
  description: string;
  category: string; // Simple string según tu modelo
  phone: string;
  address: string;
  logo: string;
  ownerId: string; // Relación con User
  createdAt: string;
  updatedAt: string;
  // Nota: rating y reviewCount serían calculados
  // coverImage: string;
  // rating: number;
  // reviewCount: number;
}

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  image: string;
  available: boolean;
  quantity: number;
  restaurantId: string;
  categoryId: string;
  createdAt: string;
  updatedAt: string;
}

export interface GroupedProduct {
  categoryName: string;
  categoryId: number,
  restaurantName: string;
  restaurantId: number,
  products: Product[];
}

export type OrderStatus = 'pendiente' | 'pagado' | 'entregado' | 'cancelado';

export interface Order {
  id: string;
  clientId: string;
  restaurantId: string;
  status: OrderStatus;
  total: number;
  comments?: string;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[]; // Relación con OrderDetails
}

export interface OrderItem {
  id: string;
  productId: string;
  quantity: number;
  subtotal: number;
}

export interface Review {
  id: string;
  restaurantId: string;
  userId: string;
  score: number; // 0-10
  comments?: string;
  createdAt: string;
}
