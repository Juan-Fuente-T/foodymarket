import React from 'react';
import { Layout } from '@/components/layout/Layout';
import { useAuth } from '@/hooks/use-auth';
import { useQuery } from '@tanstack/react-query';
import { orderAPI } from '@/services/api';
import { UserRole } from '@/types/models';

const Orders = () => {
  const { user } = useAuth();

  // Fetch orders based on user role
  const { data: orders = [], isLoading } = useQuery({
    queryKey: ['orders', user?.id],
    queryFn: async () => {
      if (!user?.id) return [];
      
      // If user is a restaurant owner, get restaurant orders
      if (user.role === "RESTAURANTE") {
        const restaurants = await getRestaurantsForUser(user.id);
        if (restaurants.length > 0) {
          return orderAPI.getByRestaurant(restaurants[0].id.toString());
        }
        return [];
      }
      
      // Otherwise get customer orders
      return orderAPI.getByClient(user.id);
    },
    enabled: !!user?.id,
  });

  // Fetch restaurants owned by user
  const getRestaurantsForUser = async (userId: string) => {
    // Implementation would depend on your restaurant API
    // This is a placeholder
    return []; 
  };

  return (
    <Layout>
      <div className="container mx-auto py-8">
        <h1 className="text-2xl font-bold mb-6">Your Orders</h1>
        
        {isLoading ? (
          <div>Loading orders...</div>
        ) : orders.length === 0 ? (
          <div className="text-center py-12">
            <h2 className="text-xl font-medium text-gray-600">You don't have any orders yet</h2>
            <p className="mt-2 text-gray-500">
              {user?.role === "RESTAURANTE" 
                ? "When customers place orders with your restaurant, they'll appear here."
                : "When you place an order, it will appear here."}
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {/* Order list would be rendered here */}
            {orders.map(order => (
              <div key={order.id} className="border rounded-lg p-4">
                Order #{order.id}
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
};

export default Orders;
