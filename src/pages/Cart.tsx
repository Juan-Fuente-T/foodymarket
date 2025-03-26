import React from "react";
import { Layout } from "@/components/layout/Layout";
import { useCart } from "@/contexts/CartContext";
import { useAuth } from "@/contexts/AuthContext";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Minus, Plus, Trash2, ArrowLeft, ShoppingCart } from "lucide-react";
import { toast } from "sonner";
import { orderAPI } from "@/services/api";
import { 
  Card, 
  CardHeader, 
  CardTitle, 
  CardContent, 
  CardFooter,
  Table,
  TableHeader,
  TableBody,
  TableHead,
  TableRow,
  TableCell
} from "@/components/ui/card";

const Cart = () => {
  const { items, totalItems, totalPrice, updateItemQuantity, removeItem, clearCart } = useCart();
  const { isAuthenticated, currentUser } = useAuth();
  const navigate = useNavigate();

  const handleCheckout = async () => {
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

    try {
      const orderItems = items.map(item => ({
        id: item.id,
        productId: item.productId,
        quantity: item.quantity,
        price: item.price,
        notes: item.notes
      }));

      const order = {
        userId: currentUser.id,
        restaurantId: items[0]?.product.restaurantId,
        items: orderItems,
        total: totalPrice,
        status: "pending"
      };

      // For mock development, just simulate order placement success
      // In production, this would use the actual API call
      console.log("Order placed:", order);
      toast.success("Order placed successfully!");
      clearCart();
      navigate('/');
      
      // Commented out for now to avoid API errors
      // const response = await orderAPI.create(order);
      // toast.success("Order placed successfully!");
      // clearCart();
      // navigate('/');
    } catch (error) {
      console.error("Error placing order:", error);
      toast.error("Failed to place order. Please try again.");
    }
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
                    <TableHead>Total</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {items.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className="font-medium">{item.product.name}</TableCell>
                      <TableCell>${item.price.toFixed(2)}</TableCell>
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
                      <TableCell>${(item.price * item.quantity).toFixed(2)}</TableCell>
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
              <Button className="bg-food-600 hover:bg-food-700" onClick={handleCheckout}>
                Checkout
              </Button>
            )}
          </CardFooter>
        </Card>
      </div>
    </Layout>
  );
};

export default Cart;
