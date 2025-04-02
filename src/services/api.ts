
import { toast } from "@/lib/toast";
import { 
  User, 
  Restaurant, 
  Product, 
  Order, 
  Review
} from "@/types/models";

// Base API URL
const API_BASE_URL = "http://localhost:3000/api";

// Generic fetch with error handling
const fetchWithError = async (
  endpoint: string,
  options: RequestInit = {}
): Promise<any> => {
  try {
    const url = `${API_BASE_URL}${endpoint}`;
    
    // Default headers
    const headers = {
      "Content-Type": "application/json",
      ...options.headers,
    };
    
    const response = await fetch(url, {
      ...options,
      headers,
    });
    
    // Check if the request was successful
    if (!response.ok) {
      let errorData;
      try {
        errorData = await response.json();
      } catch (e) {
        // If the response body is not valid JSON
        throw new Error(`Error: ${response.status} ${response.statusText}`);
      }
      
      throw new Error(errorData.message || `Error: ${response.status} ${response.statusText}`);
    }
    
    // Check if response is empty
    const text = await response.text();
    if (!text) {
      return null;
    }
    
    // Parse JSON response
    return JSON.parse(text);
  } catch (error) {
    console.error("API Error:", error);
    throw error;
  }
};

// Users
export const userAPI = {
  login: (email: string, password: string) =>
    fetchWithError("/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),
  
  register: (userData: Partial<User>) =>
    fetchWithError("/register", {
      method: "POST",
      body: JSON.stringify(userData),
    }),
  
  getProfile: () => fetchWithError("/profile"),
  
  updateProfile: (userData: Partial<User>) =>
    fetchWithError("/profile", {
      method: "PUT",
      body: JSON.stringify(userData),
    }),
  
  uploadAvatar: async (file: File) => {
    const formData = new FormData();
    formData.append("avatar", file);
    
    return fetchWithError("/profile/avatar", {
      method: "POST",
      headers: {}, // Remove Content-Type for FormData
      body: formData,
    });
  },
  
  getAllUsers: () => fetchWithError("/users"),
  
  getUserById: (id: string) => fetchWithError(`/users/${id}`),
  
  deleteUser: (id: string) =>
    fetchWithError(`/users/${id}`, { method: "DELETE" }),
};

// Auth
export const authAPI = {
  login: async (email: string, password: string) => {
    try {
      const data = await userAPI.login(email, password);
      // Store token in localStorage
      if (data.token) {
        localStorage.setItem("token", data.token);
      }
      return data.user;
    } catch (error) {
      console.error("Login error:", error);
      throw error;
    }
  },
  
  register: async (userData: Partial<User>) => {
    try {
      const data = await userAPI.register(userData);
      // Store token in localStorage
      if (data.token) {
        localStorage.setItem("token", data.token);
      }
      return data.user;
    } catch (error) {
      console.error("Registration error:", error);
      throw error;
    }
  },
  
  logout: () => {
    // Remove token from localStorage
    localStorage.removeItem("token");
  },
  
  getCurrentUser: async () => {
    // Check if token exists
    const token = localStorage.getItem("token");
    if (!token) {
      return null;
    }
    
    try {
      return await userAPI.getProfile();
    } catch (error) {
      console.error("Get current user error:", error);
      // If unauthorized, clear the token
      if ((error as any).message?.includes("401")) {
        localStorage.removeItem("token");
      }
      return null;
    }
  },
};

// Restaurants
export const restaurantAPI = {
  getAll: () => fetchWithError("/restaurants"),
  
  getById: (id: string) => fetchWithError(`/restaurants/${id}`),
  
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
  
  getByOwner: (ownerId: string) =>
    fetchWithError(`/restaurants/owner/${ownerId}`),
  
  getNearby: (lat: number, lng: number) =>
    fetchWithError(`/restaurants/nearby?lat=${lat}&lng=${lng}`),
  
  search: (query: string) =>
    fetchWithError(`/restaurants/search?q=${encodeURIComponent(query)}`),
  
  getFeatured: () => fetchWithError("/restaurants/featured"),
  
  // Simplificado: Ahora solo obtiene categorías como strings, no como entidades
  getCategories: () => fetchWithError("/restaurants/categories"),
};

// Products
export const productAPI = {
  getByRestaurant: (restaurantId: string) =>
    fetchWithError(`/products/restaurant/${restaurantId}`),
  
  getById: (id: string) => fetchWithError(`/products/${id}`),
  
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
  
  search: (query: string) =>
    fetchWithError(`/products/search?q=${encodeURIComponent(query)}`),
  
  getFeatured: () => fetchWithError("/products/featured"),
  
  // Simplificado: Ahora solo obtiene categorías como strings, no como entidades
  getCategories: () => fetchWithError("/products/categories"),
};

// Orders
export const orderAPI = {
  getAll: () => fetchWithError("/orders"),
  
  getById: (id: string) => fetchWithError(`/orders/${id}`),
  
  create: (data: Partial<Order>) =>
    fetchWithError("/orders", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  update: (id: string, data: Partial<Order>) =>
    fetchWithError(`/orders/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  
  delete: (id: string) =>
    fetchWithError(`/orders/${id}`, { method: "DELETE" }),
  
  getByUser: (userId: string) =>
    fetchWithError(`/orders/user/${userId}`),
  
  getByRestaurant: (restaurantId: string) =>
    fetchWithError(`/orders/restaurant/${restaurantId}`),
  
  updateStatus: (id: string, status: Order["status"]) =>
    fetchWithError(`/orders/${id}/status`, {
      method: "PUT",
      body: JSON.stringify({ status }),
    }),
};

// Reviews
export const reviewAPI = {
  getByRestaurant: (restaurantId: string) =>
    fetchWithError(`/reviews/restaurant/${restaurantId}`),
  
  getByUser: (userId: string) =>
    fetchWithError(`/reviews/user/${userId}`),
  
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
