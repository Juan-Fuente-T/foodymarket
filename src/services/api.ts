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
const API_BASE_URL = "http://localhost:8080/api";

// Token storage key
const TOKEN_STORAGE_KEY = "food_delivery_token";

// Helper function to get auth token
const getAuthToken = () => {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
};

// Helper function for handling fetch responses
const handleResponse = async (response: Response) => {
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || `API error: ${response.status}`);
  }
  return response.json();
};

// Mock data for fallback when API fails due to CORS
const MOCK_DATA = {
  restaurants: [
    {
      id: "1",
      name: "Burger Palace",
      description: "Delicious burgers and fries",
      address: "123 Main St",
      phone: "555-1234",
      email: "info@burgerpalace.com",
      logo: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      coverImage: "https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      category: { id: "1", name: "Fast Food", image: "https://images.unsplash.com/photo-1561758033-d89a9ad46330?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" },
      rating: 4.5,
      reviewCount: 120,
      ownerId: "owner1",
      openingHours: "9:00 AM - 10:00 PM"
    },
    {
      id: "2",
      name: "Pizza Heaven",
      description: "Authentic Italian pizzas",
      address: "456 Oak Ave",
      phone: "555-5678",
      email: "hello@pizzaheaven.com",
      logo: "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      coverImage: "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      category: { id: "2", name: "Italian", image: "https://images.unsplash.com/photo-1498579150354-977475b7ea0b?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" },
      rating: 4.8,
      reviewCount: 200,
      ownerId: "owner2",
      openingHours: "11:00 AM - 11:00 PM"
    },
    {
      id: "3",
      name: "Sushi Express",
      description: "Fresh sushi and Japanese cuisine",
      address: "789 Pine Blvd",
      phone: "555-9012",
      email: "contact@sushiexpress.com",
      logo: "https://images.unsplash.com/photo-1553621042-f6e147245754?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      coverImage: "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      category: { id: "3", name: "Japanese", image: "https://images.unsplash.com/photo-1617196035154-1e7e6e28b30f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" },
      rating: 4.7,
      reviewCount: 150,
      ownerId: "owner3",
      openingHours: "12:00 PM - 10:00 PM"
    }
  ],
  products: {
    "1": [
      {
        id: "101",
        name: "Classic Burger",
        description: "Beef patty with lettuce, tomato, and special sauce",
        price: 8.99,
        image: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
        restaurantId: "1",
        category: { id: "1", name: "Burgers", image: "https://images.unsplash.com/photo-1571091718767-18b5b1457add?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" },
        available: true,
        featured: true
      },
      {
        id: "102",
        name: "Cheese Fries",
        description: "Golden fries topped with melted cheese",
        price: 4.99,
        image: "https://images.unsplash.com/photo-1585109649139-366815a0d713?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
        restaurantId: "1",
        category: { id: "4", name: "Sides", image: "https://images.unsplash.com/photo-1576107232684-e2a11e9f7cc8?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" },
        available: true,
        featured: false
      }
    ],
    "2": [
      {
        id: "201",
        name: "Margherita Pizza",
        description: "Classic pizza with tomato sauce, mozzarella, and basil",
        price: 12.99,
        image: "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
        restaurantId: "2",
        category: { id: "5", name: "Pizza", image: "https://images.unsplash.com/photo-1513104890138-7c749659a591?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" },
        available: true,
        featured: true
      },
      {
        id: "202",
        name: "Pepperoni Pizza",
        description: "Pizza topped with pepperoni slices",
        price: 14.99,
        image: "https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
        restaurantId: "2",
        category: { id: "5", name: "Pizza", image: "https://images.unsplash.com/photo-1513104890138-7c749659a591?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" },
        available: true,
        featured: false
      }
    ],
    "3": [
      {
        id: "301",
        name: "California Roll",
        description: "Crab, avocado, and cucumber roll",
        price: 7.99,
        image: "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
        restaurantId: "3",
        category: { id: "6", name: "Sushi Rolls", image: "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" },
        available: true,
        featured: true
      },
      {
        id: "302",
        name: "Salmon Nigiri",
        description: "Fresh salmon over pressed rice",
        price: 6.99,
        image: "https://images.unsplash.com/photo-1553621042-f6e147245754?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
        restaurantId: "3",
        category: { id: "7", name: "Nigiri", image: "https://images.unsplash.com/photo-1553621042-f6e147245754?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" },
        available: true,
        featured: false
      }
    ]
  }
};

