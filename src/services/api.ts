
import { toast } from "@/lib/toast";
import { 
  User, 
  Restaurant, 
  Category, 
  Product, 
  Order, 
  Review 
} from "@/types/models";

// Base URL for API requests
const API_BASE_URL = "https://c24-39-t-webapp.onrender.com/api";

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
  getAll: () => fetchWithError("/restaurant/all"),
  
  getById: (id: string) => fetchWithError(`/restaurant/${id}`),
  
  getByCategory: (categoryId: string) =>
    fetchWithError(`/restaurant/byCategory/${categoryId}`),
  
  create: (data: Partial<Restaurant>) =>
    fetchWithError("/restaurant", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  update: (id: string, data: Partial<Restaurant>) =>
    fetchWithError(`/restaurant/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/restaurant/${id}`, { method: "DELETE" }),
  
  getFeatured: () => fetchWithError("/restaurant/featured"),
};

// Categories
export const categoryAPI = {
  getAll: () => fetchWithError("/category/all"),
  
  getById: (id: string) => fetchWithError(`/category/${id}`),
  
  create: (data: Partial<Category>) =>
    fetchWithError("/category", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  update: (id: string, data: Partial<Category>) =>
    fetchWithError(`/category/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/category/${id}`, { method: "DELETE" }),
};

// Products
export const productAPI = {
  getByRestaurant: (restaurantId: string) =>
    fetchWithError(`/product/byRestaurant/${restaurantId}`),
  
  getById: (id: string) => fetchWithError(`/product/${id}`),
  
  getByCategory: (categoryId: string) =>
    fetchWithError(`/product/byCategory/${categoryId}`),
  
  getByRestaurantAndCategory: (restaurantId: string, categoryId: string) =>
    fetchWithError(`/product/byRestaurantAndCategory/${restaurantId}/${categoryId}`),
  
  create: (data: Partial<Product>) =>
    fetchWithError("/product", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  update: (id: string, data: Partial<Product>) =>
    fetchWithError(`/product/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/product/${id}`, { method: "DELETE" }),
  
  getFeatured: () => fetchWithError("/product/featured"),
};

// Orders
export const orderAPI = {
  create: (data: Partial<Order>) =>
    fetchWithError("/order", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  getByUser: (userId: string) =>
    fetchWithError(`/order/byUser/${userId}`),
  
  getByRestaurant: (restaurantId: string) =>
    fetchWithError(`/order/byRestaurant/${restaurantId}`),
  
  getById: (id: string) => fetchWithError(`/order/${id}`),
  
  updateStatus: (id: string, status: Order["status"]) =>
    fetchWithError(`/order/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    }),
};

// Reviews
export const reviewAPI = {
  getByRestaurant: (restaurantId: string) =>
    fetchWithError(`/review/byRestaurant/${restaurantId}`),
  
  create: (data: Partial<Review>) =>
    fetchWithError("/review", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  update: (id: string, data: Partial<Review>) =>
    fetchWithError(`/review/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/review/${id}`, { method: "DELETE" }),
};

// Users
export const userAPI = {
  update: (id: string, data: Partial<User>) =>
    fetchWithError(`/user/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/user/${id}`, { method: "DELETE" }),
  
  getById: (id: string) => fetchWithError(`/user/${id}`),
};
