
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Layout } from '@/components/layout/Layout';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/use-auth';
import { Card } from '@/components/ui/card';
import { toast } from 'sonner';
import { UserRole } from '@/types/models';

type FormData = {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: UserRole;
};

const Signup = () => {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const { register, handleSubmit, watch, formState: { errors } } = useForm<FormData>({
    defaultValues: {
      role: 'CLIENTE'
    }
  });
  const [isLoading, setIsLoading] = useState(false);

  const onSubmit = async (data: FormData) => {
    if (data.password !== data.confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    try {
      setIsLoading(true);
      await registerUser({
        name: data.name,
        email: data.email,
        password: data.password,
        role: data.role
      });
      toast.success('Account created successfully! Redirecting to login...');
      navigate('/login');
    } catch (error) {
      console.error('Registration error:', error);
      toast.error('Failed to create account. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Layout>
      <div className="min-h-screen py-12 flex items-center justify-center">
        <Card className="w-full max-w-md p-8 rounded-xl shadow-md">
          <div className="mb-8 text-center">
            <h1 className="text-2xl font-bold">Create your account</h1>
            <p className="text-gray-600 mt-2">
              Join our food delivery platform to order delicious meals or register your restaurant
            </p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="name">Full Name</Label>
              <Input
                id="name"
                type="text"
                placeholder="Enter your full name"
                {...register('name', { required: 'Name is required' })}
              />
              {errors.name && (
                <p className="text-red-500 text-sm">{errors.name.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="Enter your email"
                {...register('email', {
                  required: 'Email is required',
                  pattern: {
                    value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                    message: 'Invalid email address',
                  },
                })}
              />
              {errors.email && (
                <p className="text-red-500 text-sm">{errors.email.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                placeholder="Create a password"
                {...register('password', {
                  required: 'Password is required',
                  minLength: {
                    value: 6,
                    message: 'Password must be at least 6 characters',
                  },
                })}
              />
              {errors.password && (
                <p className="text-red-500 text-sm">{errors.password.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirm Password</Label>
              <Input
                id="confirmPassword"
                type="password"
                placeholder="Confirm your password"
                {...register('confirmPassword', {
                  required: 'Please confirm your password',
                  validate: (value) =>
                    value === watch('password') || 'Passwords do not match',
                })}
              />
              {errors.confirmPassword && (
                <p className="text-red-500 text-sm">
                  {errors.confirmPassword.message}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label>I am registering as a:</Label>
              <RadioGroup defaultValue="CLIENTE" className="flex space-x-4">
                <div className="flex items-center space-x-2">
                  <RadioGroupItem
                    value="CLIENTE"
                    id="customer"
                    {...register('role')}
                  />
                  <Label htmlFor="customer" className="cursor-pointer">
                    Customer
                  </Label>
                </div>
                <div className="flex items-center space-x-2">
                  <RadioGroupItem
                    value="RESTAURANTE"
                    id="restaurant"
                    {...register('role')}
                  />
                  <Label htmlFor="restaurant" className="cursor-pointer">
                    Restaurant Owner
                  </Label>
                </div>
              </RadioGroup>
            </div>

            <Button
              type="submit"
              className="w-full bg-food-600 hover:bg-food-700"
              disabled={isLoading}
            >
              {isLoading ? 'Creating Account...' : 'Create Account'}
            </Button>

            <div className="text-center text-sm text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="text-food-600 hover:underline">
                Sign in
              </Link>
            </div>
          </form>
        </Card>
      </div>
    </Layout>
  );
};

export default Signup;
