import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { useNavigate } from 'react-router-dom';
import { User, UserRole } from "@/types/models";
import { toast } from "@/lib/toast";
import { authAPI, userAPI } from "@/services/api";

export interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (userData: Partial<User>) => Promise<User | void>;
  logout: () => Promise<void>;
  updateUser: (userData: Partial<User>) => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Token storage key - must match the one used in api.ts
const TOKEN_STORAGE_KEY = "food_delivery_token";
const USER_STORAGE_KEY = "food_delivery_user";

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  // Helper to store token
  const storeToken = (token: string) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  };

  // Helper to get token
  const getToken = () => {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  };

  // Helper to remove token
  const removeToken = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
  };

  useEffect(() => {
    const initAuth = async () => {
      setIsLoading(true);
      try {
        // Try to get user from localStorage first for quick init
        const storedUser = localStorage.getItem(USER_STORAGE_KEY);
        if (storedUser) {
          setUser(JSON.parse(storedUser));
        }

        // If we have a token, verify with backend
        const token = getToken();
        if (token) {
          console.log("Found stored token, trying to fetch current user");
          const userData = await authAPI.getCurrentUser();
          if (userData) {
            console.log("Successfully retrieved user data:", userData);
            setUser(userData);
            // Update stored user
            localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(userData));
          } else {
            // If userData is null, clear the token and user
            console.log("No user data returned despite having token, clearing auth state");
            removeToken();
            setUser(null);
          }
        }
      } catch (error) {
        console.error("Failed to fetch current user:", error);
        // Clear invalid token/user
        removeToken();
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    initAuth();
  }, []);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    try {
      console.log("AuthContext: Attempting login with email:", email);
      const response = await authAPI.login(email, password); // response = { access_token: "...", message: "...", user: {...} }
      console.log("Login response received in AuthContext:", response);

      if (response) {
        // --- CORRECCIÓN AQUÍ ---
        if (response.access_token) { // <-- Comprueba la propiedad correcta 'access_token'
          console.log("Access Token received, storing token:", response.access_token.substring(0, 10) + "...");
          storeToken(response.access_token); // <-- Usa la propiedad correcta para guardar

          // Asumiendo que la info del usuario está en response.user
          const userData = response.user;
          if (userData) {
              console.log("User data received:", userData);
              setUser(userData); // Actualiza el estado del usuario en el contexto
              localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(userData)); // Guarda el usuario también
          } else {
              console.warn("Login successful but no user data found in response.user field. Fetching profile...");
              // Si el login no devuelve user, intenta obtenerlo con otra llamada
              try {
                  const profileData = await userAPI.getProfile(); // Llama a GET /api/user
                  if (profileData) {
                      setUser(profileData);
                      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(profileData));
                  } else {
                       console.error("Could not fetch user profile after login.");
                       // Quizás limpiar token si no se puede obtener perfil? O dejarlo y que la app maneje un user null?
                  }
              } catch (profileError) {
                   console.error("Error fetching profile after login:", profileError);
                   // Limpiar token si falla la obtención del perfil podría ser una opción
                   // removeToken();
                   // setUser(null);
              }
          }
        } else {
          // Ya no debería entrar aquí si el backend envía 'access_token'
          console.error("No 'access_token' property found in login response object:", response);
          throw new Error("Authentication failed: No access_token received");
        }
      } else {
        throw new Error("Invalid response from server");
      }
    } catch (error) {
      console.error("Login failed in AuthContext:", error);
      // Limpiar estado en caso de error
      removeToken();
      setUser(null);
      throw error; // Relanza para que el componente Login pueda manejarlo (mostrar mensaje, etc.)
    } finally {
      setIsLoading(false);
    }
  };
  const register = async (userData: Partial<User>) => {
    setIsLoading(true);
    try {
      // ... (llamada a authAPI.register) ...
      const response = await authAPI.register(userData);
      console.log("Registration response received in AuthContext:", response);

      if (response && response.access_token) {
        console.log("Access Token received from registration, storing token");
        storeToken(response.access_token);

        const userObj = response.user;
        if (userObj) {
            console.log("User data received from registration:", userObj);
            setUser(userObj);
            localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(userObj));
            return userObj;
        } else {
            console.warn("Registration successful but no user data found in response.user field");
            // Podrías intentar obtener perfil aquí también si fuera necesario
            return undefined;
        }
      } else {
        console.error("No valid 'access_token' or 'user' received in registration response object:", response);
        throw new Error("Registration failed: No valid access token or user received");
      }
    } catch (error) {
      console.error("Registration failed:", error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };
  const logout = async () => {
    setIsLoading(true);
    try {
      await authAPI.logout();
      removeToken();
      setUser(null);
      console.log("Local state cleared.");
    } catch (error) {
      console.error("Logout failed:", error);
      // Still remove token and user even if logout API fails
      removeToken();
      setUser(null);
    } finally {
      setIsLoading(false);
      navigate('/', { replace: true });
    }
  };

  const updateUser = async (userData: Partial<User>) => {
    try {
      setIsLoading(true);
      // If we're updating the role, we need to update the backend
      if (user && userData.role && userData.role !== user.role) {
        console.log("Updating user role from", user.role, "to", userData.role);
        // Here you would normally call an API to update the user role
        // For now, we'll just update the local state
      }

      const updatedUser = { ...user, ...userData } as User;
      setUser(updatedUser);
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(updatedUser));
      console.log("User updated successfully:", updatedUser);
      return updatedUser;
    } catch (error) {
      console.error("Failed to update user:", error);
      toast.error("Failed to update user profile");
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        register,
        logout,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
