
import React, { useState, useEffect, useMemo } from "react";
import { Layout } from "@/components/layout/Layout";
import { useQuery } from "@tanstack/react-query";
import { restaurantAPI } from "@/services/api";
import { RestaurantCard } from "@/components/restaurant/RestaurantCard";
import { Skeleton } from "@/components/ui/skeleton";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useSearchParams, Link } from "react-router-dom";
import { Search, Filter, Star } from "lucide-react";

const Restaurants = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [searchTerm, setSearchTerm] = useState(searchParams.get("search") || "");
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  
  const { data: restaurants = [], isLoading: isLoadingRestaurants } = useQuery({
    queryKey: ["restaurants"],
    queryFn: () => restaurantAPI.getAll(),
  });
  
  // Obtener categorías únicas de los restaurantes
  const categories = React.useMemo(() => {
    if (!restaurants.length) return [];
    
    const uniqueCategories = new Set<string>();
    restaurants.forEach(restaurant => {
      if (restaurant.category) {
        uniqueCategories.add(restaurant.category);
      }
    });
    
    return Array.from(uniqueCategories);
  }, [restaurants]);
  
  // Filter restaurants based on search term and category
  const filteredRestaurants = restaurants.filter(restaurant => {
    const matchesSearch = searchTerm 
      ? restaurant.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        restaurant.description.toLowerCase().includes(searchTerm.toLowerCase())
      : true;
      
    const matchesCategory = selectedCategory
      ? restaurant.category === selectedCategory
      : true;
      
    return matchesSearch && matchesCategory;
  });
  
  // Update search params when search term changes
  useEffect(() => {
    if (searchTerm) {
      searchParams.set("search", searchTerm);
    } else {
      searchParams.delete("search");
    }
    setSearchParams(searchParams);
  }, [searchTerm]);
  
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // Search is already updated via the input onChange
  };
  
  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Restaurants</h1>
        
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          <div className="md:col-span-1">
            <div className="bg-white rounded-lg shadow p-4 sticky top-20">
              <h2 className="font-medium text-lg mb-4">Filters</h2>
              
              <div className="mb-6">
                <h3 className="text-sm font-medium text-gray-700 mb-2">Categories</h3>
                <div className="space-y-2">
                  <Button
                    onClick={() => setSelectedCategory(null)}
                    variant={selectedCategory === null ? "default" : "outline"}
                    size="sm"
                    className={`w-full justify-start ${selectedCategory === null ? "bg-food-600" : ""}`}
                  >
                    All Restaurants
                  </Button>
                  
                  {isLoadingRestaurants ? (
                    Array(5).fill(0).map((_, i) => (
                      <Skeleton key={i} className="h-8 w-full" />
                    ))
                  ) : (
                    categories.map(category => (
                      <Button
                        key={category}
                        onClick={() => setSelectedCategory(category)}
                        variant={selectedCategory === category ? "default" : "outline"}
                        size="sm"
                        className={`w-full justify-start ${selectedCategory === category ? "bg-food-600" : ""}`}
                      >
                        {category}
                      </Button>
                    ))
                  )}
                </div>
              </div>
            </div>
          </div>
          
          <div className="md:col-span-3">
            <form onSubmit={handleSearch} className="mb-6">
              <Input
                type="text"
                placeholder="Search restaurants..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full"
              />
            </form>
            
            {isLoadingRestaurants ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {Array(6).fill(0).map((_, i) => (
                  <Skeleton key={i} className="h-72 rounded-xl" />
                ))}
              </div>
            ) : filteredRestaurants.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredRestaurants.map(restaurant => (
                  <RestaurantCard key={restaurant.id} restaurant={restaurant} />
                ))}
              </div>
            ) : (
              <div className="text-center py-10">
                <p className="text-gray-500">No restaurants found</p>
                <Button 
                  onClick={() => {
                    setSearchTerm("");
                    setSelectedCategory(null);
                  }} 
                  variant="link"
                  className="mt-2"
                >
                  Clear filters
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default Restaurants; 