
import React, { useState } from "react";
import { Layout } from "@/components/layout/Layout";
import { useCart } from "@/contexts/CartContext";
import { useAuth } from "../hooks/use-auth";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Minus, Plus, Trash2, ArrowLeft, ShoppingCart, CreditCard, Home } from "lucide-react";
import { toast } from "sonner";
import { orderAPI } from "@/services/api";
import { 
  Card, 
  CardHeader, 
  CardTitle, 
  CardContent, 
  CardFooter
} from "@/components/ui/card";
import {
  Table,
  TableHeader,
  TableBody,
  TableHead,
  TableRow,
  TableCell
} from "@/components/ui/table";
import { 
  Dialog, 
  DialogContent, 
  DialogHeader, 
  DialogTitle, 
  DialogFooter,
  DialogDescription
} from "@/components/ui/dialog";
import { OrderStatus } from "@/types/models";

type PaymentMethod = "card" | "pickup";

const Cart = () => {
  const { items, totalItems, totalPrice, restaurant, updateItemQuantity, removeItem, clearCart } = useCart();
  const { user: currentUser, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] = useState(false);
  const [isConfirmationModalOpen, setIsConfirmationModalOpen] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("card");
  const [isProcessing, setIsProcessing] = useState(false);

  const handleOpenCheckout = () => {
    if (!isAuthenticated) {
      toast.error("Login Required", {
        description: "You need to be logged in to checkout.",
        action: {
          label: "Log in",
          onClick: () => navigate('/login')
        }
      });
      return;
    }

    if (!currentUser || !currentUser.id) {
      toast.error("User information missing");
      return;
    }
  
    if (!restaurant) {
      toast.error("No restaurant selected");
      return;
    }

    setIsCheckoutModalOpen(true);
  };

  const handlePayment = async () => {
    if (!currentUser || !currentUser.id || !restaurant) {
      toast.error("Missing information for checkout");
      return;
    }

    setIsProcessing(true);

    try {
      const orderData = {
        clientId: currentUser.id,
        restaurantId: restaurant.id.toString(),
        status: paymentMethod === "card" ? "pagado" : "pendiente" as OrderStatus,
        total: totalPrice,
        comments: paymentMethod === "pickup" ? "Payment on pickup" : "Paid by card",
        items: items.map(item => ({
          id: '',
          productId: item.productId,
          quantity: item.quantity,
          subtotal: item.subtotal
        })),
      };
      
      const response = await orderAPI.create(orderData, currentUser.email);
      
      // Close checkout modal and open confirmation
      setIsCheckoutModalOpen(false);
      setIsConfirmationModalOpen(true);
      
      // Simulate payment processing
      setTimeout(() => {
        setIsProcessing(false);
      }, 1500);
    } catch (error) {
      console.error("Error placing order:", error);
      toast.error("Failed to place order. Please try again.");
      setIsProcessing(false);
    }
  };

  const handleOrderComplete = () => {
    setIsConfirmationModalOpen(false);
    clearCart();
    navigate('/');
    toast.success("Thank you for your order!");
  };

  return (
    <Layout>
      <div className="max-w-4xl mx-auto mt-8 p-4">
        <Card>
          <CardHeader>
            <CardTitle className="text-2xl font-semibold">
              <div className="flex items-center">
                <ShoppingCart className="mr-2 h-6 w-6" />
                Your Cart
              </div>
            </CardTitle>
          </CardHeader>
          <CardContent>
            {items.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-500">Your cart is empty.</p>
                <Button asChild className="mt-4 bg-food-600 hover:bg-food-700">
                  <Link to="/">Continue Shopping</Link>
                </Button>
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Product</TableHead>
                    <TableHead>Price</TableHead>
                    <TableHead>Quantity</TableHead>
                    <TableHead>Subtotal</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {items.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className="font-medium">{item.productId}</TableCell>
                      <TableCell>${(item.subtotal/item.quantity).toFixed(2)}</TableCell>
                      <TableCell>
                        <div className="flex items-center">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => updateItemQuantity(item.id, item.quantity - 1)}
                          >
                            <Minus className="h-4 w-4" />
                          </Button>
                          <span className="mx-2">{item.quantity}</span>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => updateItemQuantity(item.id, item.quantity + 1)}
                          >
                            <Plus className="h-4 w-4" />
                          </Button>
                        </div>
                      </TableCell>
                      <TableCell>${(item.subtotal.toFixed(2))}</TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => removeItem(item.id)}
                        >
                          <Trash2 className="h-4 w-4 mr-2" />
                          Remove
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
          {items.length > 0 && (
            <CardFooter className="flex justify-between items-center">
              <div className="font-semibold">Total Items: {totalItems}</div>
              <div className="text-2xl font-bold">Total Price: ${totalPrice.toFixed(2)}</div>
            </CardFooter>
          )}
          <CardFooter className="flex justify-between items-center">
            <Button asChild variant="link">
              <Link to="/" className="flex items-center">
                <ArrowLeft className="w-4 h-4 mr-1" />
                Continue Shopping
              </Link>
            </Button>
            {items.length > 0 && (
              <Button 
                className="bg-food-600 hover:bg-food-700" 
                onClick={handleOpenCheckout}
              >
                Checkout
              </Button>
            )}
          </CardFooter>
        </Card>
      </div>

      {/* Checkout Modal */}
      <Dialog open={isCheckoutModalOpen} onOpenChange={setIsCheckoutModalOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Checkout</DialogTitle>
            <DialogDescription>
              Complete your order by selecting a payment method
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-4 py-4">
            <div className="grid grid-cols-2 gap-4">
              <div 
                className={`border rounded-lg p-4 cursor-pointer flex flex-col items-center transition-all ${
                  paymentMethod === "card" ? "border-food-600 bg-food-50" : "hover:border-gray-400"
                }`}
                onClick={() => setPaymentMethod("card")}
              >
                <CreditCard className="h-8 w-8 mb-2 text-food-600" />
                <h3 className="font-medium">Pay with Card</h3>
                <p className="text-sm text-gray-500 text-center">Process payment now</p>
              </div>
              
              <div 
                className={`border rounded-lg p-4 cursor-pointer flex flex-col items-center transition-all ${
                  paymentMethod === "pickup" ? "border-food-600 bg-food-50" : "hover:border-gray-400"
                }`}
                onClick={() => setPaymentMethod("pickup")}
              >
                <Home className="h-8 w-8 mb-2 text-food-600" />
                <h3 className="font-medium">Pay at Pickup</h3>
                <p className="text-sm text-gray-500 text-center">Pay when collecting your order</p>
              </div>
            </div>
            
            <div className="bg-gray-50 p-4 rounded-lg">
              <h4 className="font-medium mb-2">Order Summary</h4>
              <div className="flex justify-between mb-1">
                <span>Items ({totalItems}):</span>
                <span>${totalPrice.toFixed(2)}</span>
              </div>
              <div className="flex justify-between font-bold text-lg mt-2 pt-2 border-t">
                <span>Total:</span>
                <span>${totalPrice.toFixed(2)}</span>
              </div>
            </div>
          </div>
          
          <DialogFooter>
            <Button 
              variant="outline" 
              onClick={() => setIsCheckoutModalOpen(false)}
            >
              Cancel
            </Button>
            <Button 
              className="bg-food-600 hover:bg-food-700"
              onClick={handlePayment}
              disabled={isProcessing}
            >
              {isProcessing ? "Processing..." : paymentMethod === "card" ? "Pay Now" : "Place Order"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      
      {/* Order Confirmation Modal */}
      <Dialog open={isConfirmationModalOpen} onOpenChange={setIsConfirmationModalOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-center text-2xl text-food-600">
              Order Confirmed!
            </DialogTitle>
          </DialogHeader>
          
          <div className="py-6 flex flex-col items-center">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mb-4">
              <svg 
                xmlns="http://www.w3.org/2000/svg" 
                className="h-10 w-10 text-green-500" 
                fill="none" 
                viewBox="0 0 24 24" 
                stroke="currentColor"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            
            <p className="text-center text-gray-600 mb-4">
              {paymentMethod === "card" 
                ? "Your payment was successful and your order has been placed." 
                : "Your order has been placed. Please pay when you pick up your order."}
            </p>
            
            <p className="text-sm text-gray-500 text-center">
              You will receive an email confirmation shortly.
            </p>
          </div>
          
          <DialogFooter>
            <Button 
              className="w-full bg-food-600 hover:bg-food-700"
              onClick={handleOrderComplete}
            >
              Continue Shopping
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Layout>
  );
};

export default Cart;
