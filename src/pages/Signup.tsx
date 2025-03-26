
import React from "react";
import { Layout } from "@/components/layout/Layout";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/contexts/AuthContext";
import { useForm } from "react-hook-form";
import { toast } from "@/lib/toast";

interface SignupFormValues {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  phone?: string;
  address?: string;
}

const Signup = () => {
  const navigate = useNavigate();
  const { register: registerUser, isAuthenticated } = useAuth();
  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm<SignupFormValues>();
  
  const password = React.useRef({});
  password.current = watch("password", "");

  React.useEffect(() => {
    if (isAuthenticated) {
      navigate("/");
    }
  }, [isAuthenticated, navigate]);

  const onSubmit = async (data: SignupFormValues) => {
    try {
      const { confirmPassword, ...userData } = data;
      await registerUser({
        ...userData,
        role: "customer"
      });
      toast.success("Account created successfully!");
      navigate("/");
    } catch (error) {
      console.error("Signup error:", error);
      toast.error("Failed to create account. Please try again.");
    }
  };

  return (
    <Layout>
      <div className="max-w-md mx-auto px-4 sm:px-6 py-12">
        <Card>
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold text-center">Create an account</CardTitle>
            <CardDescription className="text-center">
              Enter your information to create an account
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
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
              
              <div className="space-y-2">
                <Label htmlFor="phone">Phone (optional)</Label>
                <Input 
                  id="phone" 
                  placeholder="+1 (555) 123-4567" 
                  {...register("phone")}
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="address">Address (optional)</Label>
                <Input 
                  id="address" 
                  placeholder="123 Main St, City, Country" 
                  {...register("address")}
                />
              </div>
              
              <Button 
                type="submit" 
                className="w-full bg-food-600 hover:bg-food-700"
                disabled={isSubmitting}
              >
                {isSubmitting ? "Creating account..." : "Create Account"}
              </Button>
            </form>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <div className="text-center text-sm text-gray-600">
              Already have an account?{" "}
              <Link to="/login" className="text-food-600 hover:text-food-800 font-medium">
                Login
              </Link>
            </div>
          </CardFooter>
        </Card>
      </div>
    </Layout>
  );
};

export default Signup;
