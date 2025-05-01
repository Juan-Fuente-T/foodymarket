import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "../ui/dialog";
import { Button } from "../ui/button";

interface OrderConfirmationModalProps {
    isOpen: boolean;
    onClose: () => void;
    onContinueShopping: () => void;
    deliveryMethod?: 'Recogida en local' | 'Entrega a domicilio' | string; 
  }

const OrderConfirmationModal: React.FC<OrderConfirmationModalProps> = ({
    isOpen,
    onClose,
    onContinueShopping,
    deliveryMethod
  }) => {
    
     return (
      <Dialog open={isOpen} onOpenChange={(openState) => { if (!openState) onClose(); }}>
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
              onClick={onContinueShopping}
            >
              Continue Shopping
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
        );
};

export default OrderConfirmationModal;
