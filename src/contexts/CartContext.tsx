
import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from "react";
import { Product, OrderItem, Restaurant } from "@/types/models";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";

interface CartContextType {
  items: OrderItem[];
  restaurant: Restaurant | null;
  addItem: (product: Product, quantity: number, notes?: string) => void;
  removeItem: (itemId: string) => void;
  updateItemQuantity: (itemId: string, quantity: number) => void;
  clearCart: () => void;
  isProductInCart: (productId: string) => boolean;
  totalItems: number;
  totalPrice: number;
  setRestaurant: (restaurant: Restaurant) => void;
  canAddProduct: (product: Product) => boolean;
  getItemQuantity: (productId: string) => number;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

const CART_STORAGE_KEY = "food_delivery_cart";

// Helper para obtener el carrito inicial desde localStorage
const getInitialCart = () => {
  try {
    const storedCart = localStorage.getItem(CART_STORAGE_KEY);
    if (storedCart) {
      const parsedCart = JSON.parse(storedCart);
      // Validar que la estructura sea la esperada (opcional pero recomendado)
      if (parsedCart.items && parsedCart.restaurant !== undefined) { // Asegúrate que restaurant también se guarde/cargue
        return {
          items: parsedCart.items as OrderItem[],
          restaurant: parsedCart.restaurant as Restaurant | null
        };
      }
    }
  } catch (error) {
    console.error("Error reading cart from localStorage:", error);
  }
  // Estado inicial por defecto si no hay nada en localStorage o hay error
  return { items: [], restaurant: null };
};

export const CartProvider = ({ children }: { children: ReactNode }) => {
  const initialState = getInitialCart();
  const [items, setItems] = useState<OrderItem[]>(initialState.items || []);
  const [restaurant, setRestaurant] = useState<Restaurant | null>(initialState.restaurant || null);
  
  // Calcular totales (memoizar si se vuelve complejo)
  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = items.reduce((sum, item) => sum + item.subtotal, 0);

  useEffect(() => {
    const savedCart = localStorage.getItem(CART_STORAGE_KEY);
    if (savedCart) {
      try {
        const { items, restaurant } = JSON.parse(savedCart);
        setItems(items || []);
        setRestaurant(restaurant || null);
      } catch (error) {
        console.error("Failed to parse cart from localStorage:", error);
      }
    }
  }, []);

  useEffect(() => {
    try {
      localStorage.setItem(
        CART_STORAGE_KEY,
        JSON.stringify({ items, restaurant })
      );
    } catch (error) {
      console.error("Error saving cart to localStorage:", error);
    }
  }, [items, restaurant]);

  const addItem = (product: Product, quantity: number, notes?: string) => {
    if (quantity <= 0) return;
    
    // Make sure we set the restaurant when adding the first item
    if (items.length === 0 && !restaurant) {
      // Fetch restaurant info - we need to set the restaurant for the cart
      const productRestaurant: Restaurant = {
        id: Number(product.restaurantId),
        name: "", // These will be filled in by a real API call in a full implementation
        description: "",
        address: "",
        phone: "",
        email: "",
        logo: "",
        openingHours: "",
        coverImage: "",
        cuisineId: 0,
        cuisineName: "",
        ownerId: "",
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      setRestaurant(productRestaurant);
      console.log("Setting restaurant from product:", productRestaurant);
    } else if (restaurant && Number(product.restaurantId) !== restaurant.id) {
      toast.warning("Different Restaurants", {
        description: "Your cart contains items from a different restaurant. Would you like to clear your cart?",
        action: {
          label: "Clear cart",
          onClick: () => {
            clearCart();
            addItem(product, quantity, notes);
          }
        }
      });
      return;
    }

    const existingItem = items.find((item) => item.productId === product.id);
    
    if (existingItem) {
      setItems(
        items.map((item) =>
          item.productId === product.id
            ? { 
                ...item, 
                quantity: item.quantity + quantity, 
                subtotal: (item.quantity + quantity) * (product.price)
              }
            : item
        )
      );
    } else {
      const newItem: OrderItem = {
        id: Date.now().toString(),
        productId: product.id,
        quantity,
        subtotal: product.price * quantity
      };
      setItems([...items, newItem]);
    }

    toast.success(`Added ${product.name} to cart`);
  };

  const removeItem = useCallback((itemId: string) => { 
    console.log(`[Context] removeItem - itemId: ${itemId}`);
    setItems(prevItems => {
      const initialLength = prevItems.length;
      const updatedItems = prevItems.filter((item) => item.id !== itemId);
      
      if (updatedItems.length === 0 && initialLength > 0) {
        setRestaurant(null);
      }
      
      if (updatedItems.length !== initialLength) {
        toast.success("Item removed from cart");
      } else {
        console.warn(`[Context] removeItem: Item ID ${itemId} not found.`);
      }
      
      return updatedItems;
    });
  }, []); 

  const updateItemQuantity = useCallback((itemId: string, quantity: number) => {
    console.log(`[Context] updateItemQuantity - itemId: ${itemId}, newQuantity: ${quantity}`);
    if (quantity <= 0) {
      removeItem(itemId);
      return;
    }

    setItems(prevItems => {
      let itemFound = false;
      const updatedItems = prevItems.map((item) => {
        if (item.id === itemId) {
          itemFound = true;
          const pricePerUnit = item.subtotal / item.quantity;
          return {
            ...item,
            quantity: quantity,
            subtotal: pricePerUnit * quantity
          };
        }
        return item;
      });
      
      if (!itemFound) console.warn(`[Context] updateItemQuantity: Item ID ${itemId} not found.`);
      return updatedItems;
    });
  }, [removeItem]); 

  const clearCart = () => {
    setItems([]);
    setRestaurant(null);
    toast.success("Cart cleared");
  };

  const isProductInCart = (productId: string) => {
    return items.some((item) => item.productId === productId);
  };

  const canAddProduct = (product: Product) => {
    return !restaurant || restaurant.id === Number(product.restaurantId);
  };

  const getItemQuantity = (productId: string): number => {
    const item = items.find(item => item.productId === productId);
    return item ? item.quantity : 0;
  };

  return (
    <CartContext.Provider
      value={{
        items,
        restaurant,
        addItem,
        removeItem,
        updateItemQuantity,
        clearCart,
        isProductInCart,
        totalItems,
        totalPrice,
        setRestaurant,
        canAddProduct,
        getItemQuantity
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error("useCart must be used within a CartProvider");
  }
  return context;
};
