
import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
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
}

const CartContext = createContext<CartContextType | undefined>(undefined);

const CART_STORAGE_KEY = "food_delivery_cart";

export const CartProvider = ({ children }: { children: ReactNode }) => {
  const [items, setItems] = useState<OrderItem[]>([]);
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);

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
    localStorage.setItem(
      CART_STORAGE_KEY,
      JSON.stringify({ items, restaurant })
    );
  }, [items, restaurant]);

  const addItem = (product: Product, quantity: number, notes?: string) => {
    if (restaurant && Number(product.restaurantId) !== restaurant.id) {
      toast.warning("Different Restaurant", {
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
            ? { ...item, quantity: item.quantity + quantity, notes: notes || ''}
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

  const removeItem = (itemId: string) => {
    setItems(items.filter((item) => item.id !== itemId));
    toast.success("Item removed from cart");
  };

  const updateItemQuantity = (itemId: string, quantity: number) => {
    if (quantity <= 0) {
      removeItem(itemId);
      return;
    }

    setItems(
      items.map((item) =>
        item.id === itemId ? { ...item, quantity } : item
      )
    );
  };

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

  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  
  const totalPrice = items.reduce(
    (sum, item) => sum + item.subtotal,
    0
  );

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
