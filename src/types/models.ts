
// export type UserRole = 'cliente' | 'restaurante' | 'admin';
export type UserRole = 'CLIENTE' | 'RESTAURANTE';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  phone?: string;
  address?: string;
  createdAt: string;
  avatar?: string;
  password?: string; // Add password field for registration
}

export interface Category {
  id: string;
  name: string;
  // description?: string;
  // createdAt: string;
  // updatedAt: string;
  // image?: string;
}

export interface Restaurant {
  id: number;
  name: string;
  description: string;
  phone: string;
  email: string;
  address: string;
  openingHours: string;
  logo: string;
  coverImage: string;
  // cuisine: {id: number, name: string};
  cuisineId: number;
  cuisineName: string;
  ownerId: string;
  createdAt: string;
  updatedAt: string;
  rating?: number;
  reviewCount?: number;
  logoImage?: string; // Added for backward compatibility
  minOrderAmount?: number;
  deliveryFee?: number;
  category?: string; // Added for backward compatibility - maps to cuisineName
}

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  image: string;
  isActive: boolean;
  available: boolean; // Always set to same value as isActive
  quantity: number;
  restaurantId: string;
  categoryId: string;
  createdAt: string;
  updatedAt: string;
  categoryName?: string; // Added for UI convenience
}

export interface GroupedProduct {
  categoryName: string;
  categoryId: number;
  restaurantName: string;
  restaurantId: number;
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
  productName?: string; // Added for UI convenience
}

export interface Review {
  id: string;
  restaurantId: string;
  userId: string;
  score: number; // 0-10
  comments?: string;
  createdAt: string;
  user?: {
    name: string;
    avatar?: string;
  };
}
