
import React from "react";
import { Link } from "react-router-dom";
import { Star, Clock, MapPin } from "lucide-react";
import { Restaurant, Category } from "@/types/models";
import { Badge } from "@/components/ui/badge";

interface RestaurantCardProps {
  restaurant: Restaurant;
}

export function RestaurantCard({ restaurant }: RestaurantCardProps) {
  // Handle empty categories array
  const categories = restaurant.categories || [];
  
  return (
    <Link to={`/restaurants/${restaurant.id}`} className="block">
      <div className="restaurant-card bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-500">
        <div className="restaurant-image-container h-48 relative">
          <img
            src={restaurant.coverImage || "https://via.placeholder.com/800x400?text=Restaurant"}
            alt={restaurant.name}
            className="restaurant-image w-full h-full object-cover"
          />
          <div className="absolute top-4 left-4 flex flex-wrap gap-2">
            {categories.slice(0, 3).map((category) => (
              <Badge key={category.id} variant="secondary" className="bg-white/90 backdrop-blur-sm text-food-700">
                {category.name}
              </Badge>
            ))}
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
