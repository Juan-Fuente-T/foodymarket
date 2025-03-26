import React from "react";
import { Layout } from "@/components/layout/Layout";
import { useQuery } from "@tanstack/react-query";
import { categoryAPI, restaurantAPI } from "@/services/api";
import { CategoryCard } from "@/components/restaurant/CategoryCard";
import { Skeleton } from "@/components/ui/skeleton";
import { useParams, Link } from "react-router-dom";
import { RestaurantCard } from "@/components/restaurant/RestaurantCard";
import { ChevronRight, ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";

const Categories = () => {
  const { id } = useParams<{ id: string }>();
  
  const { data: categories = [], isLoading: isLoadingCategories } = useQuery({
    queryKey: ["categories"],
    queryFn: () => categoryAPI.getAll(),
  });
  
  const { data: restaurants = [], isLoading: isLoadingRestaurants } = useQuery({
    queryKey: ["restaurantsByCategory", id],
    queryFn: () => restaurantAPI.getByCategory(id as string),
    enabled: !!id,
  });
  
  const { data: category, isLoading: isLoadingCategory } = useQuery({
    queryKey: ["category", id],
    queryFn: () => categoryAPI.getById(id as string),
    enabled: !!id,
  });
  
  // If we have a category ID, show restaurants for that category
  if (id) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Link to="/categories" className="inline-flex items-center text-food-600 hover:text-food-800 mb-6">
            <ArrowLeft className="h-4 w-4 mr-1" />
            Back to Categories
          </Link>
          
          {isLoadingCategory ? (
            <Skeleton className="h-12 w-1/3 mb-6" />
          ) : (
            <div className="mb-6">
              <h1 className="text-3xl font-bold text-gray-900">{category?.name} Restaurants</h1>
              {category?.description && (
                <p className="text-gray-600 mt-2">{category.description}</p>
              )}
            </div>
          )}
          
          {isLoadingRestaurants ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {Array(6).fill(0).map((_, i) => (
                <Skeleton key={i} className="h-64 w-full rounded-xl" />
              ))}
            </div>
          ) : restaurants.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {restaurants.map((restaurant) => (
                <RestaurantCard key={restaurant.id} restaurant={restaurant} />
              ))}
            </div>
          ) : (
            <div className="text-center py-16">
              <h2 className="text-xl font-medium text-gray-900 mb-2">No restaurants found</h2>
              <p className="text-gray-600 mb-6">There are no restaurants in this category yet.</p>
              <Button asChild className="bg-food-600 hover:bg-food-700">
                <Link to="/restaurants">Browse All Restaurants</Link>
              </Button>
            </div>
          )}
        </div>
      </Layout>
    );
  }
  
  // Otherwise, show all categories
  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Food Categories</h1>
        </div>
        
        {isLoadingCategories ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 md:gap-6">
            {Array(8).fill(0).map((_, i) => (
              <Skeleton key={i} className="aspect-square rounded-xl" />
            ))}
          </div>
        ) : categories.length > 0 ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 md:gap-6">
            {categories.map((category) => (
              <CategoryCard key={category.id} category={category} />
            ))}
          </div>
        ) : (
          <div className="text-center py-16">
            <h2 className="text-xl font-medium text-gray-900 mb-2">No categories found</h2>
            <p className="text-gray-600 mb-6">There are no food categories available yet.</p>
            <Button asChild className="bg-food-600 hover:bg-food-700">
              <Link to="/">Back to Home</Link>
            </Button>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default Categories;
