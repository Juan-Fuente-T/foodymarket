import React, { useState } from 'react';
import { Layout } from '@/components/layout/Layout';
import { useAuth } from '@/hooks/use-auth';
import { useQuery } from '@tanstack/react-query';
import { orderAPI, restaurantAPI } from '@/services/api';
import { UserRole } from '@/types/models';
import { Order } from '@/types/models';
import OrderDetailsModal from '@/components/order/OrderDetailsModal';
import { Button } from '@/components/ui/button';

const Orders = () => {
  const { user } = useAuth();
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);

  const { data: allOrdersRestaurant = [], isLoading } = useQuery({
    queryKey: ['orders', user?.id],
    queryFn: async () => {
      if (!user?.id) return [];
      if (user.role === "RESTAURANTE") {
        const allOrdersRestaurant = await orderAPI.getAllByRestaurant(user.id);
        return allOrdersRestaurant;
      } else if (user.role === "CLIENTE") {
        const ordersClient = await orderAPI.getByClient(user.id);
        return ordersClient;
      } else {
        return [];
      }
    },
    enabled: !!user?.id,
  });

  const handleCloseModal = () => {
    setIsDetailsModalOpen(false);
    setSelectedOrder(null);
  };

  return (
    <Layout>
      <div className="container mx-auto py-8">
        <h1 className="text-2xl font-bold mb-6">Your Orders</h1>

        {isLoading ? (
          <div>Loading orders...</div>
        ) : allOrdersRestaurant.length === 0 ? (
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
            {allOrdersRestaurant?.map((order: Order) => (
              <div key={order.id} className="border rounded-lg p-4 flex flex-col sm:flex-row justify-between items-start sm:items-center space-y-2 sm:space-y-0">
                {/* --- Lado Izquierdo: Info Básica del Pedido (SIN CAMBIOS) --- */}
                <div className="flex justify-between items-start space-y-1 sm:space-y-0 sm:space-x-4 flex-grow sm:flex-grow-0">
                  <p className="text-md font-medium text-gray-900">Order #{order.id?.slice(0, 8)}</p>
                  <p className="text-md font-medium text-gray-900">Restaurant {order.restaurantName?.slice(0, 25)}</p>
                  <p className="text-md text-gray-600">Date: {order.createdAt ? new Date(order.createdAt).toLocaleDateString('es-ES') : 'N/A'}</p>
                  <p className="text-md text-gray-600">Status: <span className={` font-semibold font-medium ml-1 ${order.status === 'pendiente' ? 'text-blue-600' :
                      order.status === 'entregado' ? 'text-green-600' :
                        order.status === 'pagado' ? 'text-yellow-600' :
                          order.status === 'cancelado' ? 'text-red-600' : ''
                    }`}>{order.status ? order.status.charAt(0).toUpperCase() + order.status.slice(1) : 'N/A'}</span></p>
                </div>

                {/* --- Lado Derecho: Total y Acciones --- */}
                <div className="flex flex-col items-end space-y-2 sm:flex-row sm:items-center sm:space-y-0 sm:space-x-2 w-full sm:w-auto">
                  <p className="text-md font-semibold text-gray-900 text-right">
                    Total: ${typeof order.total === 'number' ? order.total.toFixed(2) : 'N/A'}
                  </p>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => {
                      setSelectedOrder(order);
                      setIsDetailsModalOpen(true);
                    }}
                  >
                    View Details
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
        
                        {isDetailsModalOpen && selectedOrder && (
                          <OrderDetailsModal
                            isOpen={isDetailsModalOpen}
                            order={selectedOrder}
                            onClose={handleCloseModal}
                          />
                        )}
    </Layout>
  );
};

export default Orders;
