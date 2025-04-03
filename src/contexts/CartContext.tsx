
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
    const storedCart = localStorage.getItem('shoppingCart');
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
  const [items, setItems] = useState<OrderItem[]>([]);
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
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
    console.log("Context: Updating existing PRODUCT:", product);
    if (restaurant && Number(product.restaurantId) !== restaurant.id) {
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
    console.log("Context: Updating existing item1:", existingItem);
    if (existingItem) {
      setItems(
        items.map((item) =>
          item.productId === product.id
            ? { ...item, quantity: item.quantity + quantity, notes: notes || '' }
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
      console.log("Context: Updating existing item2:", newItem);
      setItems([...items, newItem]);
    }

    toast.success(`Added ${product.name} to cart`);
  };

  // const removeItem = (itemId: string) => {
  //   setItems(items.filter((item) => item.id !== itemId));
  //   toast.success("Item removed from cart");
  // };

  // const updateItemQuantity = (itemId: string, quantity: number) => {
  //   if (quantity <= 0) {
  //     removeItem(itemId);
  //     return;
  //   }

  //   setItems(
  //     items.map((item) =>
  //       item.id === itemId ? { ...item, quantity } : item
  //     )
  //   );
  // };

  const removeItem = useCallback((itemId: string) => { // Acepta itemId
    console.log(`[Context] removeItem - itemId: ${itemId}`);
    setItems(prevItems => {
      const initialLength = prevItems.length;
      const updatedItems = prevItems.filter((item) => item.id !== itemId); // <-- Compara con item.id
      // ... (lógica para limpiar restaurante y logs) ...
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

  const updateItemQuantity = useCallback((itemId: string, quantity: number) => { // Acepta itemId
    console.log(`[Context] updateItemQuantity - itemId: ${itemId}, newQuantity: ${quantity}`);
    if (quantity <= 0) {
      removeItem(itemId);
      return;
    }

    setItems(prevItems => {
      let itemFound = false;
      const updatedItems = prevItems.map((item) => {
        if (item.id === itemId) { // <-- Compara con item.id
          itemFound = true;
          // Necesitas el precio unitario. Añádelo a OrderItem o búscalo en el producto original.
          // Si lo añadiste a OrderItem como 'price':
          // const pricePerUnit = item.price;
          // Si no, necesitas buscar el producto (más complejo) o calcularlo si es posible:
           const pricePerUnit = item.subtotal / item.quantity; // Puede ser impreciso si quantity era 0
          return {
            ...item,
            quantity: quantity,
            subtotal: pricePerUnit * quantity // Recalcula subtotal
          };
        }
        return item;
      });
      // ... (logs y return updatedItems) ...
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
    console.log("Data in ContextXX: ", restaurant);
    console.log("Data in Context", restaurant.id, product, product.restaurantId);
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
