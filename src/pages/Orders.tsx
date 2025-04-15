
import React, { useState } from 'react';
import { Layout } from "@/components/layout/Layout";
import { useQuery } from "@tanstack/react-query";
import { orderAPI } from "@/services/api";
import { useAuth } from "../hooks/use-auth";
import { Order, OrderStatus } from "@/types/models";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Navigate } from "react-router-dom";
import { Calendar, Clock, Package, Filter } from "lucide-react";
import { format } from "date-fns";

const Orders = () => {
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const [statusFilter, setStatusFilter] = useState<OrderStatus | 'all'>('all');
  
  // Redirigir si no está autenticado
  if (!authLoading && !isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  // Dependiendo del rol, cargamos diferentes datos
  const isRestaurant = user?.role === 'restaurante';
  
  const { data: orders = [], isLoading } = useQuery({
    queryKey: ["orders", user?.id, isRestaurant],
    queryFn: async () => {
      if (!user) return [];
      
      if (isRestaurant) {
        // Si es restaurante, obtener todos los pedidos del restaurante
        return orderAPI.getByRestaurant(user.id);
      } else {
        // Si es cliente, obtener los pedidos del cliente
        return orderAPI.getByClient(user.id);
      }
    },
    enabled: !!user && isAuthenticated,
  });
  
  // Filtrar órdenes por estado
  const filteredOrders = orders.filter(
    order => statusFilter === 'all' || order.status === statusFilter
  );
  
  // Función para obtener color de estado
  const getStatusColor = (status: OrderStatus) => {
    switch (status) {
      case 'pendiente': return 'bg-yellow-100 text-yellow-800';
      case 'pagado': return 'bg-blue-100 text-blue-800';
      case 'entregado': return 'bg-green-100 text-green-800';
      case 'cancelado': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };
  
  // Función para formatear fecha
  const formatDate = (dateString: string) => {
    try {
      return format(new Date(dateString), 'dd/MM/yyyy HH:mm');
    } catch (error) {
      return dateString;
    }
  };
  
  if (authLoading) {
    return (
      <Layout>
        <div className="flex justify-center items-center h-screen">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-food-600"></div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-4">
          {isRestaurant ? 'Restaurant Orders' : 'My Orders'}
        </h1>
        <p className="text-gray-600 mb-8">
          {isRestaurant 
            ? 'Manage orders from your customers' 
            : 'View your order history and track current orders'}
        </p>
        
        <div className="bg-white rounded-xl shadow-sm p-6 mb-8">
          <div className="flex flex-wrap justify-between items-center mb-6">
            <h2 className="text-xl font-semibold">
              {statusFilter === 'all' ? 'All Orders' : `${statusFilter.charAt(0).toUpperCase() + statusFilter.slice(1)} Orders`}
            </h2>
            
            <div className="flex space-x-2 mt-2 sm:mt-0">
              <Button
                variant={statusFilter === 'all' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setStatusFilter('all')}
                className={statusFilter === 'all' ? 'bg-food-600' : ''}
              >
                All
              </Button>
              <Button
                variant={statusFilter === 'pendiente' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setStatusFilter('pendiente')}
                className={statusFilter === 'pendiente' ? 'bg-food-600' : ''}
              >
                Pending
              </Button>
              <Button
                variant={statusFilter === 'pagado' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setStatusFilter('pagado')}
                className={statusFilter === 'pagado' ? 'bg-food-600' : ''}
              >
                Paid
              </Button>
              <Button
                variant={statusFilter === 'entregado' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setStatusFilter('entregado')}
                className={statusFilter === 'entregado' ? 'bg-food-600' : ''}
              >
                Delivered
              </Button>
              <Button
                variant={statusFilter === 'cancelado' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setStatusFilter('cancelado')}
                className={statusFilter === 'cancelado' ? 'bg-food-600' : ''}
              >
                Cancelled
              </Button>
            </div>
          </div>
          
          {isLoading ? (
            <div className="space-y-4">
              {Array(3).fill(0).map((_, i) => (
                <Skeleton key={i} className="h-24 w-full rounded-lg" />
              ))}
            </div>
          ) : filteredOrders.length > 0 ? (
            <div className="space-y-4">
              {filteredOrders.map((order) => (
                <div
                  key={order.id}
                  className="border rounded-lg p-4 hover:border-food-600 transition-colors"
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="font-medium text-lg">Order #{order.id}</h3>
                        <Badge className={getStatusColor(order.status)}>
                          {order.status.charAt(0).toUpperCase() + order.status.slice(1)}
                        </Badge>
                      </div>
                      
                      <div className="text-sm text-gray-500 mb-3">
                        <div className="flex items-center gap-1 mb-1">
                          <Calendar className="h-3.5 w-3.5" />
                          <span>{formatDate(order.createdAt)}</span>
                        </div>
                        
                        {order.comments && (
                          <div className="text-gray-600 mt-2">
                            <span className="font-medium">Comments: </span>
                            {order.comments}
                          </div>
                        )}
                      </div>
                      
                      <div className="mt-2">
                        <span className="font-medium">Items: </span>
                        {order.items.length} items
                      </div>
                    </div>
                    
                    <div className="text-right">
                      <div className="text-xl font-semibold text-food-700">
                        ${order.total.toFixed(2)}
                      </div>
                      
                      <Button
                        variant="outline"
                        size="sm"
                        className="mt-2"
                      >
                        View Details
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="py-12 text-center">
              <Package className="mx-auto h-12 w-12 text-gray-400 mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-1">No orders found</h3>
              <p className="text-gray-500">
                {statusFilter !== 'all' 
                  ? `You don't have any ${statusFilter} orders yet` 
                  : `You don't have any orders yet`}
              </p>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default Orders;
