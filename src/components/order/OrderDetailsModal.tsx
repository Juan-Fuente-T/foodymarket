import React from 'react';
 
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { 
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { OrderStatus, Order } from '@/types/models';
import { orderAPI } from '@/services/api';
import { toast } from 'sonner';

interface OrderDetailsModalProps {
  order: Order | null;
  isOpen: boolean;
  onClose: () => void;
  onStatusChange?: () => void;
}

const OrderDetailsModal: React.FC<OrderDetailsModalProps> = ({
  order,
  isOpen,
  onClose,
  onStatusChange
}) => {

  const handleStatusChange = async (newStatus: OrderStatus) => {
    if (!order?.id) return;
    try {
      await orderAPI.updateStatus(order.id, newStatus);
      toast.success("Order status updated successfully");
      if (onStatusChange) onStatusChange();
    } catch (error) {
      console.error("Error updating order status:", error);
      toast.error("Failed to update order status");
    }
  };

  if (!order) return null;
  return (
    <Dialog open={isOpen} onOpenChange={() => onClose()}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Order Details #{order.id}</DialogTitle>
        </DialogHeader>
        <div className="grid gap-6">
          <div className="flex justify-between items-center">
            <div>
              <p className="text-sm text-gray-500 mb-2">Order Status</p>
              <Select
                defaultValue={order.status}
                onValueChange={(value) => handleStatusChange(value as OrderStatus)}
              >

                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="Select status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="pendiente">Pendiente</SelectItem>
                  <SelectItem value="pagado">Pagado</SelectItem>
                  <SelectItem value="entregado">Entregado</SelectItem>
                  <SelectItem value="cancelado">Cancelado</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <p className="text-sm text-gray-500 mb-2">Total Amount</p>
              <p className="text-lg font-semibold">${order.total.toFixed(2)}</p>
            </div>
          </div>
          {order.comments && (
            <div>
              <p className="text-sm text-gray-500 mb-2">Customer Comments</p>
              <p className="p-3 bg-gray-50 rounded-md">{order.comments}</p>
            </div>
          )}
          <div>
             <p className="text-sm text-gray-500 mb-2">Order Items</p>
            <div className="space-y-2">
              {order.details.map((item) => (
                <div
                  key={item.id}
                  className="flex justify-between p-3 bg-gray-50 rounded-md"
                >
                  <div>
                    <p className="font-medium">{item.productName}</p>
                    <p className="text-sm text-gray-500">Quantity: {item.quantity}</p>
                  </div>
                  <p className="font-medium">${item.subtotal.toFixed(2)}</p>
                </div>
              ))}
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-gray-500">Order Date</p>
              <p>{new Date(order.createdAt).toLocaleString()}</p>
            </div>
            <div>
              <p className="text-gray-500">Last Updated</p>
              <p>{new Date(order.updatedAt).toLocaleString()}</p>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default OrderDetailsModal;