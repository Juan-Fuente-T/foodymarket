import { RadioGroup } from "../ui/radio-group";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "../ui/dialog";
import { RadioGroupItem } from "../ui/radio-group";
import { Label } from "../ui/label";
import { CreditCard, Home, Truck } from "lucide-react";
import { Input } from "../ui/input";
import { Button } from "../ui/button";

interface CheckoutModalProps {
    isOpen: boolean;
    onClose: () => void;
    restaurant?: {
        name: string;
        deliveryFee?: number;
    };
    totalItems: number;
    totalPrice: number;
    isProcessing: boolean;
    cardDetails: CardDetails;
    cardErrors: CardErrors;
    deliveryMethod: "Recogida en local" | "Entrega a domicilio";
    setDeliveryMethod: (method: "Recogida en local" | "Entrega a domicilio") => void;
    formatCardNumber: (e: React.ChangeEvent<HTMLInputElement>) => void;
    formatCardExpiry: (e: React.ChangeEvent<HTMLInputElement>) => void;
    formatCardCvc: (e: React.ChangeEvent<HTMLInputElement>) => void;
    handlePayment: () => void;
    handleInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
};
// interface DeliveryMethod {
//     pickup: "Recoger en local";
//     delivery: "Envio a domicilio";
// };
interface CardDetails {
    cardNumber: string;
    cardExpiry: string;
    cardName: string;
    cardCvc: string;
};
interface CardErrors {
    cardNumber: boolean,
    cardExpiry: boolean,
    cardName: boolean,
    cardCvc: boolean
};
const CheckoutModal: React.FC<CheckoutModalProps> = ({
    isOpen,
    onClose,
    restaurant,
    totalItems,
    totalPrice,
    handlePayment,
    isProcessing,
    cardDetails,
    cardErrors,
    setDeliveryMethod,
    deliveryMethod,
    formatCardNumber,
    formatCardExpiry,
    formatCardCvc,
    handleInputChange
}) => {

    return (
        <Dialog open={isOpen} onOpenChange={(openState) => { if (!openState) onClose(); }}>
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
                            defaultValue="Recogida en local"
                            value={deliveryMethod}
                            onValueChange={(value: "Recogida en local" | "Entrega a domicilio") => setDeliveryMethod(value)}
                            className="grid grid-cols-2 gap-4"
                        >
                            <div className={`flex items-center space-x-2 border rounded-lg p-4 cursor-pointer ${deliveryMethod === "Recogida en local" ? "border-food-600 bg-food-50" : ""
                                }`}>
                                <RadioGroupItem value="Recogida en local" id="Recogida en local" />
                                <Label htmlFor="Recogida en local" className="flex items-center cursor-pointer">
                                    <Home className="h-5 w-5 mr-2 text-food-600" />
                                    Recogida en local
                                </Label>
                            </div>

                            <div className={`flex items-center space-x-2 border rounded-lg p-4 cursor-pointer ${deliveryMethod === "Entrega a domicilio" ? "border-food-600 bg-food-50" : ""
                                }`}>
                                <RadioGroupItem value="Entrega a domicilio" id="Entrega a domicilio" />
                                <Label htmlFor="Entrega a domicilio" className="flex items-center cursor-pointer">
                                    <Truck className="h-5 w-5 mr-2 text-food-600" />
                                    Entrega a domicilio
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
                        {deliveryMethod === "Recogida en local" && (
                            <div className="flex justify-between mb-1">
                                <span>Delivery Fee:</span>
                                <span>${(restaurant?.deliveryFee || 2).toFixed(2)}</span>
                            </div>
                        )}
                        <div className="flex justify-between font-bold text-lg mt-2 pt-2 border-t">
                            <span>Total:</span>
                            <span>${(
                                totalPrice + (deliveryMethod === "Entrega a domicilio" ? (restaurant?.deliveryFee || 2) : 0)
                            ).toFixed(2)}</span>
                        </div>
                    </div>
                </div>

                <DialogFooter>
                    <Button
                        variant="outline"
                        onClick={onClose}
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
    );
};

export default CheckoutModal;