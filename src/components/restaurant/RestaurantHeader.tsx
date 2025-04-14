
import React from 'react';
import { Star, MapPin, Clock, Phone } from 'lucide-react'; // o el icon library que uses
import { Badge } from '@/components/ui/badge'; // Ajusta la ruta según tu estructura
import { Button } from '@/components/ui/button'; // Ajusta la ruta
import { Restaurant } from '@/types/models'; // Ajusta la ruta de tus tipos

const RestaurantHeader = ({ restaurant }: { restaurant: Restaurant }) => (
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
      {/* Rating inline */}
      <div className="flex items-center text-white mt-2">
        <Star className="h-5 w-5 mr-1 fill-yellow-400 stroke-yellow-400" />
        <span>{restaurant.rating?.toFixed(1) || 'N/A'}</span>
      </div>
    </div>
  </div>
);

const RestaurantInfoSidebar = ({ restaurant }: { restaurant: Restaurant }) => (
  <div className="bg-gray-50 rounded-2xl p-6 sticky top-20">
    <h3 className="text-xl font-semibold text-gray-900 mb-4">
      Restaurant Information
    </h3>
    <div className="space-y-4">
      {/* Info items inline */}
      <div className="flex items-center text-gray-600">
        <MapPin className="h-4 w-4 mr-2" />
        <span>{restaurant.address || 'Address not available'}</span>
      </div>
      <div className="flex items-center text-gray-600">
        <Clock className="h-4 w-4 mr-2" />
        <span>{restaurant.openingHours || 'Opening hours not available'}</span>
      </div>
      <div className="flex items-center text-gray-600">
        <Phone className="h-4 w-4 mr-2" />
        <span>{restaurant.phone || 'Phone not available'}</span>
      </div>
    </div>
    <Button className="w-full mt-4 bg-food-600 hover:bg-food-700">
      View on Map
    </Button>
  </div>
);

export { RestaurantHeader, RestaurantInfoSidebar };
