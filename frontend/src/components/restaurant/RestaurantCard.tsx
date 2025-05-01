
import React from "react";
import { Link } from "react-router-dom";
import { Star, Clock, MapPin } from "lucide-react";
import { Restaurant } from "@/types/models";
import { Badge } from "@/components/ui/badge";

interface RestaurantCardProps {
  restaurant: Restaurant;
}

export function RestaurantCard({ restaurant }: RestaurantCardProps) {
  if (!restaurant.id) {
    console.error("Restaurant ID is missing!", restaurant);
    return null;
  }
  
  // Use cuisineName if category is not available
  const categoryName = restaurant.category || restaurant.cuisineName || "Uncategorized";
  
  return (
    <Link to={`/restaurants/${restaurant.id}`} className="block">
      <div className="restaurant-card bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-500">
        <div className="restaurant-image-container relative rounded-md">
          <img
            src={restaurant.logo || "https://via.placeholder.com/800x400?text=Restaurant"}
            alt={restaurant.name}
            className="restaurant-image w-full h-full object-contain rounded-md"
          />
          <div className="absolute top-4 left-4 flex flex-wrap gap-2">
            {categoryName && (
              <Badge variant="secondary" className="bg-white/90 backdrop-blur-sm text-food-700">
                {categoryName}
              </Badge>
            )}
          </div>
        </div>
        <div className="p-4">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-lg font-semibold text-gray-900 line-clamp-1">
              {restaurant.name}
            </h3>
            <div className="flex items-center">
              <Star className="h-4 w-4 fill-yellow-400 stroke-yellow-400 mr-1" />
              <span className="text-sm font-medium">
                {restaurant.rating?.toFixed(1) || "New"}
              </span>
              <span className="text-xs text-gray-500 ml-1">
                ({restaurant.reviewCount || 0})
              </span>
            </div>
          </div>
          <p className="text-sm text-gray-500 line-clamp-2 mb-3">
            {restaurant.description || "No description available"}
          </p>
          <div className="flex items-center text-sm text-gray-500 space-x-4">
            <div className="flex items-center">
              <Clock className="h-4 w-4 mr-1 text-gray-400" />
              <span>{restaurant.openingHours || "Hours not available"}</span>
            </div>
            <div className="flex items-center">
              <MapPin className="h-4 w-4 mr-1 text-gray-400" />
              <span className="truncate">
                {restaurant.address 
                  ? restaurant.address.split(',')[0] 
                  : "Address not available"}
              </span>
            </div>
          </div>
        </div>
      </div>
    </Link>
  );
}
