
import React from "react";
import { Layout } from "@/components/layout/Layout";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useAuth } from "@/contexts/AuthContext";
import { useForm } from "react-hook-form";
import { toast } from "@/lib/toast";
import { restaurantAPI } from "@/services/api";
import { User } from "@/types/models";

interface PartnerFormValues {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  phone: string;
  restaurantName: string;
  restaurantDescription: string;
  restaurantAddress: string;
  restaurantPhone: string;
  restaurantEmail: string;
}

const RestaurantPartner = () => {
  const navigate = useNavigate();
  const { register: registerUser, isAuthenticated, user } = useAuth();
  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm<PartnerFormValues>();
  
  const password = React.useRef({});
  password.current = watch("password", "");

  const onSubmit = async (data: PartnerFormValues) => {
    try {
      // First register the owner if not authenticated
      let ownerId = user?.id;
      
      if (!isAuthenticated) {
        const { confirmPassword, restaurantName, restaurantDescription, restaurantAddress, 
               restaurantPhone, restaurantEmail, ...userData } = data;
        
        // Handle user registration
        const newUser = await registerUser({
          ...userData,
          role: "owner"
        }) as User; // Cast to User to ensure TypeScript knows it has an id
        
        if (!newUser || !newUser.id) {
          throw new Error("Failed to register user");
        }
        
        ownerId = newUser.id;
      }
      
      if (!ownerId) {
        throw new Error("Owner ID is required");
      }
      
      // Then create the restaurant
      await restaurantAPI.create({
        name: data.restaurantName,
        description: data.restaurantDescription,
        address: data.restaurantAddress,
        phone: data.restaurantPhone,
        email: data.restaurantEmail,
        ownerId: ownerId,
        logo: "https://via.placeholder.com/150",
        coverImage: "https://via.placeholder.com/800x400",
        rating: 0,
        reviewCount: 0,
        categories: [],
        openingHours: "9 AM - 9 PM"
      });
      
      toast.success("Restaurant registered successfully!");
      navigate("/dashboard");
    } catch (error) {
      console.error("Registration error:", error);
      toast.error("Failed to register restaurant. Please try again.");
    }
  };

  return (
    <Layout>
      <div className="max-w-3xl mx-auto px-4 sm:px-6 py-12">
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
                <>
                  <div className="bg-gray-50 p-4 rounded-lg space-y-4">
                    <h3 className="font-medium text-lg">Owner Information</h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="name">Full Name</Label>
                        <Input 
                          id="name" 
                          placeholder="John Doe" 
                          {...register("name", { 
                            required: "Name is required" 
                          })}
                        />
                        {errors.name && (
                          <p className="text-sm text-red-500">{errors.name.message}</p>
                        )}
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="email">Email</Label>
                        <Input 
                          id="email" 
                          type="email" 
                          placeholder="name@example.com" 
                          {...register("email", { 
                            required: "Email is required",
                            pattern: {
                              value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                              message: "Invalid email address"
                            }
                          })}
                        />
                        {errors.email && (
                          <p className="text-sm text-red-500">{errors.email.message}</p>
                        )}
                      </div>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="password">Password</Label>
                        <Input 
                          id="password" 
                          type="password" 
                          {...register("password", { 
                            required: "Password is required",
                            minLength: {
                              value: 6,
                              message: "Password must be at least 6 characters"
                            }
                          })}
                        />
                        {errors.password && (
                          <p className="text-sm text-red-500">{errors.password.message}</p>
                        )}
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="confirmPassword">Confirm Password</Label>
                        <Input 
                          id="confirmPassword" 
                          type="password" 
                          {...register("confirmPassword", { 
                            required: "Please confirm your password",
                            validate: value => value === password.current || "Passwords do not match"
                          })}
                        />
                        {errors.confirmPassword && (
                          <p className="text-sm text-red-500">{errors.confirmPassword.message}</p>
                        )}
                      </div>
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="phone">Phone</Label>
                      <Input 
                        id="phone" 
                        placeholder="+1 (555) 123-4567" 
                        {...register("phone", { 
                          required: "Phone number is required" 
                        })}
                      />
                      {errors.phone && (
                        <p className="text-sm text-red-500">{errors.phone.message}</p>
                      )}
                    </div>
                  </div>
                </>
              )}
              
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
              </div>
              
              <Button 
                type="submit" 
                className="w-full bg-food-600 hover:bg-food-700"
                disabled={isSubmitting}
              >
                {isSubmitting ? "Registering..." : "Register Restaurant"}
              </Button>
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
