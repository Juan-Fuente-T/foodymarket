
import React, { useState } from 'react';
import { Layout } from '@/components/layout/Layout';
import { useAuth } from '@/hooks/use-auth';
import { useQuery } from '@tanstack/react-query';
import { orderAPI } from '@/services/api';
import { Order } from '@/types/models';
import OrderDetailsModal from '@/components/order/OrderDetailsModal';

const Orders = () => {
  const { user } = useAuth();
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);

  const { data: orders = [], isLoading, refetch } = useQuery({
    queryKey: ['orders', user?.id],
    queryFn: async () => {
      if (!user?.id) return [];
      
      if (user.role === "RESTAURANTE") {
        const restaurants = await getRestaurantsForUser(user.id);
        if (restaurants.length > 0) {
          return orderAPI.getByRestaurant(restaurants[0].id.toString());
        }
        return [];
      }
      
      return orderAPI.getByClient(user.id);
    },
    enabled: !!user?.id,
  });

  const getRestaurantsForUser = async (userId: string) => {
    return [];
  };

  const handleViewDetails = (order: Order) => {
    setSelectedOrder(order);
    setIsDetailsModalOpen(true);
  };

  const handleStatusUpdate = () => {
    // Refetch orders after status update
    refetch();
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
            {orders.map(order => (
              <div key={order.id} className="border rounded-lg p-4 bg-white shadow-sm">
                <div className="flex justify-between items-center">
                  <div>
                    <p className="font-medium">Order #{order.id}</p>
                    <p className="text-sm text-gray-500">
                      {new Date(order.createdAt).toLocaleString()}
                    </p>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className={`px-3 py-1 rounded-full text-sm ${
                      order.status === 'entregado' ? 'bg-green-100 text-green-800' :
                      order.status === 'pagado' ? 'bg-blue-100 text-blue-800' :
                      order.status === 'pendiente' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {order.status.charAt(0).toUpperCase() + order.status.slice(1)}
                    </span>
                    <button
                      onClick={() => handleViewDetails(order)}
                      className="text-food-600 hover:text-food-700 font-medium"
                    >
                      View Details
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        <OrderDetailsModal
          order={selectedOrder}
          isOpen={isDetailsModalOpen}
          onClose={() => {
            setIsDetailsModalOpen(false);
            setSelectedOrder(null);
          }}
          onStatusChange={handleStatusUpdate}
        />
      </div>
    </Layout>
  );
};

export default Orders;
