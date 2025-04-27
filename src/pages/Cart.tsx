
import React, { useState, useEffect } from "react";
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

type DeliveryMethod = "pickup" | "delivery";

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
  const [deliveryMethod, setDeliveryMethod] = useState<DeliveryMethod>("pickup");
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
      
      console.log("Submitting order:", orderData);
      const response = await orderAPI.create(orderData, currentUser.email);
      console.log("Order created response:", response);
      
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

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCardDetails(prev => ({
      ...prev,
      [name]: value
    }));
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
            
              <div className="p-4">
            <CardFooter className="flex justify-between items-center">
              <div className="font-semibold">Total Items: {totalItems}</div>
              <div className="text-2xl font-bold">Total Price: ${totalPrice.toFixed(2)}</div>
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

      {/* Checkout Modal */}
      <Dialog open={isCheckoutModalOpen} onOpenChange={setIsCheckoutModalOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Complete Your Order</DialogTitle>
            <DialogDescription>
              Choose delivery method and enter your payment details
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-6 py-4">
            {/* Delivery Method Selection */}
            <div className="space-y-4">
              <Label>Delivery Method</Label>
              <RadioGroup 
                defaultValue="pickup" 
                value={deliveryMethod} 
                onValueChange={value => setDeliveryMethod(value as DeliveryMethod)} 
                className="grid grid-cols-2 gap-4"
              >
                <div className={`flex items-center space-x-2 border rounded-lg p-4 cursor-pointer ${
                  deliveryMethod === "pickup" ? "border-food-600 bg-food-50" : ""
                }`}>
                  <RadioGroupItem value="pickup" id="pickup" />
                  <Label htmlFor="pickup" className="flex items-center cursor-pointer">
                    <Home className="h-5 w-5 mr-2 text-food-600" />
                    Pickup
                  </Label>
                </div>
                
                <div className={`flex items-center space-x-2 border rounded-lg p-4 cursor-pointer ${
                  deliveryMethod === "delivery" ? "border-food-600 bg-food-50" : ""
                }`}>
                  <RadioGroupItem value="delivery" id="delivery" />
                  <Label htmlFor="delivery" className="flex items-center cursor-pointer">
                    <Truck className="h-5 w-5 mr-2 text-food-600" />
                    Delivery
                  </Label>
                </div>
              </RadioGroup>
            </div>
            
            {/* Card Payment Form */}
            <div className="space-y-4 border rounded-lg p-4">
              <div className="flex items-center mb-2">
                <CreditCard className="h-5 w-5 mr-2 text-food-600" />
                <h3 className="text-lg font-medium">Payment Details</h3>
              </div>
              
              <div className="space-y-4">
                <div>
                  <Label htmlFor="cardName">Cardholder Name</Label>
                  <Input 
                    id="cardName" 
                    name="cardName"
                    placeholder="John Smith" 
                    value={cardDetails.cardName}
                    onChange={handleInputChange}
                    className={cardErrors.cardName ? "border-red-500" : ""}
                  />
                  {cardErrors.cardName && <p className="text-red-500 text-xs mt-1">Please enter a valid name</p>}
                </div>
                
                <div>
                  <Label htmlFor="cardNumber">Card Number</Label>
                  <Input 
                    id="cardNumber" 
                    name="cardNumber"
                    placeholder="1234 5678 9012 3456"
                    value={cardDetails.cardNumber}
                    onChange={formatCardNumber}
                    className={cardErrors.cardNumber ? "border-red-500" : ""}
                    maxLength={16}
                  />
                  {cardErrors.cardNumber && <p className="text-red-500 text-xs mt-1">Please enter a valid card number</p>}
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="cardExpiry">Expiry Date</Label>
                    <Input 
                      id="cardExpiry"
                      name="cardExpiry" 
                      placeholder="MM/YY" 
                      value={cardDetails.cardExpiry}
                      onChange={formatCardExpiry}
                      className={cardErrors.cardExpiry ? "border-red-500" : ""}
                      maxLength={5}
                    />
                    {cardErrors.cardExpiry && <p className="text-red-500 text-xs mt-1">Please enter a valid date (MM/YY)</p>}
                  </div>
                  
                  <div>
                    <Label htmlFor="cardCvc">CVC</Label>
                    <Input 
                      id="cardCvc" 
                      name="cardCvc"
                      placeholder="123" 
                      value={cardDetails.cardCvc}
                      onChange={formatCardCvc}
                      className={cardErrors.cardCvc ? "border-red-500" : ""}
                      maxLength={4}
                    />
                    {cardErrors.cardCvc && <p className="text-red-500 text-xs mt-1">Please enter a valid CVC</p>}
                  </div>
                </div>
              </div>
            </div>
            
            <div className="bg-gray-50 p-4 rounded-lg">
              <h4 className="font-medium mb-2">Order Summary</h4>
              <div className="flex justify-between mb-1">
                <span>Items ({totalItems}):</span>
                <span>${totalPrice.toFixed(2)}</span>
              </div>
              {restaurant && (
                <div className="flex justify-between mb-1">
                  <span>Restaurant:</span>
                  <span>{restaurant.name}</span>
                </div>
              )}
              {deliveryMethod === "delivery" && (
                <div className="flex justify-between mb-1">
                  <span>Delivery Fee:</span>
                  <span>${(restaurant?.deliveryFee || 2).toFixed(2)}</span>
                </div>
              )}
              <div className="flex justify-between font-bold text-lg mt-2 pt-2 border-t">
                <span>Total:</span>
                <span>${(
                  totalPrice + (deliveryMethod === "delivery" ? (restaurant?.deliveryFee || 2) : 0)
                ).toFixed(2)}</span>
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
              {isProcessing ? "Processing..." : "Complete Payment"}
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
              Your payment was successful and your order has been placed.
            </p>
            
            <div className="bg-gray-50 w-full p-4 rounded-lg mb-4">
              <h4 className="font-medium mb-2 text-center">Order Details</h4>
              {deliveryMethod === "pickup" ? (
                <p className="text-center text-gray-500">
                  You will pick up your order at the restaurant
                </p>
              ) : (
                <p className="text-center text-gray-500">
                  Your order will be delivered to your address
                </p>
              )}
            </div>
            
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
