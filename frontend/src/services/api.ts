import { toast } from "@/lib/toast";
import {
  User,
  Restaurant,
  Category,
  Product,
  Order,
  Review,
  OrderStatus,
  OrderRequestDto
} from "@/types/models";
import { adaptRestaurant } from '../services/api/adapters/restaurant.adapter';
import { adaptProduct } from '../services/api/adapters/product.adapter';
import { adaptOrder } from '../services/api/adapters/order.adapter';
import { adaptReview } from '../services/api/adapters/review.adapter';
import { adaptUser } from '../services/api/adapters/user.adapter';
import { adaptCategory } from "./api/adapters/category.adapter";

// Base URL for API requests - Using localhost for development
// const API_BASE_URL = "http://localhost:8080/api";
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// Token storage key - IMPORTANT: must match the one in AuthContext.tsx
const TOKEN_STORAGE_KEY = "food_delivery_token";

// Helper function to get auth token
const getAuthToken = () => {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
};

// Helper function for handling fetch responses
const handleResponse = async (response: Response) => {
  const contentType = response.headers.get("content-type");
  
  if (!response.ok) {
    let errorMessage = `API error: ${response.status}`;
    
    try {
      if (contentType && contentType.includes("application/json")) {
        const error = await response.json();
        errorMessage = error.message || errorMessage;
      } else {
        const textError = await response.text();
        if (textError) errorMessage = textError;
      }
    } catch (e) {
      console.error("Error parsing error response:", e);
    }
    
    throw new Error(errorMessage);
  }
  
  // If response has no content, return empty object
  if (response.status === 204 || response.headers.get("content-length") === "0") {
    return {};
  }
  
  // Parse response based on content type
  if (contentType && contentType.includes("application/json")) {
    return response.json();
  } else {
    return response.text();
  }
};

// Generic fetch with error handling
const fetchWithError = async (
  endpoint: string,
  options: RequestInit = {},
  sendAuth = true
): Promise<any> => {
  try {
    const url = `${API_BASE_URL}${endpoint}`;
    
    // Preparar headers con token de autenticación
    const headers: HeadersInit = {
      ...options.headers,
    };

    // Añadir token de autenticación si existe
    const token = getAuthToken();
    if (token && sendAuth) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    // Solo añadir Content-Type para métodos que llevan body
    if (options.method && ['POST', 'PUT', 'PATCH'].includes(options.method)) {
      headers['Content-Type'] = 'application/json';
    }

    // if (options.body) {
    //   console.log('Request body:', options.body);
    // }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    // console.log(`Response status: ${response.status}`);

    const data = await handleResponse(response);
    // console.log('Response data:', data);
    return data;
  } catch (error) {
    console.error(`API request failed: ${error}`);
    console.error(`Failed URL: ${API_BASE_URL}${endpoint}`);

    // Mostrar error en toast
    toast.error("Something went wrong with the request");
    throw error;
  }
};

// Users
export const userAPI = {
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
  
  getProfile: () => fetchWithError("/user"), // Cambiado a /user para coincidir con el backend
  
  updateProfile: (userData: Partial<User>) =>
    fetchWithError("/user", {
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
      if (data.access_token) {
        localStorage.setItem(TOKEN_STORAGE_KEY, data.access_token);
      }
      return data;
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
        localStorage.setItem(TOKEN_STORAGE_KEY, data.token);
      }
      return data;
    } catch (error) {
      console.error("Registration error:", error);
      throw error;
    }
  },
  
  logout: () => {
    // Remove token from localStorage
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  },
  
  getCurrentUser: async () => {
    // Check if token exists
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (!token) {
      return null;
    }
    
    try {
      const userData = await userAPI.getProfile();
      return userData;
    } catch (error) {
      console.error("Get current user error:", error);
      // If unauthorized, clear the token
      if ((error as any).message?.includes("401")) {
        localStorage.removeItem(TOKEN_STORAGE_KEY);
      }
      return null;
    }
  },
};

