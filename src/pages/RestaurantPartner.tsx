
import React, { useState } from 'react';
import { Layout } from '@/components/layout/Layout';
import { useAuth } from '@/hooks/use-auth';
import { Navigate, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';
import { restaurantAPI } from '@/services/api';
import { UserRole } from '@/types/models';

type RestaurantFormData = {
  name: string;
  description: string;
  category: string;
  phone: string;
  email?: string;
  address: string;
  openingHours: string;
  logo: string;
  photo: string;
  minOrderAmount?: number;
  deliveryFee?: number;
};

const RestaurantPartner = () => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  
  const { register, handleSubmit, setValue, watch, formState: { errors } } = useForm<RestaurantFormData>({
    defaultValues: {
      category: 'fast-food',
      minOrderAmount: 0,
      deliveryFee: 0,
    }
  });

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: '/partner' }} />;
  }

  // Check if user is a restaurant owner
  if (user?.role !== "RESTAURANTE") {
    return (
      <Layout>
        <div className="container mx-auto py-12">
          <Card className="max-w-2xl mx-auto">
            <CardHeader>
              <CardTitle>Restaurant Partner Program</CardTitle>
              <CardDescription>
                You need a restaurant owner account to register your restaurant.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <p className="mb-4">
                Your current account is registered as a customer. Please create a new account as a restaurant owner.
              </p>
              <div className="flex space-x-4">
                <Button variant="outline" onClick={() => navigate('/')}>
                  Go back to home
                </Button>
                <Button onClick={() => navigate('/signup')}>
                  Sign up as restaurant owner
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </Layout>
    );
  }

  const onSubmit = async (data: RestaurantFormData) => {
    try {
      setSubmitting(true);
      
      const restaurantData = {
        ...data,
        ownerId: user.id,
        coverImage: data.photo, // Add coverImage property, mapping it from photo
      };
      
      await restaurantAPI.create(restaurantData);
      
      toast.success('Restaurant registered successfully!');
      navigate('/dashboard');
    } catch (error) {
      console.error('Error registering restaurant:', error);
      toast.error('Failed to register restaurant. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCategoryChange = (value: string) => {
    setValue('category', value);
  };

  return (
    <Layout>
      <div className="container mx-auto py-8">
        <h1 className="text-3xl font-bold mb-6">Register Your Restaurant</h1>
        
        <div className="max-w-4xl mx-auto">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* Basic Information */}
            <Card>
              <CardHeader>
                <CardTitle>Basic Information</CardTitle>
                <CardDescription>
                  Tell us about your restaurant. This information will be displayed to customers.
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="name">Restaurant Name *</Label>
                    <Input
                      id="name"
                      placeholder="e.g. Joe's Pizza"
                      {...register('name', { required: 'Restaurant name is required' })}
                    />
                    {errors.name && <p className="text-red-500 text-sm">{errors.name.message}</p>}
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="email">Contact Email</Label>
                    <Input
                      id="email"
                      type="email"
                      placeholder="e.g. contact@restaurant.com"
                      {...register('email')}
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="phone">Phone Number *</Label>
                    <Input
                      id="phone"
                      placeholder="e.g. (555) 123-4567"
                      {...register('phone', { required: 'Phone number is required' })}
                    />
                    {errors.phone && <p className="text-red-500 text-sm">{errors.phone.message}</p>}
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="category">Restaurant Type *</Label>
                    <Select
                      defaultValue="fast-food"
                      onValueChange={handleCategoryChange}
                    >
                      <SelectTrigger id="category">
                        <SelectValue placeholder="Select type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="fast-food">Fast Food</SelectItem>
                        <SelectItem value="fine-dining">Fine Dining</SelectItem>
                        <SelectItem value="cafe">Café</SelectItem>
                        <SelectItem value="italian">Italian</SelectItem>
                        <SelectItem value="mexican">Mexican</SelectItem>
                        <SelectItem value="asian">Asian</SelectItem>
                        <SelectItem value="dessert">Dessert</SelectItem>
                        <SelectItem value="other">Other</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="address">Address *</Label>
                  <Textarea
                    id="address"
                    placeholder="Full address"
                    {...register('address', { required: 'Address is required' })}
                    className="min-h-[80px]"
                  />
                  {errors.address && <p className="text-red-500 text-sm">{errors.address.message}</p>}
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="description">Description *</Label>
                  <Textarea
                    id="description"
                    placeholder="Tell customers about your restaurant, cuisine, specialties, etc."
                    {...register('description', { 
                      required: 'Description is required',
                      minLength: { value: 20, message: 'Description should be at least 20 characters' }
                    })}
                    className="min-h-[120px]"
                  />
                  {errors.description && <p className="text-red-500 text-sm">{errors.description.message}</p>}
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="logo">Opening Hours</Label>
                  <Input
                    id="logo"
                    placeholder="The hours that your restaurant is open. Optional"
                    {...register('openingHours')}
                  />
                  {errors.logo && <p className="text-red-500 text-sm">{errors.logo.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="logo">Logo URL *</Label>
                  <Input
                    id="logo"
                    placeholder="URL to your restaurant logo"
                    {...register('logo', { required: 'Logo URL is required' })}
                  />
                  {errors.logo && <p className="text-red-500 text-sm">{errors.logo.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="photo">Foto URL *</Label>
                  <Input
                    id="photo"
                    placeholder="URL to your restaurant photo"
                    {...register('photo', { required: 'Photo URL is required' })}
                  />
                  {errors.photo && <p className="text-red-500 text-sm">{errors.photo.message}</p>}
                </div>
              </CardContent>
            </Card>
            
            {/* Delivery Settings */}
            <Card>
              <CardHeader>
                <CardTitle>Delivery Settings</CardTitle>
                <CardDescription>
                  Configure your delivery options
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="minOrderAmount">Minimum Order Amount ($)</Label>
                    <Input
                      id="minOrderAmount"
                      type="number"
                      step="0.01"
                      min="0"
                      {...register('minOrderAmount', { 
                        valueAsNumber: true,
                        min: { value: 0, message: 'Minimum order amount cannot be negative' }
                      })}
                    />
                    {errors.minOrderAmount && <p className="text-red-500 text-sm">{errors.minOrderAmount.message}</p>}
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="deliveryFee">Delivery Fee ($)</Label>
                    <Input
                      id="deliveryFee"
                      type="number"
                      step="0.01"
                      min="0"
                      {...register('deliveryFee', { 
                        valueAsNumber: true,
                        min: { value: 0, message: 'Delivery fee cannot be negative' }
                      })}
                    />
                    {errors.deliveryFee && <p className="text-red-500 text-sm">{errors.deliveryFee.message}</p>}
                  </div>
                </div>
              </CardContent>
            </Card>
            
            <div className="flex justify-end space-x-4">
              <Button 
                type="button" 
                variant="outline" 
                onClick={() => navigate(-1)}
                disabled={submitting}
              >
                Cancel
              </Button>
              <Button 
                type="submit"
                className="bg-food-600 hover:bg-food-700"
                disabled={submitting}
              >
                {submitting ? 'Submitting...' : 'Register Restaurant'}
              </Button>
            </div>
          </form>
        </div>
      </div>
    </Layout>
  );
};

export default RestaurantPartner;
