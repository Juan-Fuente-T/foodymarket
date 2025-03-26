
import { toast } from "@/components/ui/sonner";
import { 
  User, 
  Restaurant, 
  Category, 
  Product, 
  Order, 
  Review 
} from "@/types/models";

// Base URL for API requests
const API_BASE_URL = "https://api.example.com"; // Replace with your actual API URL

// Helper function for handling fetch responses
const handleResponse = async (response: Response) => {
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || `API error: ${response.status}`);
  }
  return response.json();
};

// Generic fetch function with error handling
const fetchWithError = async (
  url: string,
  options: RequestInit = {}
): Promise<any> => {
  try {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
    });
    return await handleResponse(response);
  } catch (error) {
    console.error(`API request failed: ${error}`);
    toast.error("Something went wrong with the request");
    throw error;
  }
};

// Authentication
export const authAPI = {
  login: (email: string, password: string) =>
    fetchWithError("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),

  register: (userData: Partial<User>) =>
    fetchWithError("/auth/register", {
      method: "POST",
      body: JSON.stringify(userData),
    }),

  logout: () => fetchWithError("/auth/logout", { method: "POST" }),
  
  getCurrentUser: () => fetchWithError("/auth/me"),
};

// Restaurants
export const restaurantAPI = {
  getAll: () => fetchWithError("/restaurants"),
  
  getById: (id: string) => fetchWithError(`/restaurants/${id}`),
  
  getByCategory: (categoryId: string) =>
    fetchWithError(`/restaurants?categoryId=${categoryId}`),
  
  create: (data: Partial<Restaurant>) =>
    fetchWithError("/restaurants", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  update: (id: string, data: Partial<Restaurant>) =>
    fetchWithError(`/restaurants/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/restaurants/${id}`, { method: "DELETE" }),
  
  getFeatured: () => fetchWithError("/restaurants/featured"),
};

// Categories
export const categoryAPI = {
  getAll: () => fetchWithError("/categories"),
  
  getById: (id: string) => fetchWithError(`/categories/${id}`),
  
  create: (data: Partial<Category>) =>
    fetchWithError("/categories", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  update: (id: string, data: Partial<Category>) =>
    fetchWithError(`/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/categories/${id}`, { method: "DELETE" }),
};

// Products
export const productAPI = {
  getByRestaurant: (restaurantId: string) =>
    fetchWithError(`/products?restaurantId=${restaurantId}`),
  
  getById: (id: string) => fetchWithError(`/products/${id}`),
  
  getByCategory: (categoryId: string) =>
    fetchWithError(`/products?categoryId=${categoryId}`),
  
  create: (data: Partial<Product>) =>
    fetchWithError("/products", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  update: (id: string, data: Partial<Product>) =>
    fetchWithError(`/products/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/products/${id}`, { method: "DELETE" }),
  
  getFeatured: () => fetchWithError("/products/featured"),
};

// Orders
export const orderAPI = {
  create: (data: Partial<Order>) =>
    fetchWithError("/orders", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  getByUser: (userId: string) =>
    fetchWithError(`/orders?userId=${userId}`),
  
  getByRestaurant: (restaurantId: string) =>
    fetchWithError(`/orders?restaurantId=${restaurantId}`),
  
  getById: (id: string) => fetchWithError(`/orders/${id}`),
  
  updateStatus: (id: string, status: Order["status"]) =>
    fetchWithError(`/orders/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    }),
};

// Reviews
export const reviewAPI = {
  getByRestaurant: (restaurantId: string) =>
    fetchWithError(`/reviews?restaurantId=${restaurantId}`),
  
  create: (data: Partial<Review>) =>
    fetchWithError("/reviews", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  update: (id: string, data: Partial<Review>) =>
    fetchWithError(`/reviews/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/reviews/${id}`, { method: "DELETE" }),
};

// Users
export const userAPI = {
  update: (id: string, data: Partial<User>) =>
    fetchWithError(`/users/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/users/${id}`, { method: "DELETE" }),
  
  getById: (id: string) => fetchWithError(`/users/${id}`),
};