// Restaurants
export const restaurantAPI = {
  getAll: async (): Promise<Restaurant[]> => {
    const data = await fetchWithError("/restaurant/all");
    return data.map(adaptRestaurant);
  },

  getById: async (id: string) => {
    const data = await fetchWithError(`/restaurant/${id}`);
    return adaptRestaurant(data);
  },
  getByOwnerId: async (ownerId: string): Promise<Restaurant[]>  => {
    const data = await fetchWithError(`/restaurant/byOwnerId/${ownerId}`);
    return data.map(adaptRestaurant);
  },
  getProductCategories: async (restaurantId: string): Promise<Category[]> => { 
    const rawData = await fetchWithError(`/restaurant/${restaurantId}/categories`); 

    if (!Array.isArray(rawData)) {
         console.error("getProductCategories did not return an array:", rawData);
         return [];
    }
    const adaptedData = rawData.map(adaptCategory);
    return adaptedData; 
},

  // getByCategory: async (categoryId: string) => {
  //   const data = await fetchWithError(`/restaurant/byCategory/${categoryId}`);
  //   return data.map(adaptRestaurant);
  // },

  create: async (data: Omit<Restaurant, 'id' | 'createdAt' | 'updatedAt'>) => {
    const response = await fetchWithError("/restaurant", {
      method: "POST",
      body: JSON.stringify({
        ...data
      }),
    });
    return adaptRestaurant(response);
  },

  update: async (id: string, data: Partial<Restaurant>) => {
    const response = await fetchWithError(`/restaurant/${id}`, {
      method: "PATCH",
      body: JSON.stringify(data),
    });
    return adaptRestaurant(response);
  },
  addProductCategory: async (id: string, categoryData: Partial<Category>) => {
    const response = await fetchWithError(`/restaurant/${id}/categories`, {
      method: "POST",
      body: JSON.stringify(categoryData),
    });
    return response;
  },

  delete: (id: string) => fetchWithError(`/restaurant/${id}`, { method: "DELETE" }),

  //   getFeatured: () => fetchWithError("/restaurant/featured"),
};

// Restaurant Cuisines
export const restaurantCuisinesAPI = {
  getAll: async () => {
    const data = await fetchWithError("/cuisines");
    return data;
  },

  // getById: async (id: string) => {
  //   const data = await fetchWithError(`/cuisines/${id}`);
  //   return data;
  // }
}

// Product Categories
export const categoryAPI = {
  getAll: async () => {
    const data = await fetchWithError("/category");
    return data.map(adaptCategory);
  },

  getById: async (id: string) => {
    const data = await fetchWithError(`/category/${id}`);
    return adaptCategory(data);
  },

  // update: async (id: string, data: Partial<Category>) => {
  //     const response = await fetchWithError(`/category/${id}`, {
  //       method: "PUT",
  //       body: JSON.stringify(data),
  //     });
  //     return response;
  //   },

  delete: (categoryId: string, restaurantId: string) => fetchWithError(
    `/category/${categoryId}/restaurant/${restaurantId}`,
     { method: "DELETE" }
    ),
};

// Products
export const productAPI = {
  getByRestaurant: async (restaurantId: string) => {
    const data = await fetchWithError(`/product/byRestaurant/${restaurantId}`);
    return data.map(adaptProduct);
  },

  getById: async (id: string) => {
    const data = await fetchWithError(`/product/${id}`);
    return adaptProduct(data);
  },

  getByCategory: async (categoryId: string) => {
    const data = await fetchWithError(`/product/byCategory/${categoryId}`);
    return data.map(adaptProduct);
  },

  getByRestaurantAndCategory: async (restaurantId: string) => {
    const resp = await fetchWithError(`/product/byRestaurantAndCategory/${restaurantId}`);
    // Verifica si la respuesta es un array de categorías con productos
    if (!Array.isArray(resp)) {
      throw new Error("Formato de respuesta inválido");
    }
    const response =  resp.map((categoryGroup: any) => ({
      categoryName: categoryGroup.categoryName,
      categoryId: categoryGroup.categoryId,
      restaurantName: categoryGroup.restaurantName,
      restaurantId: categoryGroup.restaurantId,
      products: categoryGroup.products.map((product: any) => ({
        id: product.prd_id.toString(),
        name: product.name,
        price: product.price,
        image: product.image,
        description: product.description,
        category: categoryGroup.categoryName, 
        isActive: product.isActive,
        quantity: product.quantity,
        restaurantId: product.restaurantId
      })),
    }));
    return response;
  },

  create: async (data: Omit<Product, 'id' | 'createdAt' | 'updatedAt'>) => {
    const response = await fetchWithError("/product", {
      method: "POST",
      body: JSON.stringify(data),
    });
    return adaptProduct(response);
  },

  update: async (id: string, data: Omit<Product, 'id' | 'restaurantId' | 'createdAt' | 'updatedAt'>) => {
    const response = await fetchWithError(`/product/${id}`, {
      method: "PATCH",
      body: JSON.stringify(data),
    });
    return adaptProduct(response);
  },

  delete: (id: string) => fetchWithError(`/product/${id}`, { method: "DELETE" }),

  // getFeatured: async () => {
  //   const data = await fetchWithError("/product/featured");
  //   return data.map(adaptProduct);
  // },
};

