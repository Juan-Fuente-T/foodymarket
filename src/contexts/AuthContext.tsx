
import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { User } from "@/types/models";
import { authAPI } from "@/services/api";
import { toast } from "@/lib/toast";

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (userData: Partial<User>) => Promise<User | void>;
  logout: () => Promise<void>;
  updateUser: (userData: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Token storage key
const TOKEN_STORAGE_KEY = "food_delivery_token";
const USER_STORAGE_KEY = "food_delivery_user";

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

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
      try {
        // Try to get user from localStorage first for quick init
        const storedUser = localStorage.getItem(USER_STORAGE_KEY);
        if (storedUser) {
          setUser(JSON.parse(storedUser));
        }

        // If we have a token, verify with backend
        const token = getToken();
        if (token) {
          const userData = await authAPI.getCurrentUser();
          setUser(userData);
          // Update stored user
          localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(userData));
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
      const response = await authAPI.login(email, password);
      if (response.token) {
        storeToken(response.token);
        setUser(response.user);
        localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(response.user));
        toast.success("Login successful!");
        return response.user;
      } else {
        throw new Error("No token received");
      }
    } catch (error) {
      console.error("Login failed:", error);
      toast.error("Login failed. Please check your credentials.");
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (userData: Partial<User>) => {
    setIsLoading(true);
    try {
      const response = await authAPI.register(userData);
      if (response.token) {
        storeToken(response.token);
        setUser(response.user);
        localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(response.user));
        toast.success("Registration successful!");
        return response.user;
      } else {
        throw new Error("No token received");
      }
    } catch (error) {
      console.error("Registration failed:", error);
      toast.error("Registration failed. Please try again.");
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
      toast.success("Logged out successfully!");
    } catch (error) {
      console.error("Logout failed:", error);
      // Still remove token and user even if logout API fails
      removeToken();
      setUser(null);
      toast.error("Failed to log out properly.");
    } finally {
      setIsLoading(false);
    }
  };

  const updateUser = (userData: Partial<User>) => {
    if (user) {
      const updatedUser = { ...user, ...userData };
      setUser(updatedUser);
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(updatedUser));
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

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
