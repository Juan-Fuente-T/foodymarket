
import React, { useState, useEffect } from "react";
import { Layout } from "@/components/layout/Layout";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { RestaurantCard } from "@/components/restaurant/RestaurantCard";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { useQuery } from "@tanstack/react-query";
import { restaurantAPI } from "@/services/api";
import { Restaurant } from "@/types/models";
import { Skeleton } from "@/components/ui/skeleton";

const Index = () => {
  const [searchTerm, setSearchTerm] = useState("");

  const { data: restaurants = [], isLoading: isLoadingRestaurants, error: restaurantsError } = useQuery({
    queryKey: ["restaurants"],
    queryFn: () => restaurantAPI.getAll(),
  });
  
  // Para debugging - puede ser eliminado en producción
  useEffect(() => {
    if (restaurantsError) {
      console.error("Error fetching restaurants:", restaurantsError);
    }
  }, [restaurantsError]);

  // Agrupar restaurantes por categoría (ahora es solo un string)
  const restaurantsByCategory = React.useMemo(() => {
    if (!restaurants.length) return {};
    
    return restaurants.reduce((acc, restaurant) => {
      const categoryName = restaurant.category || 'Uncategorized';
      if (!acc[categoryName]) {
        acc[categoryName] = [];
      }
      acc[categoryName].push(restaurant);
      return acc;
    }, {} as Record<string, Restaurant[]>);
  }, [restaurants]);

  // Obtener categorías únicas
  const categories = Object.keys(restaurantsByCategory);

  return (
    <Layout>
      {/* Hero Section */}
      <section className="hero-section">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 md:py-24">
          <div className="max-w-3xl mx-auto text-center">
            <h1 className="animate-fade-in text-4xl md:text-5xl lg:text-6xl font-bold text-gray-900 mb-6">
              Delicious Food, 
              <span className="text-food-600"> Delivered Fast</span>
            </h1>
            <p className="animate-fade-in animation-delay-100 text-xl text-gray-600 mb-8">
              Order from your favorite local restaurants with free delivery on your first order.
            </p>
            
            <div className="animate-fade-in animation-delay-200 flex flex-col sm:flex-row justify-center space-y-4 sm:space-y-0 sm:space-x-4 mb-8">
              <div className="relative w-full sm:w-auto sm:flex-grow max-w-xl">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <Input
                  type="text"
                  placeholder="Search for restaurants or food..."
                  className="pl-10 pr-4 py-6 w-full rounded-full shadow-sm border border-gray-200 focus:border-food-500 focus:ring-food-500"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <Button 
                asChild
                size="lg" 
                className="bg-food-600 hover:bg-food-700 text-white rounded-full py-6 button-hover"
              >
                <Link to={`/restaurants${searchTerm ? `?search=${searchTerm}` : ''}`}>
                  Find Food
                </Link>
              </Button>
            </div>
          </div>
        </div>
      </section>

      {/* Categories Section */}
      <section className="py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900">
              Browse By Category
            </h2>
            <Button asChild variant="ghost" className="text-food-600 hover:text-food-700">
              <Link to="/restaurants">View All</Link>
            </Button>
          </div>
          
          {isLoadingRestaurants ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 md:gap-6">
              {Array(4).fill(null).map((_, i) => (
                <Skeleton key={i} className="aspect-square rounded-xl" />
              ))}
            </div>
          ) : restaurantsError ? (
            <div className="text-center py-10">
              <p className="text-red-500">Failed to load categories</p>
              <Button onClick={() => window.location.reload()} className="mt-4">Retry</Button>
            </div>
          ) : categories.length > 0 ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 md:gap-6">
              {categories.slice(0, 4).map((category, index) => {
                const firstRestaurantWithCategory = restaurantsByCategory[category][0];
                return (
                  <Link 
                    key={index}
                    to={`/restaurants?category=${category}`}
                    className="group relative overflow-hidden rounded-xl bg-white shadow-sm transition-all duration-300 hover:shadow-lg"
                  >
                    <div className="aspect-square overflow-hidden">
                      {/* Usar una imagen del primer restaurante con esta categoría */}
                      <img 
                        src={firstRestaurantWithCategory?.coverImage || "https://via.placeholder.com/300?text=Category"}
                        alt={category}
                        className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/30 to-transparent"></div>
                    </div>
                    <div className="absolute bottom-0 left-0 right-0 p-4">
                      <h3 className="text-xl font-bold text-white drop-shadow-md">
                        {category}
                      </h3>
                      <p className="mt-1 text-sm text-white/90 line-clamp-2">
                        {restaurantsByCategory[category].length} restaurants
                      </p>
                    </div>
                  </Link>
                );
              })}
            </div>
          ) : (
            <div className="text-center py-8">
              <p className="text-gray-500">No categories available</p>
            </div>
          )}
        </div>
      </section>

      {/* Featured Restaurants Section */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900">
              Popular Restaurants
            </h2>
            <Button asChild variant="ghost" className="text-food-600 hover:text-food-700">
              <Link to="/restaurants">View All</Link>
            </Button>
          </div>

          {isLoadingRestaurants ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {Array(3).fill(null).map((_, i) => (
                <Skeleton key={i} className="h-72 rounded-xl" />
              ))}
            </div>
          ) : restaurantsError ? (
            <div className="text-center py-10">
              <p className="text-red-500">Failed to load restaurants</p>
              <Button onClick={() => window.location.reload()} className="mt-4">Retry</Button>
            </div>
          ) : restaurants.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {restaurants.slice(0, 3).map((restaurant) => (
                <RestaurantCard key={restaurant.id} restaurant={restaurant} />
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <p className="text-gray-500">No restaurants available</p>
            </div>
          )}
        </div>
      </section>

      {/* How It Works Section */}
      <section className="py-16 bg-gradient-to-b from-white to-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900 mb-4">
              How It Works
            </h2>
            <p className="text-lg text-gray-600 max-w-3xl mx-auto">
              Order your favorite food in just a few easy steps
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Step 1 */}
            <div className="flex flex-col items-center text-center p-6 hover:transform hover:scale-105 transition-transform duration-300">
              <div className="w-16 h-16 bg-food-100 rounded-full flex items-center justify-center mb-4">
                <span className="text-food-600 text-2xl font-bold">1</span>
              </div>
              <h3 className="text-xl font-semibold mb-2">Browse Restaurants</h3>
              <p className="text-gray-600">
                Find your favorite restaurants or discover new ones nearby
              </p>
            </div>

            {/* Step 2 */}
            <div className="flex flex-col items-center text-center p-6 hover:transform hover:scale-105 transition-transform duration-300">
              <div className="w-16 h-16 bg-food-100 rounded-full flex items-center justify-center mb-4">
                <span className="text-food-600 text-2xl font-bold">2</span>
              </div>
              <h3 className="text-xl font-semibold mb-2">Choose Your Food</h3>
              <p className="text-gray-600">
                Select from a wide variety of delicious meals and add to cart
              </p>
            </div>

            {/* Step 3 */}
            <div className="flex flex-col items-center text-center p-6 hover:transform hover:scale-105 transition-transform duration-300">
              <div className="w-16 h-16 bg-food-100 rounded-full flex items-center justify-center mb-4">
                <span className="text-food-600 text-2xl font-bold">3</span>
              </div>
              <h3 className="text-xl font-semibold mb-2">Enjoy Your Meal</h3>
              <p className="text-gray-600">
                Your food will be prepared and delivered right to your door
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-food-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
            <div className="grid grid-cols-1 md:grid-cols-2">
              <div className="p-8 md:p-12 flex flex-col justify-center">
                <h2 className="text-2xl md:text-3xl font-bold text-gray-900 mb-4">
                  Become a Restaurant Partner
                </h2>
                <p className="text-lg text-gray-600 mb-6">
                  Reach more customers, grow your business and let us handle the delivery. Join our network of restaurant partners today.
                </p>
                <div>
                  <Button 
                    asChild
                    size="lg"
                    className="bg-food-600 hover:bg-food-700 text-white button-hover"
                  >
                    <Link to="/partner">
                      Join Now
                    </Link>
                  </Button>
                </div>
              </div>
              <div className="relative h-64 md:h-auto">
                <img
                  src="https://images.unsplash.com/photo-1484972759836-b93f9b778036?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80"
                  alt="Restaurant kitchen"
                  className="absolute inset-0 w-full h-full object-cover"
                />
              </div>
            </div>
          </div>
        </div>
      </section>
    </Layout>
  );
};

export default Index;