// Órdenes (Order)
export const orderAPI = {
  create: async (data: OrderRequestDto, email: string) => {
    const response = await fetchWithError(`/order?email=${encodeURIComponent(email)}`, {
      method: "POST",
      body: JSON.stringify(data),
    });
    return adaptOrder(response);
  },
  
  getByRestaurant: async (restaurantId: string) => {
    const data = await fetchWithError(`/order?restaurantId=${restaurantId}`);
    return data.map(adaptOrder);
  },

  getById: async (ord_id: string) => {
    const data = await fetchWithError(`/order/${ord_id}`);
    return adaptOrder(data);
  },

  updateStatus: async (ord_id: string, status: OrderStatus) => {
    const response = await fetchWithError(`/order/${ord_id}`, {
      method: "PATCH",
      body: JSON.stringify({ status: status}),
      headers: {
        "Content-Type": "application/json",
      },
    });
    return adaptOrder(response);
  },

  delete: async (ord_id: string) => {
    await fetchWithError(`/order/${ord_id}`, { method: "DELETE" });
  },

  getByClient: async (cln_id: string) => {
    const data = await fetchWithError(`/order/byClientId/${cln_id}`);
    return data.map(adaptOrder);
  },
  getAllByRestaurant: async (ownerId: string) => {
    // const data = await fetchWithError(`/order/byOwnerId/${ownerId}`);
    const data = await fetchWithError(`/order/byOwnerId/${ownerId}/paged`);
    return data.map(adaptOrder);
  },

  getByDateRange: async (restaurantId: string, start: string, end: string) => {
    const data = await fetchWithError(
      `/order/byDate?restaurantId=${restaurantId}&start=${start}&end=${end}`
    );
    return data.map(adaptOrder);
  },

  getByClientAndDate: async (clientId: string, start: string, end: string) => {
    const data = await fetchWithError(
      `/order/byClientDate?clientId=${clientId}&start=${start}&end=${end}`
    );
    return data.map(adaptOrder);
  },

  getByRestaurantAndState: async (restaurantId: string, state: OrderStatus) => {
    const data = await fetchWithError(
      `/order/byRestaurantAndState?restaurantId=${restaurantId}&state=${state}`
    );
    return data.map(adaptOrder);
  }
};

// Reseñas (Review)
export const reviewAPI = {
  create: async (data: Omit<Review, 'id'>, userEmail: string) => {
    const response = await fetchWithError(`/review?email=${encodeURIComponent(userEmail)}`, {
      method: "POST",
      body: JSON.stringify(data),
    });
    return adaptReview(response);
  },

  getByRestaurant: async (restaurantId: string) => {
    const data = await fetchWithError(`/review/restaurant?restaurantId=${restaurantId}`);
    return data.map(adaptReview);
  },

  getById: async (id: string) => {
    const data = await fetchWithError(`/review/id?id=${id}`);
    return adaptReview(data);
  },

  // update: async (Omit<Review, 'id'>, userEmail: string) => {
  //   const response = await fetchWithError(`/review?email=${encodeURIComponent(userEmail)}`, {
  //     method: "PATCH",
  //     body: JSON.stringify(data),
  //   });
  //   return adaptReview(response);
  // },

  // Elimina reseña (para cliente dueño)
  delete: async (id: string, userEmail: string) => {
    await fetchWithError(`/review?id=${id}&email=${encodeURIComponent(userEmail)}`, {
      method: "DELETE"
    });
  }
};
