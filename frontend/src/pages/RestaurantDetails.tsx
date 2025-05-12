
import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Layout } from '@/components/layout/Layout';
import { useQuery } from '@tanstack/react-query';
import { restaurantAPI, productAPI } from '@/services/api';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Star, Clock, DollarSign, Utensils, MapPin, Phone, Mail, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Product } from '@/types/models';
import { ProductCard } from '@/components/product/ProductCard';
import { ProductDetailModal } from '@/components/product/ProductDetailModal';

const RestaurantDetails = () => {
  const { id } = useParams<{ id: string }>();
  const [selectedTab, setSelectedTab] = useState('menu');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  
  const { data: restaurant, isLoading: isLoadingRestaurant } = useQuery({
    queryKey: ['restaurant', id],
    queryFn: () => restaurantAPI.getById(id!),
    enabled: !!id,
  });
  
  const { data: categoriesWithProducts = [], isLoading: isLoadingProducts } = useQuery({
    queryKey: ['restaurantProducts', id],
    queryFn: () => productAPI.getByRestaurantAndCategory(id!),
    enabled: !!id,
  });

  const handleOpenProductModal = (product: Product) => {
    setSelectedProduct(product);
  };

  const handleCloseProductModal = () => {
    setSelectedProduct(null);
  };

  // Get the category name (either from category or cuisineName)
  const getCategoryName = (restaurant) => {
    return restaurant?.category || restaurant?.cuisineName || 'Uncategorized';
  };

  return (
    <Layout>
      <div className="container mx-auto py-8 px-4">
        {isLoadingRestaurant ? (
          <>
            <Skeleton className="h-80 w-full rounded-xl mb-6" />
            <div className="flex flex-col md:flex-row md:justify-between md:items-end gap-4 mb-6">
              <div>
                <Skeleton className="h-10 w-64 mb-2" />
                <div className="flex gap-2">
                  <Skeleton className="h-6 w-24" />
                  <Skeleton className="h-6 w-24" />
                </div>
              </div>
              <Skeleton className="h-10 w-36" />
            </div>
          </>
        ) : restaurant ? (
          <>
            <div className="relative h-80 w-full rounded-xl overflow-hidden mb-6">
              <img
                src={restaurant.coverImage || 'https://via.placeholder.com/1200x400?text=Restaurant+Cover'}
                alt={restaurant.name}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
              
              <div className="absolute bottom-0 left-0 right-0 p-6 text-white">
                <div className="flex items-start gap-4">
                  <div className="h-24 w-24 bg-white rounded-lg overflow-hidden shrink-0 border-4 border-white">
                    <img
                      src={restaurant.logo || 'https://via.placeholder.com/100?text=Logo'}
                      alt={`${restaurant.name} logo`}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  
                  <div>
                    <h1 className="text-3xl font-bold mb-2">{restaurant.name}</h1>
                    <div className="flex flex-wrap gap-3 text-sm">
                      <span className="flex items-center gap-1">
                        <Utensils className="h-4 w-4" />
                        {getCategoryName(restaurant)}
                      </span>
                      {restaurant.rating && (
                        <span className="flex items-center gap-1">
                          <Star className="h-4 w-4 text-yellow-400" />
                          {restaurant.rating.toFixed(1)} ({restaurant.reviewCount || 0} reviews)
                        </span>
                      )}
                      {restaurant.deliveryFee !== undefined && (
                        <span className="flex items-center gap-1">
                          <DollarSign className="h-4 w-4" />
                          Delivery: ${restaurant.deliveryFee.toFixed(2)}
                        </span>
                      )}
                      {restaurant.minOrderAmount !== undefined && restaurant.minOrderAmount > 0 && (
                        <span className="flex items-center gap-1">
                          <AlertCircle className="h-4 w-4" />
                          Min order: ${restaurant.minOrderAmount.toFixed(2)}
                        </span>
                      )}
                      {restaurant.openingHours && (
                        <span className="flex items-center gap-1">
                          <Clock className="h-4 w-4" />
                          {restaurant.openingHours}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="flex flex-col md:flex-row md:justify-between md:items-start gap-4 mb-6">
              <p className="text-gray-600 max-w-3xl">{restaurant.description}</p>
              <div className="flex gap-2">
                {/* <Button variant="outline" asChild>
                  <a href={`tel:${restaurant.phone}`}>
                    <Phone className="h-4 w-4 mr-2" />
                    Call
                  </a>
                </Button> */}
                <Button asChild className="bg-food-600 hover:bg-food-700">
                  <Link to="/cart">View Cart</Link>
                </Button>
              </div>
            </div>
          </>
        ) : (
          <div className="text-center py-12">
            <h2 className="text-2xl font-bold text-gray-800 mb-2">Restaurant not found</h2>
            <p className="text-gray-600 mb-6">The restaurant you're looking for doesn't exist or has been removed.</p>
            <Button asChild>
              <Link to="/restaurants">Browse Restaurants</Link>
            </Button>
          </div>
        )}

        {restaurant && (
          <Tabs value={selectedTab} onValueChange={setSelectedTab} className="mt-8">
            <TabsList className="mb-6">
              <TabsTrigger value="menu">Menu</TabsTrigger>
              <TabsTrigger value="info">Information</TabsTrigger>
              <TabsTrigger value="reviews">Reviews</TabsTrigger>
            </TabsList>
            
            <TabsContent value="menu">
              {isLoadingProducts ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {Array(6).fill(0).map((_, i) => (
                    <Skeleton key={i} className="h-64 w-full rounded-xl" />
                  ))}
                </div>
              ) : categoriesWithProducts.length > 0 ? (
                <div className="space-y-8">
                  {categoriesWithProducts.map((category) => (
                    <div key={category.categoryId} className="space-y-4">
                      <h2 className="text-xl font-bold flex items-center">
                        <span>{category.categoryName}</span>
                        <Badge variant="outline" className="ml-2">
                          {category.products.length} items
                        </Badge>
                      </h2>
                      
                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {category.products.map((product) => {
                          // Create a well-formatted product object for ProductCard
                          const formattedProduct: Product = {
                            id: product.prd_id?.toString() || product.id?.toString() || '',
                            name: product.name || '',
                            description: product.description || '',
                            price: product.price || '0',
                            image: product.image || '',
                            isActive: product.isActive === true,
                            available: product.isActive === true,
                            quantity: Number(product.quantity) || 0,
                            restaurantId: product.restaurantId?.toString() || category.restaurantId?.toString() || '',
                            categoryId: product.categoryId?.toString() || category.categoryId?.toString() || '',
                            createdAt: product.createdAt || new Date().toISOString(),
                            updatedAt: product.updatedAt || new Date().toISOString(),
                            categoryName: product.categoryName || category.categoryName || '',
                          };
                          
                          return (
                            <ProductCard 
                              key={formattedProduct.id} 
                              product={formattedProduct}
                              onOpenModal={handleOpenProductModal}
                            />
                          );
                        })}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-12 bg-gray-50 rounded-lg">
                  <h3 className="text-xl font-medium text-gray-700 mb-2">No menu items available</h3>
                  <p className="text-gray-500">This restaurant hasn't added any menu items yet.</p>
                </div>
              )}
            </TabsContent>
            
            <TabsContent value="info">
              <Card>
                <CardContent className="pt-6">
                  <div className="space-y-4">
                    <div>
                      <h3 className="text-lg font-medium mb-2">Contact Information</h3>
                      <div className="space-y-2">
                        <p className="flex items-center gap-2 text-gray-600">
                          <Phone className="h-4 w-4" />
                          <a href={`tel:${restaurant.phone}`} className="hover:underline">{restaurant.phone}</a>
                        </p>
                        {restaurant.email && (
                          <p className="flex items-center gap-2 text-gray-600">
                            <Mail className="h-4 w-4" />
                            <a href={`mailto:${restaurant.email}`} className="hover:underline">{restaurant.email}</a>
                          </p>
                        )}
                      </div>
                    </div>
                    
                    <div>
                      <h3 className="text-lg font-medium mb-2">Location</h3>
                      <p className="flex items-start gap-2 text-gray-600">
                        <MapPin className="h-4 w-4 mt-1 shrink-0" />
                        <span>{restaurant.address}</span>
                      </p>
                      {/* Google Map could be added here */}
                    </div>
                    
                    <div>
                      <h3 className="text-lg font-medium mb-2">Hours of Operation</h3>
                      <p className="text-gray-600">
                        {restaurant.openingHours || "Hours not specified"}
                      </p>
                    </div>
                    
                    <div>
                      <h3 className="text-lg font-medium mb-2">Delivery Information</h3>
                      <ul className="space-y-1 text-gray-600">
                        <li>Delivery Fee: ${restaurant.deliveryFee?.toFixed(2) || "0.00"}</li>
                        <li>Minimum Order: ${restaurant.minOrderAmount?.toFixed(2) || "0.00"}</li>
                      </ul>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="reviews">
              <div className="text-center py-12 bg-gray-50 rounded-lg">
                <h3 className="text-xl font-medium text-gray-700 mb-2">No reviews yet</h3>
                <p className="text-gray-500 mb-4">Be the first to review this restaurant!</p>
                <Button>Write a Review</Button>
              </div>
            </TabsContent>
          </Tabs>
        )}
        
        {selectedProduct && (
          <ProductDetailModal
            product={selectedProduct}
            onClose={handleCloseProductModal}
          />
        )}
      </div>
    </Layout>
  );
};

export default RestaurantDetails;
