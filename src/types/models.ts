
export interface User {
  id: string;
  name: string;
  email: string;
  role: 'customer' | 'owner' | 'admin';
  phone?: string;
  address?: string;
  avatar?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Restaurant {
  id: string;
  name: string;
  description: string;
  address: string;
  phone: string;
  email: string;
  logo: string;
  coverImage: string;
  rating: number;
  reviewCount: number;
  category: string; // Cambiado: ahora es solo un string, no una entidad
  ownerId: string;
  openingHours: string;
  createdAt: string;
  updatedAt: string;
}

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  image: string;
  category: string; // Cambiado: ahora es solo un string, no una entidad
  restaurantId: string;
  available: boolean;
  featured: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  id: string;
  productId: string;
  product: Product;
  quantity: number;
  price: number;
  notes?: string;
}

export interface Order {
  id: string;
  userId: string;
  restaurantId: string;
  restaurant: Restaurant;
  items: OrderItem[];
  status: 'pending' | 'accepted' | 'rejected' | 'preparing' | 'ready' | 'delivered' | 'cancelled';
  total: number;
  deliveryAddress: string;
  paymentMethod: 'cash' | 'card' | 'online';
  createdAt: string;
  updatedAt: string;
}

export interface Review {
  id: string;
  userId: string;
  user: User;
  restaurantId: string;
  rating: number;
  comment: string;
  createdAt: string;
  updatedAt: string;
}