// Generic fetch function with error handling and mock data fallback
const fetchWithError = async (
  url: string,
  options: RequestInit = {}
): Promise<any> => {
  try {
    // Get the auth token if available
    const token = getAuthToken();
    
    // Prepare headers
    const headers: HeadersInit = {
      "Content-Type": "application/json",
      ...options.headers,
    };
    
    // Add auth token if available
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    
    console.log(`Making API request to: ${API_BASE_URL}${url}`);
    
    // Try to fetch from API
    try {
      const response = await fetch(`${API_BASE_URL}${url}`, {
        ...options,
        headers,
      });
      
      return await handleResponse(response);
    } catch (error) {
      console.error(`API request failed: ${error}`);
      console.error(`Failed URL: ${API_BASE_URL}${url}`);
      
      // Return mock data if available for this endpoint
      if (url === "/restaurants") {
        console.log("Using mock restaurant data due to API failure");
        return MOCK_DATA.restaurants;
      } else if (url.startsWith("/restaurant/")) {
        const requestedId = url.split("/").pop() || "";
        const mockRestaurant = MOCK_DATA.restaurants.find(r => r.id === requestedId);
        if (mockRestaurant) {
          console.log(`Using mock data for restaurant ${requestedId} due to API failure`);
          return mockRestaurant;
        }
      } else if (url.startsWith("/products/restaurant/")) {
        const restaurantId = url.split("/").pop() || "";
        console.log(`Using mock product data for restaurant ${restaurantId} due to API failure`);
        return MOCK_DATA.products[restaurantId] || [];
      } else if (url.startsWith("/restaurants/by-category/")) {
        const categoryId = url.split("/").pop() || "";
        const matchingRestaurants = MOCK_DATA.restaurants.filter(r => r.category.id === categoryId);
        console.log(`Using mock restaurant data for category ${categoryId} due to API failure`);
        return matchingRestaurants;
      }
      
      // If no mock data is available for this endpoint, display error and rethrow
      toast.error("Something went wrong with the request");
      throw error;
    }
  } catch (error) {
    console.error(`Request completely failed: ${error}`);
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
  
  getById: (id: string) => fetchWithError(`/restaurant/${id}`),
  
  getByCategory: (categoryId: string) =>
    fetchWithError(`/restaurants/by-category/${categoryId}`),
  
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
  
  getFeatured: () => fetchWithError("/restaurants/featured"),
};

// Categories
export const categoryAPI = {
  getAll: () => fetchWithError("/categories"),
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
    fetchWithError(`/products/restaurant/${restaurantId}`),
  
  getById: (id: string) => fetchWithError(`/product/${id}`),
  
  getByCategory: (categoryId: string) =>
    fetchWithError(`/products/by-category/${categoryId}`),
  
  getByRestaurantAndCategory: (restaurantId: string, categoryId: string) =>
    fetchWithError(`/products/restaurant/${restaurantId}/category/${categoryId}`),
  
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
  
  getFeatured: () => fetchWithError("/products/featured"),
};

// Orders
export const orderAPI = {
  create: (data: Partial<Order>) =>
    fetchWithError("/order", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  
  getByUser: (userId: string) =>
    fetchWithError(`/orders/user/${userId}`),
  
  getByRestaurant: (restaurantId: string) =>
    fetchWithError(`/orders/restaurant/${restaurantId}`),
  
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
    fetchWithError(`/reviews/restaurant/${restaurantId}`),
  
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
