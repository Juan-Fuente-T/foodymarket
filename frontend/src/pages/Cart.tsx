
import React, { useState, useEffect } from "react";
import OrderConfirmationModal from "@/components/order/OrderConfirmationModal";
import CheckoutModalProps from "@/components/order/CheckoutModal";
import { Layout } from "@/components/layout/Layout";
import { useCart } from "@/contexts/CartContext";
import { useAuth } from "../hooks/use-auth";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Minus, Plus, Trash2, ArrowLeft, ShoppingCart, CreditCard, Home, Truck } from "lucide-react";
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
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Textarea } from "@/components/ui/textarea"; 
import { OrderStatus } from "@/types/models";

type DeliveryMethod = "Recogida en local" | "Entrega a domicilio";

interface CardDetails {
  cardNumber: string;
  cardExpiry: string;
  cardName: string;
  cardCvc: string;
}

const Cart = () => {
  const { items, totalItems, totalPrice, restaurant, updateItemQuantity, removeItem, clearCart } = useCart();
  const { user: currentUser, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] = useState(false);
  const [isConfirmationModalOpen, setIsConfirmationModalOpen] = useState(false);
  const [deliveryMethod, setDeliveryMethod] = useState<DeliveryMethod>("Recogida en local");
  const [isProcessing, setIsProcessing] = useState(false);
  const [cardDetails, setCardDetails] = useState<CardDetails>({
    cardNumber: "",
    cardExpiry: "",
    cardName: "",
    cardCvc: ""
  });
  const [cardErrors, setCardErrors] = useState({
    cardNumber: false,
    cardExpiry: false,
    cardName: false,
    cardCvc: false
  });
  const [orderComments, setOrderComments] = useState<string>('');

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
      toast.error("No restaurant selected. Please try adding items to your cart again.");
      return;
    }

    setIsCheckoutModalOpen(true);
  };

  const validateCardDetails = (): boolean => {
    const errors = {
      cardNumber: !cardDetails.cardNumber || cardDetails.cardNumber.length < 16,
      cardExpiry: !cardDetails.cardExpiry || !cardDetails.cardExpiry.match(/^\d{2}\/\d{2}$/),
      cardName: !cardDetails.cardName || cardDetails.cardName.length < 3,
      cardCvc: !cardDetails.cardCvc || cardDetails.cardCvc.length < 3
    };
    
    setCardErrors(errors);
    return !Object.values(errors).some(error => error);
  };

  const handlePayment = async () => {
    if (!currentUser || !currentUser.id || !restaurant) {
      toast.error("Missing information for checkout");
      return;
    }

    if (!validateCardDetails()) {
      toast.error("Please fill in all card details correctly");
      return;
    }

    setIsProcessing(true);

    try {
      const orderData = {
        clientId: currentUser.id,
        restaurantId: restaurant.id.toString(),
        status: "pagado" as OrderStatus,
        total: totalPrice,
        // comments: deliveryMethod === "pickup" ? "Customer will pickup" : "Delivery to customer address",
        comments: orderComments,
        details: items.map(item => ({
          id: '',
          productId: item.productId,
          quantity: item.quantity,
          subtotal: item.subtotal
        })),
      };
      
      // console.log("Submitting order:", orderData);
      const response = await orderAPI.create(orderData, currentUser.email);
      // console.log("Order created response:", response);
      
      // Close checkout modal and open confirmation
      setIsCheckoutModalOpen(false);
      setIsConfirmationModalOpen(true);
      
      //FOR ME IN THE FUTURE:
      // Simulate a successful payment process
      // In a real-world scenario, you would integrate with a payment gateway here
      // For example, using Stripe or PayPal SDKs to handle the payment
      // After successful payment, must be updated the order status in the backend
      // For now, we'll just simulate a successful payment
      // and set a timeout to close the modaland clear the cart after a few seconds
      setTimeout(() => {
        setIsProcessing(false);
        setIsConfirmationModalOpen(false);
        clearCart();
        navigate('/orders');
      }, 5000);
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

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCardDetails(prev => ({
      ...prev,
      [name]: value
    }));
  };
  const handleContinueShopping = () => {
    setIsConfirmationModalOpen(false); 
    clearCart();                    
    navigate('/');                   
    toast.success("Thank you for your order!"); 
};

  const formatCardNumber = (e: React.ChangeEvent<HTMLInputElement>) => {
    let { value } = e.target;
    // Remove anything that's not a digit
    value = value.replace(/\D/g, '');
    // Limit to max 16 digits
    value = value.slice(0, 16);
    
    setCardDetails(prev => ({
      ...prev,
      cardNumber: value
    }));
  };

  const formatCardExpiry = (e: React.ChangeEvent<HTMLInputElement>) => {
    let { value } = e.target;
    // Remove anything that's not a digit or slash
    value = value.replace(/[^\d/]/g, '');
    
    // If user is typing and gets to 2 digits, add slash automatically
    if (value.length === 2 && !value.includes('/') && e.target.selectionStart === 2) {
      value = `${value}/`;
    }
    
    // Don't allow more than 5 chars (MM/YY)
    value = value.slice(0, 5);
    
    setCardDetails(prev => ({
      ...prev,
      cardExpiry: value
    }));
  };

  const formatCardCvc = (e: React.ChangeEvent<HTMLInputElement>) => {
    let { value } = e.target;
    // Remove anything that's not a digit
    value = value.replace(/\D/g, '');
    // Limit to max 4 digits
    value = value.slice(0, 4);
    
    setCardDetails(prev => ({
      ...prev,
      cardCvc: value
    }));
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
                      <TableCell className="font-medium">{item.productName || item.productId}</TableCell>
                      <TableCell>${(item.productPrice || '0.00')}</TableCell>
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
                      <TableCell>${(item.subtotal || '0.00')}</TableCell>
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
            
              <div className="p-4">
            <CardFooter className="flex justify-between items-center">
              <div className="font-semibold">Total Items: {totalItems}</div>
              <div className="text-2xl font-bold">Total Price: ${parseFloat(totalPrice).toFixed(2)}</div>
            </CardFooter>
              <Textarea 
              id="orderComments"
              value={orderComments}
              placeholder="Commets about your order..."      
              // className="w-5/6 h-16 my-4 mx-auto" 
              className=" h-16 my-4" 
              onChange={(e) => setOrderComments(e.target.value)}>
              </Textarea>
            </div>
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

      <CheckoutModalProps
        isOpen={isCheckoutModalOpen} 
        onClose={() => setIsCheckoutModalOpen(false)} 
        restaurant={restaurant}
        totalItems={totalItems}
        totalPrice={totalPrice} 
        isProcessing={isProcessing}
        cardDetails={cardDetails}
        cardErrors={cardErrors}
        deliveryMethod={deliveryMethod}  
        setDeliveryMethod={setDeliveryMethod}
        formatCardNumber={formatCardNumber}
        formatCardExpiry={formatCardExpiry}
        formatCardCvc={formatCardCvc}
        handlePayment={handlePayment}
        handleInputChange={handleInputChange}
      />  
      
      <OrderConfirmationModal 
        isOpen={isConfirmationModalOpen} 
        onClose={() => setIsConfirmationModalOpen(false)} 
        onContinueShopping={handleContinueShopping} 
        deliveryMethod={deliveryMethod}
      />
    </Layout>
  );
};

export default Cart;
