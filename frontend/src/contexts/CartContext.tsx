
import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from "react";
import { Product, OrderItem, Restaurant } from "@/types/models";
import { toast } from "sonner";
import Decimal from 'decimal.js'

interface CartContextType {
  items: OrderItem[];
  restaurant: Restaurant | null;
  addItem: (product: Product, quantity: number, notes?: string) => void;
  removeItem: (itemId: string) => void;
  updateItemQuantity: (itemId: string, quantity: number) => void;
  clearCart: () => void;
  isProductInCart: (productId: string) => boolean;
  totalItems: number;
  totalPrice: string;
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
  // const totalPrice = items.reduce((sum, item) => (sum + parseFloat(item.subtotal), 0)).toString();
  const totalPriceDecimal = items.reduce((sum, item) => {
    if (item.subtotal && typeof item.subtotal === 'string') {
      try {
        // Convierte el string del subtotal actual a un objeto Decimal
        const subtotalDecimal = new Decimal(item.subtotal);
        // Suma el subtotal actual al acumulador usando el método .plus() de la librería
        return sum.plus(subtotalDecimal);
      } catch (error) {
        console.error(`Subtotal inválido encontrado: ${item.subtotal}, omitiendo.`);
        return sum; // Devuelve la suma sin cambios
      }
    }
    return sum; // Si no hay subtotal o no es string, devuelve la suma sin cambios
  }, new Decimal(0));
  const totalPrice = totalPriceDecimal.toFixed(2);

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
      // Ensure we have restaurant data when adding the first product
      console.log("Restaurant not set, setting from product:", product.restaurantId);
      // We need to fetch restaurant info from somewhere or create a placeholder
      // For now, we'll create a minimal restaurant object to ensure checkout works
      const productRestaurant: Restaurant = {
        id: Number(product.restaurantId),
        name: "Restaurant", // These will be updated with real data in a full implementation
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
              subtotal: new Decimal(product.price || 0) // Convierte precio (string) a Decimal (o 0 si falta)
                .times(item.quantity + quantity)
                .toFixed(2), // Convierte resultado a string con 2 decimales
              productName: product.name
            }
            : item
        )
      );
    } else {
      let newSubtotalString = '0.00'; // Valor por defecto si algo falla

  try {
    // Convierte el precio (string) y la cantidad (number) a objetos Decimal
    const priceDecimal = new Decimal(product.price || 0);
    const quantityDecimal = new Decimal(quantity || 0); 
    // Multiplica usando el método .times() de la librería para precisión exacta
    const newSubtotalDecimal = priceDecimal.times(quantityDecimal);
    // Convierte el resultado Decimal de nuevo a un string con 2 decimales
    newSubtotalString = newSubtotalDecimal.toFixed(2);

  } catch (error) {
      console.error(`Error calculando subtotal para nuevo item (producto ${product.id}):`, error);
      // Se podría no añadir el item o mostrar un error al usuario
      // Por ahora mantiene el subtotal en '0.00'
  }

  const newItem: OrderItem = { 
    id: Date.now().toString(),
    productId: product.id,     
    productName: product.name,     
    quantity: quantity,          
    productPrice: product.price,     
    subtotal: newSubtotalString 
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
          try {
            const currentSubtotal = new Decimal(item.subtotal || 0); // Subtotal actual a Decimal
            const currentQuantity = new Decimal(item.quantity || 1); // Cantidad actual a Decimal (evitar dividir por 0)
            // Calcula precio unitario con precisión decimal
            const pricePerUnit = currentQuantity.isZero() ? new Decimal(0) : currentSubtotal.dividedBy(currentQuantity);
            const newQuantity = new Decimal(quantity); // Nueva cantidad a Decimal

            return {
              ...item,
              quantity: quantity,
              // Calcula nuevo subtotal con precisión decimal y convierte a string
              subtotal: pricePerUnit.times(newQuantity).toFixed(2)
            };
          } catch (error) {
            console.error("Error actualizando subtotal para item:", item.id, error);
            return item; // Devuelve el item sin cambios si hay error
          }
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
