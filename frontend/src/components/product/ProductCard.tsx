
import React, { useState } from "react";
import { Product } from "@/types/models";
import { useCart } from "@/contexts/CartContext";
import { Button } from "@/components/ui/button";
import { Plus, Check } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { useNavigate } from "react-router-dom";
import { toast } from 'sonner';
import { ProductDetailModal } from './ProductDetailModal';

interface ProductCardProps {
  product: Product
  onOpenModal: (product: Product) => void
}

export function ProductCard({ product, onOpenModal }: ProductCardProps) {
  const navigate = useNavigate();
  const { addItem, isProductInCart, canAddProduct } = useCart();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleClick = () => {
    onOpenModal(product)
  }

  const handleAddToCart = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    addItem(product, 1);
  };

  const isInCart = isProductInCart(product.id);
  const canAdd = canAddProduct(product);

  if (!product.id) {
    console.error("ProductID is missing!", product);
    return null;
  }
  
  return (
    <div className="food-card bg-white rounded-xl overflow-hidden shadow-sm h-full flex flex-col hover:shadow-md transition-shadow"
      onClick={handleClick}
    >
      <div className="relative h-40 overflow-hidden">
        <img
          src={product.image || "https://via.placeholder.com/300"}
          alt={product.name}
          className="w-full h-full object-cover"
        />
        {!product.available && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
            <Badge variant="destructive" className="text-sm px-2 py-1 bg-red-500">
              Unavailable
            </Badge>
          </div>
        )}
        {product.categoryName && (
          <Badge className="absolute top-2 right-2 bg-white/90 text-food-700">
            {product.categoryName}
          </Badge>
        )}
      </div>
      <div className="p-4 flex flex-col flex-grow">
        <div className="flex justify-between items-start mb-2">
          <h3 className="text-lg font-medium text-gray-900 line-clamp-1">
            {product.name}
          </h3>
          <span className="text-food-600 font-semibold">
            ${product.price.toFixed(2)}
          </span>
        </div>
        <p className="text-sm text-gray-500 line-clamp-2 mb-4 flex-grow">
          {product.description}
        </p>
        <Button
          onClick={handleAddToCart}
          className="w-full transition-all disabled:opacity-50 disabled:bg-gray-300 disabled:cursor-not-allowed cursor-pointer"
          disabled={!product.available || isInCart || !canAdd}
          variant={isInCart ? "outline" : "default"}
        >
          {isInCart ? (
            <>
              <Check className="mr-2 h-4 w-4" /> Added
            </>
          ) : (
            <>
              <Plus className="mr-2 h-4 w-4" /> Add to Cart
            </>
          )}
        </Button>
      </div>
      {isModalOpen && (
        <ProductDetailModal
          product={product}
          onClose={() => setIsModalOpen(false)}
        />
      )}
    </div>
  );
}
