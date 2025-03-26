
import React, { useState, useEffect } from "react";
import { Layout } from "@/components/layout/Layout";
import { useParams, Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { restaurantAPI, productAPI, reviewAPI } from "@/services/api";
import { ProductCard } from "@/components/restaurant/ProductCard";
import { Button } from "@/components/ui/button";
import { useCart } from "@/contexts/CartContext";
import { Star, MapPin, Phone, Mail, Clock, ChevronRight } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "@/lib/toast";

const RestaurantDetails = () => {
  const { id } = useParams<{ id: string }>();
  const { setRestaurant } = useCart();
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  
  const { data: restaurant, isLoading: isLoadingRestaurant } = useQuery({
    queryKey: ["restaurant", id],
    queryFn: () => restaurantAPI.getById(id as string),
    enabled: !!id,
  });
  
  // Use useEffect to set restaurant in cart context after data is loaded
  useEffect(() => {
    if (restaurant) {
      setRestaurant(restaurant);
      if (restaurant.categories && restaurant.categories.length > 0) {
        setSelectedCategory(restaurant.categories[0].id);
      }
    }
  }, [restaurant, setRestaurant]);
  
  const { data: products = [], isLoading: isLoadingProducts } = useQuery({
    queryKey: ["products", id, selectedCategory],
    queryFn: () => selectedCategory 
      ? productAPI.getByRestaurantAndCategory(id as string, selectedCategory)
      : productAPI.getByRestaurant(id as string),
    enabled: !!id,
  });
  
  const { data: reviews = [], isLoading: isLoadingReviews } = useQuery({
    queryKey: ["reviews", id],
    queryFn: () => reviewAPI.getByRestaurant(id as string),
    enabled: !!id,
  });
  
  if (isLoadingRestaurant) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="animate-pulse">
            <Skeleton className="h-64 w-full rounded-xl mb-8" />
            <Skeleton className="h-10 w-1/3 mb-4" />
            <Skeleton className="h-4 w-2/3 mb-2" />
            <Skeleton className="h-4 w-1/2 mb-8" />
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {[1, 2, 3, 4, 5, 6].map((i) => (
                <Skeleton key={i} className="h-60 w-full rounded-xl" />
              ))}
            </div>
          </div>
        </div>
      </Layout>
    );
  }
  
  if (!restaurant && !isLoadingRestaurant) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Restaurant not found</h2>
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
      {restaurant && (
        <>
          <div 
            className="relative h-64 md:h-80 bg-cover bg-center"
            style={{ backgroundImage: `url(${restaurant.coverImage})` }}
          >
            <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/30 to-transparent"></div>
            <div className="absolute bottom-0 left-0 right-0 p-6 text-white">
              <div className="flex items-center mb-2">
                {restaurant.categories.map((category) => (
                  <Badge key={category.id} variant="secondary" className="mr-2 bg-white/20 backdrop-blur-sm">
                    {category.name}
                  </Badge>
                ))}
              </div>
              <h1 className="text-3xl md:text-4xl font-bold mb-2">{restaurant.name}</h1>
              <p className="text-sm md:text-base text-white/90 max-w-3xl">{restaurant.description}</p>
            </div>
          </div>
          
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="flex flex-wrap justify-between mb-8">
              <div className="flex items-center mb-4 mr-6">
                <div className="flex items-center bg-yellow-50 text-yellow-700 rounded-full px-3 py-1">
                  <Star className="h-4 w-4 fill-yellow-500 stroke-yellow-500 mr-1" />
                  <span className="font-medium">{restaurant.rating.toFixed(1)}</span>
                  <span className="text-sm text-gray-500 ml-1">({restaurant.reviewCount})</span>
                </div>
              </div>
              
              <div className="flex flex-wrap">
                <div className="flex items-center mr-6 mb-4">
                  <Clock className="h-5 w-5 text-food-600 mr-2" />
                  <span>{restaurant.openingHours}</span>
                </div>
                <div className="flex items-center mr-6 mb-4">
                  <MapPin className="h-5 w-5 text-food-600 mr-2" />
                  <span>{restaurant.address}</span>
                </div>
                <div className="flex items-center mr-6 mb-4">
                  <Phone className="h-5 w-5 text-food-600 mr-2" />
                  <span>{restaurant.phone}</span>
                </div>
                <div className="flex items-center mb-4">
                  <Mail className="h-5 w-5 text-food-600 mr-2" />
                  <span>{restaurant.email}</span>
                </div>
              </div>
            </div>
            
            <Tabs defaultValue="menu" className="w-full">
              <TabsList className="mb-6">
                <TabsTrigger value="menu">Menu</TabsTrigger>
                <TabsTrigger value="reviews">Reviews ({reviews.length})</TabsTrigger>
                <TabsTrigger value="info">Information</TabsTrigger>
              </TabsList>
              
              <TabsContent value="menu">
                <div className="mb-6">
                  <h2 className="text-xl font-bold mb-4">Categories</h2>
                  <div className="flex overflow-x-auto pb-2 space-x-2">
                    <Button
                      onClick={() => setSelectedCategory(null)}
                      variant={selectedCategory === null ? "default" : "outline"}
                      className={selectedCategory === null ? "bg-food-600" : ""}
                    >
                      All
                    </Button>
                    {restaurant.categories.map((category) => (
                      <Button
                        key={category.id}
                        onClick={() => setSelectedCategory(category.id)}
                        variant={selectedCategory === category.id ? "default" : "outline"}
                        className={selectedCategory === category.id ? "bg-food-600" : ""}
                      >
                        {category.name}
                      </Button>
                    ))}
                  </div>
                </div>
                
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                  {isLoadingProducts ? (
                    Array(6).fill(0).map((_, i) => (
                      <Skeleton key={i} className="h-60 w-full rounded-xl" />
                    ))
                  ) : products.length > 0 ? (
                    products.map((product) => (
                      <ProductCard key={product.id} product={product} />
                    ))
                  ) : (
                    <div className="col-span-3 py-12 text-center">
                      <h3 className="text-lg font-medium text-gray-900 mb-2">No products found</h3>
                      <p className="text-gray-600">
                        {selectedCategory 
                          ? "No products in this category." 
                          : "This restaurant hasn't added any products yet."}
                      </p>
                    </div>
                  )}
                </div>
              </TabsContent>
              
              <TabsContent value="reviews">
                <div className="space-y-6">
                  <div className="flex justify-between items-center">
                    <h2 className="text-xl font-bold">Customer Reviews</h2>
                    <Button className="bg-food-600 hover:bg-food-700">Write a Review</Button>
                  </div>
                  
                  {isLoadingReviews ? (
                    Array(3).fill(0).map((_, i) => (
                      <div key={i} className="border rounded-lg p-4 animate-pulse">
                        <Skeleton className="h-6 w-1/4 mb-2" />
                        <Skeleton className="h-4 w-full mb-2" />
                        <Skeleton className="h-4 w-3/4" />
                      </div>
                    ))
                  ) : reviews.length > 0 ? (
                    reviews.map((review) => (
                      <div key={review.id} className="border rounded-lg p-4">
                        <div className="flex justify-between mb-2">
                          <div className="flex items-center">
                            <div className="font-medium">{review.user.name}</div>
                          </div>
                          <div className="flex items-center">
                            <div className="flex">
                              {Array(5).fill(0).map((_, i) => (
                                <Star 
                                  key={i} 
                                  className={`h-4 w-4 ${i < review.rating ? "fill-yellow-400 stroke-yellow-400" : "stroke-gray-300"}`} 
                                />
                              ))}
                            </div>
                            <span className="text-sm text-gray-500 ml-2">
                              {new Date(review.createdAt).toLocaleDateString()}
                            </span>
                          </div>
                        </div>
                        <p className="text-gray-700">{review.comment}</p>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-8">
                      <p className="text-gray-600 mb-4">No reviews yet. Be the first to review!</p>
                      <Button className="bg-food-600 hover:bg-food-700">Write a Review</Button>
                    </div>
                  )}
                </div>
              </TabsContent>
              
              <TabsContent value="info">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  <div>
                    <h2 className="text-xl font-bold mb-4">About</h2>
                    <p className="text-gray-700 mb-6">{restaurant.description}</p>
                    
                    <h3 className="font-medium text-lg mb-2">Contact</h3>
                    <ul className="space-y-2 mb-6">
                      <li className="flex items-center">
                        <MapPin className="h-5 w-5 text-food-600 mr-2" />
                        <span>{restaurant.address}</span>
                      </li>
                      <li className="flex items-center">
                        <Phone className="h-5 w-5 text-food-600 mr-2" />
                        <span>{restaurant.phone}</span>
                      </li>
                      <li className="flex items-center">
                        <Mail className="h-5 w-5 text-food-600 mr-2" />
                        <span>{restaurant.email}</span>
                      </li>
                    </ul>
                    
                    <h3 className="font-medium text-lg mb-2">Hours</h3>
                    <p className="text-gray-700">{restaurant.openingHours}</p>
                  </div>
                  
                  <div>
                    <h2 className="text-xl font-bold mb-4">Location</h2>
                    <div className="bg-gray-200 h-64 rounded-lg flex items-center justify-center">
                      <p className="text-gray-500">Map placeholder</p>
                    </div>
                  </div>
                </div>
              </TabsContent>
            </Tabs>
          </div>
        </>
      )}
    </Layout>
  );
};

export default RestaurantDetails;
