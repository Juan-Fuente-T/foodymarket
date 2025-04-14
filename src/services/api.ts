import { toast } from "@/lib/toast";
import {
  User,
  Restaurant,Category,
  Product,
  Order,
  Review,
  OrderStatus
} from "@/types/models";
import { adaptRestaurant } from '../services/api/adapters/restaurant.adapter';
import { adaptProduct } from '../services/api/adapters/product.adapter';
import { adaptOrder } from '../services/api/adapters/order.adapter';
import { adaptReview } from '../services/api/adapters/review.adapter';
import { adaptUser } from '../services/api/adapters/user.adapter';
import { dataTagSymbol } from "@tanstack/react-query";


// Base URL for API requests
// const API_BASE_URL = "https://zealous-bravery-production.up.railway.app/api";
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
  // restaurants: [
  //   {
  //     id: "1",
  //     name: "Burger Palace",
  //     description: "Delicious burgers and fries",
  //     address: "123 Main St",
  //     phone: "555-1234",
  //     email: "info@burgerpalace.com",
  //     logo: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //     coverImage: "https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //     categories: [
  //       { id: "1", name: "Fast Food", image: "https://images.unsplash.com/photo-1561758033-d89a9ad46330?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" }
  //     ],
  //     rating: 4.5,
  //     featured: true,
  //     ownerId: "owner1"
  //   },
  //   {
  //     id: "2",
  //     name: "Pizza Heaven",
  //     description: "Authentic Italian pizzas",
  //     address: "456 Oak Ave",
  //     phone: "555-5678",
  //     email: "hello@pizzaheaven.com",
  //     logo: "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //     coverImage: "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //     categories: [
  //       { id: "2", name: "Italian", image: "https://images.unsplash.com/photo-1498579150354-977475b7ea0b?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" }
  //     ],
  //     rating: 4.8,
  //     featured: true,
  //     ownerId: "owner2"
  //   },
  //   {
  //     id: "3",
  //     name: "Sushi Express",
  //     description: "Fresh sushi and Japanese cuisine",
  //     address: "789 Pine Blvd",
  //     phone: "555-9012",
  //     email: "contact@sushiexpress.com",
  //     logo: "https://images.unsplash.com/photo-1553621042-f6e147245754?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //     coverImage: "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //     categories: [
  //       { id: "3", name: "Japanese", image: "https://images.unsplash.com/photo-1617196035154-1e7e6e28b30f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80" }
  //     ],
  //     rating: 4.7,
  //     featured: true,
  //     ownerId: "owner3"
  //   }
  // ],
  // categories: [
  //   {
  //     id: "1",
  //     name: "Fast Food",
  //     description: "Quick and tasty meals",
  //     image: "https://images.unsplash.com/photo-1561758033-d89a9ad46330?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80"
  //   },
  //   {
  //     id: "2",
  //     name: "Italian",
  //     description: "Authentic Italian cuisine",
  //     image: "https://images.unsplash.com/photo-1498579150354-977475b7ea0b?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80"
  //   },
  //   {
  //     id: "3",
  //     name: "Japanese",
  //     description: "Fresh sushi and Japanese dishes",
  //     image: "https://images.unsplash.com/photo-1617196035154-1e7e6e28b30f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80"
  //   },
  //   {
  //     id: "4",
  //     name: "Mexican",
  //     description: "Spicy and flavorful Mexican food",
  //     image: "https://images.unsplash.com/photo-1504674900247-0877df9cc836?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80"
  //   }
  // ],
  //   products: {
  //     "1": [
  //       {
  //         id: "101",
  //         name: "Classic Burger",
  //         description: "Beef patty with lettuce, tomato, and special sauce",
  //         price: 8.99,
  //         image: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //         restaurantId: "1",
  //         categoryId: "1",
  //         available: true
  //       },
  //       {
  //         id: "102",
  //         name: "Cheese Fries",
  //         description: "Golden fries topped with melted cheese",
  //         price: 4.99,
  //         image: "https://images.unsplash.com/photo-1585109649139-366815a0d713?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //         restaurantId: "1",
  //         categoryId: "1",
  //         available: true
  //       }
  //     ],
  //     "2": [
  //       {
  //         id: "201",
  //         name: "Margherita Pizza",
  //         description: "Classic pizza with tomato sauce, mozzarella, and basil",
  //         price: 12.99,
  //         image: "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //         restaurantId: "2",
  //         categoryId: "2",
  //         available: true
  //       },
  //       {
  //         id: "202",
  //         name: "Pepperoni Pizza",
  //         description: "Pizza topped with pepperoni slices",
  //         price: 14.99,
  //         image: "https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //         restaurantId: "2",
  //         categoryId: "2",
  //         available: true
  //       }
  //     ],
  //     "3": [
  //       {
  //         id: "301",
  //         name: "California Roll",
  //         description: "Crab, avocado, and cucumber roll",
  //         price: 7.99,
  //         image: "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //         restaurantId: "3",
  //         categoryId: "3",
  //         available: true
  //       },
  //       {
  //         id: "302",
  //         name: "Salmon Nigiri",
  //         description: "Fresh salmon over pressed rice",
  //         price: 6.99,
  //         image: "https://images.unsplash.com/photo-1553621042-f6e147245754?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
  //         restaurantId: "3",
  //         categoryId: "3",
  //         available: true
  //       }
  //     ]
  //   }
};

