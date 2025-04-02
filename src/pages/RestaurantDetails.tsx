
import React, { useState, useEffect } from "react";
import { Layout } from "@/components/layout/Layout";
import { useParams, Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { restaurantAPI, productAPI } from "@/services/api";
import { Restaurant, Product } from "@/types/models";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { ChevronRight, MapPin, Clock, Star, ArrowLeft } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ProductCard } from "@/components/restaurant/ProductCard";
import { useCart } from "@/contexts/CartContext";

const RestaurantDetails = () => {
  const { id } = useParams<{ id: string }>();
  const { setRestaurant } = useCart();
  
  const { data: restaurant, isLoading: isLoadingRestaurant } = useQuery({
    queryKey: ["restaurant", id],
    queryFn: () => restaurantAPI.getById(id as string),
    enabled: !!id,
  });
  
  // Use useEffect to set restaurant in cart context after data is loaded
  useEffect(() => {
    if (restaurant) {
      setRestaurant(restaurant);
    }
  }, [restaurant, setRestaurant]);
  
  const { data: products = [], isLoading: isLoadingProducts } = useQuery({
    queryKey: ["products", id],
    queryFn: () => productAPI.getByRestaurant(id as string),
    enabled: !!id && !!restaurant,
  });
  
  // Agrupar productos por categoría
  const productsByCategory = React.useMemo(() => {
    if (!products.length) return {};
    
    return products.reduce((acc, product) => {
      const categoryName = product.category?.name || 'Uncategorized';
      if (!acc[categoryName]) {
        acc[categoryName] = [];
      }
      acc[categoryName].push(product);
      return acc;
    }, {} as Record<string, Product[]>);
  }, [products]);
  
  // Obtener las categorías únicas de los productos
  const categories = React.useMemo(() => {
    return Object.keys(productsByCategory);
  }, [productsByCategory]);
  
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  
  // Establecer la primera categoría como seleccionada cuando se cargan los datos
  useEffect(() => {
    if (categories.length > 0 && !selectedCategory) {
      setSelectedCategory(categories[0]);
    }
  }, [categories, selectedCategory]);
  
  // Filtrar productos por categoría seleccionada
  const filteredProducts = React.useMemo(() => {
    if (!selectedCategory) return products;
    return productsByCategory[selectedCategory] || [];
  }, [selectedCategory, products, productsByCategory]);
  
  if (isLoadingRestaurant) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Skeleton className="h-64 w-full rounded-xl mb-8" />
          <Skeleton className="h-10 w-1/3 mb-4" />
          <Skeleton className="h-6 w-2/3 mb-8" />
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {Array(6).fill(0).map((_, i) => (
              <Skeleton key={i} className="h-64 w-full rounded-xl" />
            ))}
          </div>
        </div>
      </Layout>
    );
  }
  
  if (!restaurant) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 text-center">
          <h1 className="text-2xl font-bold mb-4">Restaurant not found</h1>
          <p className="text-gray-600 mb-8">The restaurant you're looking for doesn't exist or has been removed.</p>
          <Button asChild className="bg-food-600 hover:bg-food-700">
            <Link to="/restaurants">Browse Restaurants</Link>
          </Button>
        </div>
      </Layout>
    );
  }
  
  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Link to="/restaurants" className="inline-flex items-center text-food-600 hover:text-food-800 mb-6">
          <ArrowLeft className="h-4 w-4 mr-1" />
          Back to Restaurants
        </Link>

        {/* Restaurant Header */}
        <div className="relative rounded-2xl overflow-hidden mb-8">
          <img
            src={restaurant.coverImage}
            alt={restaurant.name}
            className="w-full h-80 object-cover"
          />
          <div className="absolute top-4 left-4 flex flex-wrap gap-2">
            {restaurant.category && (
              <Badge variant="secondary" className="bg-white/90 backdrop-blur-sm text-food-700">
                {restaurant.category.name}
              </Badge>
            )}
          </div>
          <div className="absolute bottom-0 left-0 w-full bg-gradient-to-t from-black/80 to-transparent p-4">
            <h1 className="text-3xl font-bold text-white">{restaurant.name}</h1>
            <div className="flex items-center text-white mt-2">
              <Star className="h-5 w-5 mr-1 fill-yellow-400 stroke-yellow-400" />
              <span className="font-medium">{restaurant.rating.toFixed(1)}</span>
              <span className="text-gray-300 ml-2">({restaurant.reviewCount} reviews)</span>
            </div>
          </div>
        </div>

        {/* Restaurant Details */}
        <div className="md:grid md:grid-cols-3 md:gap-8">
          <div className="md:col-span-2">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Menu</h2>

            {/* Category Tabs */}
            {categories.length > 1 && (
              <Tabs value={selectedCategory} className="mb-4">
                <TabsList>
                  {categories.map((category) => (
                    <TabsTrigger 
                      key={category} 
                      value={category}
                      onClick={() => setSelectedCategory(category)}
                    >
                      {category}
                    </TabsTrigger>
                  ))}
                </TabsList>
              </Tabs>
            )}

            {/* Product List */}
            {isLoadingProducts ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                {Array(6).fill(0).map((_, i) => (
                  <Skeleton key={i} className="h-56 w-full rounded-xl" />
                ))}
              </div>
            ) : filteredProducts.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                {filteredProducts.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
            ) : (
              <div className="text-center py-16">
                <h2 className="text-xl font-medium text-gray-900 mb-2">No products found</h2>
                <p className="text-gray-600 mb-6">This restaurant does not have any products in this category yet.</p>
              </div>
            )}
          </div>

          {/* Restaurant Information */}
          <div className="md:col-span-1">
            <div className="bg-gray-50 rounded-2xl p-6 sticky top-20">
              <h3 className="text-xl font-semibold text-gray-900 mb-4">
                Restaurant Information
              </h3>
              <div className="flex items-center text-gray-600 mb-2">
                <MapPin className="h-4 w-4 mr-2" />
                <span>{restaurant.address}</span>
              </div>
              <div className="flex items-center text-gray-600 mb-2">
                <Clock className="h-4 w-4 mr-2" />
                <span>{restaurant.openingHours}</span>
              </div>
              <div className="flex items-center text-gray-600 mb-4">
                <a href={`tel:${restaurant.phone}`} className="hover:text-food-600 transition-colors">
                  {restaurant.phone}
                </a>
              </div>
              <Button className="w-full bg-food-600 hover:bg-food-700 text-white">
                View on Map
                <ChevronRight className="h-4 w-4 ml-2" />
              </Button>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default RestaurantDetails;
