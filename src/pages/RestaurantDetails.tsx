import React, { useEffect, useState } from 'react';
import { useParams, Link, Navigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useCart } from '@/contexts/CartContext';
import { useAuth } from "../hooks/use-auth";
import { restaurantAPI, productAPI } from '@/services/api';
import { Product, Restaurant, GroupedProduct } from '@/types/models';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Star, MapPin, Clock, Phone, ArrowLeft } from 'lucide-react';  // Iconos
import { Layout } from '@/components/layout/Layout';
import { ProductCard } from '@/components/product/ProductCard';
import { ProductDetailModal } from '../components/product/ProductDetailModal';

const RestaurantDetails = () => {
  const { id } = useParams<{ id: string }>();
  const { setRestaurant } = useCart();
  const [selectedProduct, setSelectedProduct] = useState<null | Product>(null);
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();

  // Redirigir a login si no está autenticado
  if (!authLoading && !isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Obtener restaurante
  const { data: restaurant, isLoading: isLoadingRestaurant } = useQuery({
    queryKey: ["restaurant", id],
    queryFn: () => restaurantAPI.getById(id as string),
    enabled: !!id && isAuthenticated,
  });

  // Obtener productos
  const { data: categoriesWithProducts = [], isLoading: isLoadingProducts, error } = useQuery({
    queryKey: ["products", id],
    queryFn: async () => {
      const response = await productAPI.getByRestaurantAndCategory(id);
      console.log("Categories", response);
      return response || []; // Asegura array vacío si response es undefined
    },
    enabled: !!id && !!restaurant && isAuthenticated
  });
  
  const totalProducts = categoriesWithProducts.reduce(
    (acc, category) => acc + category.products.length, 
    0
  );

  // Setear restaurante en el carrito
  useEffect(() => {
    if (restaurant) setRestaurant(restaurant);
  }, [restaurant]);

  // --- Renderizado simplificado ---
  if (authLoading) {
    return (
      <Layout>
        <div className="flex justify-center items-center h-screen">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-food-600"></div>
        </div>
      </Layout>
    );
  }

  if (isLoadingRestaurant) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto p-8">
          <div className="animate-pulse space-y-4">
            <div className="h-64 bg-gray-200 rounded-xl mb-8"></div>
            <div className="h-8 bg-gray-200 w-1/3 mb-4"></div>
            <div className="h-6 bg-gray-200 w-2/3 mb-8"></div>
          </div>
        </div>
      </Layout>
    );
  }

  if (!restaurant) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto p-16 text-center">
          <h1 className="text-2xl font-bold mb-4">Restaurante no encontrado</h1>
          <Button asChild className="bg-food-600 hover:bg-food-700">
            <Link to="/restaurants">Volver a restaurantes</Link>
          </Button>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Botón de volver (inline) */}
        <Link to="/restaurants" className="inline-flex items-center text-food-600 hover:text-food-800 mb-6">
          <ArrowLeft className="h-4 w-4 mr-1" />
          Volver
        </Link>

        {/* Header del restaurante (inline) */}
        <div className="relative rounded-2xl overflow-hidden mb-8">
          <img
            src={restaurant.coverImage || "https://via.placeholder.com/800x400"}
            alt={restaurant.name}
            className="w-full h-80 object-cover"
          />
          <div className="absolute top-4 left-4">
            <Badge variant="secondary" className="bg-white/90 text-food-700">
              {restaurant.category}
            </Badge>
          </div>
          <div className="absolute bottom-0 left-0 w-full bg-gradient-to-t from-black/80 to-transparent p-4">
            <h1 className="text-3xl font-bold text-white">{restaurant.name}</h1>
            <div className="flex items-center text-white mt-2">
              <Star className="h-5 w-5 mr-1 fill-yellow-400 stroke-yellow-400" />
              <span>{restaurant.rating?.toFixed(1) || 'N/A'}</span>
            </div>
          </div>
        </div>

        {/* Menú y productos */}
        <div className="md:grid md:grid-cols-3 md:gap-8">
          <div className="md:col-span-2">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Menú</h2>

            {isLoadingProducts ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                {Array(6).fill(0).map((_, i) => (
                  <div key={i} className="h-56 bg-gray-200 rounded-xl animate-pulse"></div>
                ))}
              </div>
            ) : (
              <>
                {categoriesWithProducts.map((categoryGroup) => (
                  <div key={categoryGroup.categoryName} className="mb-8">
                    <h3 className="text-xl font-semibold mb-4">
                      {categoryGroup.categoryName}
                    </h3>
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
                      {categoryGroup.products.map((product) => (
                        <ProductCard
                          key={product.id}
                          product={product}
                          onOpenModal={(product) => setSelectedProduct(product)}
                        />
                      ))}
                    </div>
                  </div>
                ))}
                {!isLoadingProducts && totalProducts === 0 && (
                  <div className="text-center py-16">
                    <p className="text-gray-600">No hay productos disponibles.</p>
                  </div>
                )}
              </>
            )}
          </div>
            {/* Información del restaurante (inline) */}
            <div className="bg-gray-50 rounded-2xl p-6 sticky top-20">
              <h3 className="text-xl font-semibold text-gray-900 mb-4">Información</h3>
              <div className="space-y-4">
                <div className="flex items-center text-gray-600">
                  <MapPin className="h-4 w-4 mr-2" />
                  <span>{restaurant.address || 'Sin dirección'}</span>
                </div>
                <div className="flex items-center text-gray-600">
                  <Clock className="h-4 w-4 mr-2" />
                  <span>{restaurant.openingHours || 'Horario no disponible'}</span>
                </div>
                <div className="flex items-center text-gray-600">
                  <Phone className="h-4 w-4 mr-2" />
                  <span>{restaurant.phone || 'Sin teléfono'}</span>
                </div>
              </div>
              <Button className="w-full mt-4 bg-food-600 hover:bg-food-700">
                Ver en mapa
              </Button>
            </div>
          </div>
        </div>
        {selectedProduct && (
        <ProductDetailModal
          product={selectedProduct}
          onClose={() => setSelectedProduct(null)}
        />
      )}
    </Layout>
  );
};

export default RestaurantDetails;
