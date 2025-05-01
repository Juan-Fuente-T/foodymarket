// src/components/ProductDetailModal.tsx
import { useState, useEffect, useMemo } from 'react'
import { useCart } from '../../contexts/CartContext'
import { Button } from './../ui/button'
import { X } from 'lucide-react'
import { toast } from 'sonner'
import { createPortal } from 'react-dom'
import { useNavigate } from "react-router-dom";
import { Product } from '@/types/models';

interface ProductDetailModalProps {
    product: Product;
    onClose: () => void;
}

export function ProductDetailModal({
    product,
    onClose
}: ProductDetailModalProps) {
    const navigate = useNavigate();
    const { items, addItem, removeItem, isProductInCart, canAddProduct, updateItemQuantity } = useCart()
    const isInCart = isProductInCart(product.id)
    const canAdd = canAddProduct(product)

    // Encuentra la cantidad actual en el carrito para inicializar el estado local
    const cartItem = useMemo(() => {
        if (isInCart) {
          return items.find((item) => item.productId === product.id);
        }
        return undefined;
      }, [items, product.id, isInCart]);

    const [quantity, setQuantity] = useState(() => cartItem ? cartItem.quantity : 1);

    // Efecto para deshabilitar scroll
    useEffect(() => {
        document.body.classList.add('overflow-hidden')
        return () => document.body.classList.remove('overflow-hidden')
    }, [])

    // Sincronizar estado local si el item del carrito cambia externamente
    useEffect(() => {
        if (cartItem && cartItem.quantity !== quantity) {
            setQuantity(cartItem.quantity);
        }
    }, [cartItem, quantity, isInCart]);

    const handleAddToCart = () => {
        if (canAdd) {
            addItem(product, quantity)
            toast.success(`${product.name} añadido al carrito`)
        } else {
            toast.error("No se puede añadir el producto.", { description: "Verifica la disponibilidad o el carrito actual."});
        }
    }

    // --- Handlers que usan cartItem.id ---
    const handleIncreaseQuantity = () => {
        if (!cartItem) return;
        const stock = product.quantity;
        if (quantity < stock) {
          const newQuantity = quantity + 1;
          console.log(`Modal: Calling updateItemQuantity with ITEM ID: ${cartItem.id}, new quantity: ${newQuantity}`);
          updateItemQuantity(cartItem.id, newQuantity);
        } else {
            toast.warning("No hay más stock disponible.");
        }
    };
  
    const handleDecreaseQuantity = () => {
        if (!cartItem) return;
        const newQuantity = quantity - 1;
        if (newQuantity >= 1) {
           console.log(`Modal: Calling updateItemQuantity with ITEM ID: ${cartItem.id}, new quantity: ${newQuantity}`);
          updateItemQuantity(cartItem.id, newQuantity);
        } else {
          toast.info("La cantidad mínima es 1. Para eliminar, usa el botón Eliminar.");
        }
    };

    const handleRemoveItem = () => {
        if (!cartItem) return;
        console.log(`Modal: Calling removeItem with ITEM ID: ${cartItem.id}`);
        removeItem(cartItem.id);
    }

    return createPortal(
        <div
            className="fixed inset-0 bg-black/50 z-[9999] flex items-center justify-center p-4"
            onClick={onClose}
            data-testid="product-modal-backdrop"
        >
            <div
                className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto"
                onClick={(e) => e.stopPropagation()}
                onMouseDown={(e) => e.stopPropagation()}
            >
                <div className="relative p-6">
                    <button
                        onClick={onClose}
                        className="absolute top-4 right-4 rounded-full bg-gray-100 p-2 hover:bg-gray-200 z-10"
                    >
                        <X className="h-5 w-5" />
                    </button>

                    <div className="grid md:grid-cols-2 gap-6">
                        <div className="aspect-square overflow-hidden rounded-lg">
                            <img
                                src={product.image || '/placeholder-product.jpg'}
                                alt={product.name}
                                className="w-full h-full object-cover"
                            />
                        </div>

                        <div className="space-y-4">
                            <div>
                                <h2 className="text-2xl font-bold">{product.name}</h2>
                                <p className="text-gray-500">{product.categoryName}</p>
                            </div>

                            <p className="text-3xl font-semibold">
                                ${product.price.toFixed(2)}
                            </p>

                            <p className="text-gray-700">{product.description}</p>

                            <div className="flex flex-col gap-2">
                                {/* Estado del producto */}
                                {product.quantity <= 0 ? (
                                    <div className="bg-red-100 p-2 rounded-md text-red-700">
                                        Producto no disponible
                                    </div>
                                ) : (
                                    <div className="bg-green-100 p-2 rounded-md text-green-700">
                                        Disponibles: {product.quantity} unidad{product.quantity !== 1 ? 'es' : ''}
                                    </div>
                                )}

                                {isInCart ? (
                                    <>
                                        {/* Controles para productos en el carrito */}
                                        <div className="flex items-center justify-between bg-gray-100 p-2 rounded-md">
                                            <span className="text-gray-700">
                                                En carrito: {quantity} unidad{quantity !== 1 ? 'es' : ''}
                                            </span>

                                            <div className="flex items-center gap-2">
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    onClick={handleDecreaseQuantity}
                                                    disabled={quantity <= 1}
                                                >
                                                    -
                                                </Button>
                                                <span className="w-8 text-center">{quantity}</span>
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    onClick={handleIncreaseQuantity}
                                                    disabled={quantity >= product.quantity}
                                                >
                                                    +
                                                </Button>
                                            </div>
                                        </div>

                                        <div className="flex gap-2">
                                            <Button
                                                variant="destructive"
                                                onClick={handleRemoveItem}
                                                className="flex-1"
                                            >
                                                Eliminar
                                            </Button>

                                            <Button
                                                variant="default"
                                                onClick={() => {
                                                    const toastId = toast.loading('Redirigiendo...');
                                                    setTimeout(() => {
                                                        toast.dismiss(toastId);
                                                        navigate('/cart');
                                                    }, 1000);
                                                }}
                                                className="flex-1 bg-green-600 hover:bg-green-700"
                                            >
                                                Ver carrito
                                            </Button>
                                        </div>
                                    </>
                                ) : (
                                    product.quantity > 0 && (
                                        <Button
                                            onClick={() => {
                                                handleAddToCart();
                                                toast.success('Producto añadido al carrito');
                                            }}
                                            disabled={!canAdd}
                                            className="w-full bg-primary hover:bg-primary-dark"
                                        >
                                            Añadir al carrito
                                        </Button>
                                    )
                                )}

                                {!isInCart && product.quantity > 0 && (
                                    <Button
                                        variant="outline"
                                        onClick={() => {
                                            const toastId = toast.loading('Procesando...');
                                            handleAddToCart();
                                            setTimeout(() => {
                                                toast.dismiss(toastId);
                                                navigate('/cart');
                                            }, 1000);
                                        }}
                                        className="w-full border-primary text-primary hover:bg-primary/10"
                                    >
                                        Comprar ahora
                                    </Button>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>,
        document.body
    )
}
