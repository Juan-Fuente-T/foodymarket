
import React, { useState } from "react";
import { Layout } from "@/components/layout/Layout";
import { useAuth } from "../hooks/use-auth";
import { useNavigate, Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { 
  Card, 
  CardContent, 
  CardDescription, 
  CardHeader, 
  CardTitle,
  CardFooter
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useForm } from "react-hook-form";
import { toast } from "@/lib/toast";
import { restaurantAPI } from "@/services/api";
import { UserRole } from "@/types/models";

interface PartnerFormValues {
  restaurantName: string;
  restaurantDescription: string;
  restaurantAddress: string;
  restaurantPhone: string;
  restaurantEmail: string;
  restaurantCategory: string;
}

const CATEGORIES = [
  "Italian", "Mexican", "Chinese", "Japanese", "American", 
  "Thai", "Indian", "French", "Spanish", "Greek", "Other"
];

const RestaurantPartner = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, updateUser } = useAuth();
  const { register, handleSubmit, watch, setValue, formState: { errors, isSubmitting } } = useForm<PartnerFormValues>({
    defaultValues: {
      restaurantCategory: "Other"
    }
  });
  
  React.useEffect(() => {
    // If user is already a restaurant owner, redirect to dashboard
    if (isAuthenticated && user?.role === "restaurante") {
      navigate("/dashboard");
    }
  }, [isAuthenticated, user, navigate]);

  const onSubmit = async (data: PartnerFormValues) => {
    try {
      if (!isAuthenticated) {
        toast.error("You need to be logged in to register a restaurant");
        navigate("/login", { state: { from: "/partner" } });
        return;
      }
      
      if (!user?.id) {
        throw new Error("User ID is required");
      }
      
      console.log("Creating restaurant with data:", data);
      
      // Create the restaurant
      await restaurantAPI.create({
        name: data.restaurantName,
        description: data.restaurantDescription,
        address: data.restaurantAddress,
        phone: data.restaurantPhone,
        category: data.restaurantCategory,
        email: data.restaurantEmail,
        logo: "https://via.placeholder.com/150",
        ownerId: user.id,
        coverImage: "https://via.placeholder.com/800x400",
        rating: 0,
        reviewCount: 0,
        openingHours: "9:00 AM - 10:00 PM",
      });
      
      // Update user role to "restaurante"
      if (user.role !== "restaurante") {
        updateUser({ ...user, role: "restaurante" as UserRole });
      }
      
      toast.success("Restaurant registration successful!");
      navigate("/dashboard");
      
    } catch (error) {
      console.error("Partner registration error:", error);
      toast.error("Failed to register as a partner. Please try again.");
    }
  };

  return (
    <Layout>
      <div className="max-w-3xl mx-auto px-4 sm:px-6 py-12 mt-16">
        <Card>
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold text-center">Become a Restaurant Partner</CardTitle>
            <CardDescription className="text-center">
              Fill in the details below to register your restaurant
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {!isAuthenticated && (
                <div className="text-center py-8">
                  <p className="text-gray-500 mb-4">You need to be logged in to register a restaurant.</p>
                  <Button asChild className="bg-food-600 hover:bg-food-700">
                    <Link to="/login">Log In</Link>
                  </Button>
                </div>
              )}
              
              {isAuthenticated && (
                <div className="bg-gray-50 p-4 rounded-lg space-y-4">
                  <h3 className="font-medium text-lg">Restaurant Information</h3>
                  
                  <div className="space-y-2">
                    <Label htmlFor="restaurantName">Restaurant Name</Label>
                    <Input 
                      id="restaurantName" 
                      placeholder="My Amazing Restaurant" 
                      {...register("restaurantName", { 
                        required: "Restaurant name is required" 
                      })}
                    />
                    {errors.restaurantName && (
                      <p className="text-sm text-red-500">{errors.restaurantName.message}</p>
                    )}
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="restaurantCategory">Category</Label>
                    <Select 
                      onValueChange={(value) => setValue("restaurantCategory", value)}
                      defaultValue="Other"
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select a category" />
                      </SelectTrigger>
                      <SelectContent>
                        {CATEGORIES.map(category => (
                          <SelectItem key={category} value={category}>
                            {category}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="restaurantDescription">Description</Label>
                    <Textarea 
                      id="restaurantDescription" 
                      placeholder="Tell us about your restaurant..." 
                      className="min-h-[100px]"
                      {...register("restaurantDescription", { 
                        required: "Description is required" 
                      })}
                    />
                    {errors.restaurantDescription && (
                      <p className="text-sm text-red-500">{errors.restaurantDescription.message}</p>
                    )}
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="restaurantAddress">Address</Label>
                    <Input 
                      id="restaurantAddress" 
                      placeholder="123 Main St, City, Country" 
                      {...register("restaurantAddress", { 
                        required: "Address is required" 
                      })}
                    />
                    {errors.restaurantAddress && (
                      <p className="text-sm text-red-500">{errors.restaurantAddress.message}</p>
                    )}
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="restaurantPhone">Restaurant Phone</Label>
                      <Input 
                        id="restaurantPhone" 
                        placeholder="+1 (555) 987-6543" 
                        {...register("restaurantPhone", { 
                          required: "Restaurant phone is required" 
                        })}
                      />
                      {errors.restaurantPhone && (
                        <p className="text-sm text-red-500">{errors.restaurantPhone.message}</p>
                      )}
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="restaurantEmail">Restaurant Email</Label>
                      <Input 
                        id="restaurantEmail" 
                        type="email" 
                        placeholder="restaurant@example.com" 
                        {...register("restaurantEmail", { 
                          required: "Restaurant email is required",
                          pattern: {
                            value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                            message: "Invalid email address"
                          }
                        })}
                      />
                      {errors.restaurantEmail && (
                        <p className="text-sm text-red-500">{errors.restaurantEmail.message}</p>
                      )}
                    </div>
                  </div>
                  
                  <Button 
                    type="submit" 
                    className="w-full bg-food-600 hover:bg-food-700"
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? "Registering..." : "Register Restaurant"}
                  </Button>
                </div>
              )}
            </form>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <div className="text-center text-sm text-gray-600">
              By registering, you agree to our{" "}
              <Link to="/terms" className="text-food-600 hover:text-food-800 font-medium">
                Terms of Service
              </Link>{" "}
              and{" "}
              <Link to="/privacy" className="text-food-600 hover:text-food-800 font-medium">
                Privacy Policy
              </Link>
            </div>
          </CardFooter>
        </Card>
      </div>
    </Layout>
  );
};

export default RestaurantPartner;