// Generic fetch with error handling
const fetchWithError = async (
  endpoint: string,
  options: RequestInit = {}
): Promise<any> => {
  try {
    const url = `${API_BASE_URL}${endpoint}`;
    
    // Preparar headers sin Content-Type por defecto
    const headers: HeadersInit = {
      ...options.headers,
    };

    // Solo añadir Content-Type para métodos que llevan body
    if (options.method && ['POST', 'PUT', 'PATCH'].includes(options.method)) {
      headers['Content-Type'] = 'application/json';
    }

    console.log(`Making API request to: ${url}`);

    // Try to fetch from API
    const response = await fetch(url, {
      ...options,
      headers,
    });

    return await handleResponse(response);
  } catch (error) {
    console.error(`API request failed: ${error}`);
    console.error(`Failed URL: ${API_BASE_URL}${endpoint}`);

    /* // Mock data comentado (completo, sin cambios de estructura)
    if (endpoint === "/restaurant/all") {
      console.log("Using mock restaurant data due to API failure");
      return MOCK_DATA.restaurants;
    } else if (endpoint === "/category") {
      console.log("Using mock category data due to API failure");
      return MOCK_DATA.categories;
    } else if (endpoint.startsWith("/product/byRestaurant/")) {
      const restaurantId = endpoint.split("/").pop() || "";
      console.log(`Using mock product data for restaurant ${restaurantId} due to API failure`);
      return MOCK_DATA.products[restaurantId] || [];
    } else if (endpoint.startsWith("/restaurant/")) {
      const requestedId = endpoint.split("/").pop() || "";
      const mockRestaurant = MOCK_DATA.restaurants.find(r => r.id === requestedId);
      if (mockRestaurant) {
        console.log(`Using mock data for restaurant ${requestedId} due to API failure`);
        return mockRestaurant;
      }
    } else if (endpoint.startsWith("/category/")) {
      const requestedId = endpoint.split("/").pop() || "";
      const mockCategory = MOCK_DATA.categories.find(c => c.id === requestedId);
      if (mockCategory) {
        console.log(`Using mock data for category ${requestedId} due to API failure`);
        return mockCategory;
      }
    } else if (endpoint.startsWith("/restaurant/byCategory/")) {
      const categoryId = endpoint.split("/").pop() || "";
      const matchingRestaurants = MOCK_DATA.restaurants.filter(r => 
        r.categories.some(c => c.id === categoryId)
      );
      console.log(`Using mock restaurant data for category ${categoryId} due to API failure`);
      return matchingRestaurants;
    }
    */

    // If no mock data is available for this endpoint, display error and rethrow
    toast.error("Something went wrong with the request");
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
  getAll: async () => {
    const data = await fetchWithError("/restaurant/all");
    return data.map(adaptRestaurant);
  },

  getById: async (id: string) => {
    const data = await fetchWithError(`/restaurant/${id}`);
    return adaptRestaurant(data);
  },

  getByCategory: async (categoryId: string) => {
    const data = await fetchWithError(`/restaurant/byCategory/${categoryId}`);
    return data.map(adaptRestaurant);
  },

  create: async (data: Omit<Restaurant, 'id' | 'createdAt' | 'updatedAt'>) => {
    const response = await fetchWithError("/restaurant", {
      method: "POST",
      body: JSON.stringify({
        ...data,
        categoria: data.category // Solo ajustamos el nombre del campo
      }),
    });
    return adaptRestaurant(response);
  },

  update: async (id: string, data: Partial<Restaurant>) => {
    const response = await fetchWithError(`/restaurant/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
    return adaptRestaurant(response);
  },

  delete: (id: string) => fetchWithError(`/restaurant/${id}`, { method: "DELETE" }),

  //   getFeatured: () => fetchWithError("/restaurant/featured"),
};

// Categories
export const categoryAPI = {
  getAll: async () => {
    const data = await fetchWithError("/category");
    return data.map();
  },

  getById: async (id: string) => {
    const data = await fetchWithError(`/category/${id}`);
    return data;
  },

  create: async (data: Partial<Category>) => {
    const response = await fetchWithError("/category", {
      method: "POST",
      body: JSON.stringify(data),
    });
    return response;
  },

  update: async (id: string, data: Partial<Category>) => {
      const response = await fetchWithError(`/category/${id}`, {
        method: "PUT",
        body: JSON.stringify(data),
      });
      return response;
    },

  delete: (id: string) => fetchWithError(`/category/${id}`, { method: "DELETE" }),

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

  getByRestaurantAndCategory: async (restaurantId: number) => {
    const response = await fetchWithError(`/product/byRestaurantAndCategory/${restaurantId}`);
    // Verifica si la respuesta es un array de categorías con productos
    if (!Array.isArray(response)) {
      throw new Error("Formato de respuesta inválido");
    }

    return response.map((categoryGroup: any) => ({
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
        available: product.isActive,
        quantity: product.quantity,
        restaurantId: product.restaurantId
        // Opcional: agregar más campos si los necesitas en Product
      })),
    }));
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
  create: async (data: Omit<Order, 'id' | 'createdAt' | 'updatedAt'>, email: string) => {
    const response = await fetchWithError(`/api/order?email=${encodeURIComponent(email)}`, {
      method: "POST",
      body: JSON.stringify(data),
    });
    return adaptOrder(response);
  },

  getByRestaurant: async (restaurantId: string) => {
    const data = await fetchWithError(`/api/order?restaurantId=${restaurantId}`);
    return data.map(adaptOrder);
  },

  getById: async (ord_id: string) => {
    const data = await fetchWithError(`/api/order/${ord_id}`);
    return adaptOrder(data);
  },

  updateStatus: async (ord_id: string, status: OrderStatus) => {
    const response = await fetchWithError(`/api/order/${ord_id}`, {
      method: "PATCH",
      body: JSON.stringify(status),
    });
    return adaptOrder(response);
  },

  delete: async (ord_id: string) => {
    await fetchWithError(`/api/order/${ord_id}`, { method: "DELETE" });
  },

  getByClient: async (cln_id: string) => {
    const data = await fetchWithError(`/api/order/byClientId/${cln_id}`);
    return data.map(adaptOrder);
  },

  getByDateRange: async (restaurantId: string, start: string, end: string) => {
    const data = await fetchWithError(
      `/api/order/byDate?restaurantId=${restaurantId}&start=${start}&end=${end}`
    );
    return data.map(adaptOrder);
  },

  getByClientAndDate: async (clientId: string, start: string, end: string) => {
    const data = await fetchWithError(
      `/api/order/byClientDate?clientId=${clientId}&start=${start}&end=${end}`
    );
    return data.map(adaptOrder);
  },

  getByRestaurantAndState: async (restaurantId: string, state: OrderStatus) => {
    const data = await fetchWithError(
      `/api/order/byRestaurantAndState?restaurantId=${restaurantId}&state=${state}`
    );
    return data.map(adaptOrder);
  }
};

// Reseñas (Review)
export const reviewAPI = {
  create: async (data: Omit<Review, 'id'>, userEmail: string) => {
    const response = await fetchWithError(`/api/review?email=${encodeURIComponent(userEmail)}`, {
      method: "POST",
      body: JSON.stringify(data),
    });
    return adaptReview(response);
  },

  getByRestaurant: async (restaurantId: string) => {
    const data = await fetchWithError(`/api/review/restaurant?restaurantId=${restaurantId}`);
    return data.map(adaptReview);
  },

  getById: async (id: string) => {
    const data = await fetchWithError(`/api/review/id?id=${id}`);
    return adaptReview(data);
  },

  // update: async (Omit<Review, 'id'>, userEmail: string) => {
  //   const response = await fetchWithError(`/api/review?email=${encodeURIComponent(userEmail)}`, {
  //     method: "PATCH",
  //     body: JSON.stringify(data),
  //   });
  //   return adaptReview(response);
  // },

  // Elimina reseña (para cliente dueño)
  delete: async (id: string, userEmail: string) => {
    await fetchWithError(`/api/review?id=${id}&email=${encodeURIComponent(userEmail)}`, {
      method: "DELETE"
    });
  }
};

// Users
// export const userAPI = {
//   update: (id: string, data: Partial<User>) =>
//     fetchWithError(`/user/${id}`, {
//       method: "PUT",
//       body: JSON.stringify(data),
//     }),

//   delete: (id: string) =>
//     fetchWithError(`/user/${id}`, { method: "DELETE" }),

//   getById: (id: string) => fetchWithError(`/user/${id}`),
// };
// services/api.ts

// export const userAPI = {
//   // CREATE (POST) 
//   create: async (data: Omit<User, 'id' | 'createdAt'>) => {
//     const response = await fetchWithError(`/api/user}`, {
//       method: "POST",
//       body: JSON.stringify(data),
//     });
//     return adaptUser(response);
//   },

//   update: async (id: string, data: Partial<Omit<User, 'id' | 'createdAt'>>) => {
//     const response = await fetchWithError(
//       `/api/user/${id}`,
//       {
//         method: "PUT",
//         body: JSON.stringify(data), // Envía data DIRECTAMENTE
//       }
//     );
//     return adaptUser(response);
//   },

//   delete: async (id: string) => {
//     await fetchWithError(
//       `/api/user/${id}}`,
//       { method: "DELETE" }
//     );
//   },

//   getById: async (id: string) => {
//     const data = await fetchWithError(`/api/user/${id}`);
//     return adaptUser(data);
//   }

//   // // EXTRA: Get Current User (si el backend llega a tenerlo)
//   // getCurrent: async () => {
//   //   const data = await fetchWithError("/auth/me");
//   //   return adaptUser(data);
//   // }

// };
