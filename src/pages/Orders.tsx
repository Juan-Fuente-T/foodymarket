import React, { useState } from 'react';
import { Layout } from '@/components/layout/Layout';
import { useAuth } from '@/hooks/use-auth';
import { useQuery } from '@tanstack/react-query';
import { orderAPI, restaurantAPI } from '@/services/api';
import { UserRole } from '@/types/models';
import { Order } from '@/types/models';
import OrderDetailsModal from '@/components/order/OrderDetailsModal';

const Orders = () => {
  const { user } = useAuth();
  const [selectedOrders, setSelectedOrders] = useState<Order[] | null>(null);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);

  const { data: allOrdersRestaurant = [], isLoading } = useQuery({
    queryKey: ['orders', user?.id],
    queryFn: async () => {
      if (!user?.id) return [];
      if (user.role === "RESTAURANTE") {
        const allOrdersRestaurant = await orderAPI.getAllByRestaurant(user.id);
        setSelectedOrders(allOrdersRestaurant);
        return allOrdersRestaurant;
      }else if (user.role === "CLIENTE") {
        const ordersClient = await orderAPI.getByClient(user.id);
        return ordersClient;
      } else {
        return [];
      }
    },
    enabled: !!user?.id,
  });
 
  return (
    <Layout>
      <div className="container mx-auto py-8">
        <h1 className="text-2xl font-bold mb-6">Your Orders</h1>

        {isLoading ? (
          <div>Loading orders...</div>
        ) : allOrdersRestaurant.length === 0 ? (
          <div className="text-center py-12">
            <p>RAYOS</p>
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
            {selectedOrders.map(order => (
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
