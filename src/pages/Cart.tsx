
import React from "react";
import { Layout } from "@/components/layout/Layout";
import { useCart } from "@/contexts/CartContext";
import { useAuth } from "@/contexts/AuthContext";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Minus, Plus, Trash2, ArrowLeft, ShoppingCart } from "lucide-react";
import { toast } from "@/lib/toast";
import { orderAPI } from "@/services/api";
import { 
  Card, 
  CardContent, 
  CardHeader, 
  CardTitle, 
  CardDescription, 
  CardFooter 
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from "@/components/ui/table";

const Cart = () => {
  const { items, restaurant, removeItem, updateItemQuantity, clearCart, totalItems, totalPrice } = useCart();
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleCheckout = async () => {
    if (!isAuthenticated) {
      toast({
        title: "Login Required",
        description: "You need to be logged in to checkout.",
        action: (
          <Button 
            onClick={() => navigate('/login')}
            className="bg-food-600 text-white px-3 py-1 rounded-md font-medium"
          >
            Log in
          </Button>
        ),
      });
      return;
    }

    if (!restaurant) {
      toast.error("No restaurant selected");
      return;
    }

    try {
      // Prepare order items with correct structure including id and product
      const orderItems = items.map(item => ({
        id: item.id,
        productId: item.productId,
        product: item.product,
        quantity: item.quantity,
        price: item.price,
        notes: item.notes,
      }));

      await orderAPI.create({
        userId: user?.id,
        restaurantId: restaurant?.id,
        items: orderItems,
        status: "pending",
        total: totalPrice,
        deliveryAddress: user?.address || "",
        paymentMethod: "card",
      });
      
      clearCart();
      toast.success("Order placed successfully!");
      navigate("/");
    } catch (error) {
      console.error("Checkout error:", error);
      toast.error("Failed to place order");
    }
  };

  if (items.length === 0) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <Card className="w-full">
            <CardHeader>
              <CardTitle className="text-2xl">Your Cart</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col items-center py-10">
              <ShoppingCart className="w-16 h-16 text-gray-300 mb-4" />
              <h3 className="text-xl font-medium text-gray-700">Your cart is empty</h3>
              <p className="text-gray-500 mb-6">Add items to your cart to see them here</p>
              <Button asChild className="bg-food-600 hover:bg-food-700">
                <Link to="/">Browse Restaurants</Link>
              </Button>
            </CardContent>
          </Card>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Your Cart</h1>
        
        {restaurant && (
          <div className="mb-6">
            <h2 className="text-lg font-medium mb-2">Order from:</h2>
            <Link to={`/restaurants/${restaurant.id}`} className="text-food-600 hover:text-food-700">
              {restaurant.name}
            </Link>
          </div>
        )}
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle>Order Items</CardTitle>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Product</TableHead>
                      <TableHead>Price</TableHead>
                      <TableHead>Quantity</TableHead>
                      <TableHead>Total</TableHead>
                      <TableHead></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {items.map((item) => (
                      <TableRow key={item.id}>
                        <TableCell>
                          <div className="flex items-center">
                            <img 
                              src={item.product.image || "https://via.placeholder.com/50"} 
                              alt={item.product.name} 
                              className="w-12 h-12 object-cover rounded-md mr-3" 
                            />
                            <div>
                              <div className="font-medium">{item.product.name}</div>
                              {item.notes && (
                                <div className="text-sm text-gray-500">{item.notes}</div>
                              )}
                            </div>
                          </div>
                        </TableCell>
                        <TableCell>${item.price.toFixed(2)}</TableCell>
                        <TableCell>
                          <div className="flex items-center space-x-2">
                            <Button
                              variant="outline"
                              size="icon"
                              className="h-8 w-8"
                              onClick={() => updateItemQuantity(item.id, item.quantity - 1)}
                            >
                              <Minus className="h-4 w-4" />
                            </Button>
                            <span>{item.quantity}</span>
                            <Button
                              variant="outline"
                              size="icon"
                              className="h-8 w-8"
                              onClick={() => updateItemQuantity(item.id, item.quantity + 1)}
                            >
                              <Plus className="h-4 w-4" />
                            </Button>
                          </div>
                        </TableCell>
                        <TableCell>${(item.price * item.quantity).toFixed(2)}</TableCell>
                        <TableCell>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="text-red-500 hover:text-red-700"
                            onClick={() => removeItem(item.id)}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
              <CardFooter className="flex justify-end">
                <Button variant="outline" onClick={clearCart}>
                  Clear Cart
                </Button>
              </CardFooter>
            </Card>
          </div>
          
          <div>
            <Card>
              <CardHeader>
                <CardTitle>Order Summary</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span>Subtotal</span>
                    <span>${totalPrice.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Delivery Fee</span>
                    <span>$0.00</span>
                  </div>
                  <div className="border-t pt-2 mt-2">
                    <div className="flex justify-between font-bold">
                      <span>Total</span>
                      <span>${totalPrice.toFixed(2)}</span>
                    </div>
                  </div>
                </div>
              </CardContent>
              <CardFooter>
                <Button 
                  className="w-full bg-food-600 hover:bg-food-700"
                  onClick={handleCheckout}
                >
                  Proceed to Checkout
                </Button>
              </CardFooter>
            </Card>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default Cart;
