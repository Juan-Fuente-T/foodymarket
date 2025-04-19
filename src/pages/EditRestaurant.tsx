
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { restaurantAPI } from '@/services/api';
import { Restaurant } from '@/types/models';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { 
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { toast } from 'sonner';
import { ArrowLeft, Save } from 'lucide-react';

const CATEGORIES = [
  'Italiana',
  'Mexicana',
  'China',
  'Japonesa',
  'India',
  'Americana',
  'Mediterránea',
  'Vegetariana',
  'Vegana',
  'Mariscos',
  'Parrilla',
  'Pizzería',
  'Hamburguesería',
  'Cafetería',
  'Pastelería',
  'Heladería',
  'Otro'
];

const EditRestaurant = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [coverImagePreview, setCoverImagePreview] = useState<string>('');
  const [logoImagePreview, setLogoImagePreview] = useState<string>('');
  
  const { register, handleSubmit, setValue, watch, formState: { errors, isDirty } } = useForm<Restaurant>();
  
  const { data: restaurant, isLoading } = useQuery({
    queryKey: ['restaurant', id],
    queryFn: () => restaurantAPI.getById(id as string),
    enabled: !!id,
  });
  
  const { mutate: updateRestaurant, isPending: isUpdating } = useMutation({
    mutationFn: (data: Restaurant) => restaurantAPI.update(id as string, data),
    onSuccess: () => {
      toast.success('Restaurante actualizado con éxito');
      queryClient.invalidateQueries({ queryKey: ['restaurant', id] });
      queryClient.invalidateQueries({ queryKey: ['userRestaurants'] });
      navigate('/dashboard');
    },
    onError: (error) => {
      toast.error('Error al actualizar el restaurante');
      console.error('Error updating restaurant:', error);
    }
  });
  
  useEffect(() => {
    if (restaurant) {
      const fields = [
        'name', 'description', 'address', 'phone', 
        'email', 'category', 'openingHours', 'coverImage', 
        'logo'
      ];
      
      fields.forEach(field => {
        if (field in restaurant) {
          setValue(field as keyof Restaurant, restaurant[field as keyof Restaurant]);
        }
      });
      
      // Handle minOrderAmount and deliveryFee specially since they might be optional
      if (restaurant.minOrderAmount !== undefined) {
        setValue('minOrderAmount', restaurant.minOrderAmount);
      }
      
      if (restaurant.deliveryFee !== undefined) {
        setValue('deliveryFee', restaurant.deliveryFee);
      }
      
      // Use logo for logoImagePreview if logoImage doesn't exist
      setCoverImagePreview(restaurant.coverImage || '');
      setLogoImagePreview(restaurant.logo || restaurant.logoImage || '');
    }
  }, [restaurant, setValue]);
  
  const watchCoverImage = watch('coverImage');
  const watchLogo = watch('logo');
  
  useEffect(() => {
    if (watchCoverImage) setCoverImagePreview(watchCoverImage);
  }, [watchCoverImage]);
  
  useEffect(() => {
    if (watchLogo) setLogoImagePreview(watchLogo);
  }, [watchLogo]);
  
  const onSubmit = (data: Restaurant) => {
    const updatedData = {
      ...restaurant,
      ...data
    };
    
    // Ensure numeric types
    if (data.minOrderAmount !== undefined) {
      updatedData.minOrderAmount = parseFloat(data.minOrderAmount.toString() || '0');
    }
    
    if (data.deliveryFee !== undefined) {
      updatedData.deliveryFee = parseFloat(data.deliveryFee.toString() || '0');
    }
    
    updateRestaurant(updatedData);
  };
  
  if (isLoading) {
    return (
      <Layout>
        <div className="max-w-5xl mx-auto p-8">
          <div className="animate-pulse space-y-4">
            <div className="h-8 bg-gray-200 w-1/3 mb-6"></div>
            <div className="h-64 bg-gray-200 rounded-xl mb-6"></div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="h-12 bg-gray-200 rounded-md"></div>
              <div className="h-12 bg-gray-200 rounded-md"></div>
            </div>
          </div>
        </div>
      </Layout>
    );
  }
  
  if (!restaurant) {
    return (
      <Layout>
        <div className="max-w-5xl mx-auto p-16 text-center">
          <h1 className="text-2xl font-bold mb-4">Restaurante no encontrado</h1>
          <Button asChild className="bg-food-600 hover:bg-food-700">
            <Link to="/dashboard">Volver al Dashboard</Link>
          </Button>
        </div>
      </Layout>
    );
  }
  
  return (
    <Layout>
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <Link to="/dashboard" className="inline-flex items-center text-food-600 hover:text-food-800 mb-2">
              <ArrowLeft className="h-4 w-4 mr-1" />
              Volver al Dashboard
            </Link>
            <h1 className="text-3xl font-bold text-gray-900">Editar Restaurante</h1>
          </div>
        </div>
        
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-8">
            <Card>
              <CardHeader>
                <CardTitle>Información Básica</CardTitle>
                <CardDescription>
                  Información general de tu restaurante
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="name">Nombre del Restaurante</Label>
                    <Input
                      id="name"
                      placeholder="Nombre del restaurante"
                      {...register('name', { required: 'El nombre es obligatorio' })}
                    />
                    {errors.name && (
                      <p className="text-sm text-red-500">{errors.name.message}</p>
                    )}
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="category">Categoría</Label>
                    <Select
                      onValueChange={(value) => setValue('category', value)}
                      defaultValue={restaurant.category}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Selecciona una categoría" />
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
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="description">Descripción</Label>
                  <Textarea
                    id="description"
                    placeholder="Describe tu restaurante"
                    rows={4}
                    {...register('description')}
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="address">Dirección</Label>
                  <Input
                    id="address"
                    placeholder="Dirección completa"
                    {...register('address', { required: 'La dirección es obligatoria' })}
                  />
                  {errors.address && (
                    <p className="text-sm text-red-500">{errors.address.message}</p>
                  )}
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="phone">Teléfono</Label>
                    <Input
                      id="phone"
                      placeholder="Teléfono de contacto"
                      {...register('phone')}
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      placeholder="Email de contacto"
                      {...register('email')}
                    />
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="openingHours">Horario de Apertura</Label>
                  <Input
                    id="openingHours"
                    placeholder="Ej: Lun-Vie: 10:00-22:00, Sáb-Dom: 11:00-23:00"
                    {...register('openingHours')}
                  />
                </div>
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader>
                <CardTitle>Imágenes</CardTitle>
                <CardDescription>
                  Imágenes de portada y logo de tu restaurante
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="space-y-2">
                  <Label htmlFor="coverImage">Imagen de Portada (URL)</Label>
                  <Input
                    id="coverImage"
                    placeholder="https://ejemplo.com/imagen.jpg"
                    {...register('coverImage')}
                  />
                  {coverImagePreview && (
                    <div className="mt-4">
                      <p className="text-sm text-gray-500 mb-2">Vista previa:</p>
                      <img 
                        src={coverImagePreview} 
                        alt="Portada" 
                        className="w-full h-48 object-cover rounded-md"
                        onError={() => setCoverImagePreview('')}
                      />
                    </div>
                  )}
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="logo">Logo (URL)</Label>
                  <Input
                    id="logo"
                    placeholder="https://ejemplo.com/logo.jpg"
                    {...register('logo')}
                  />
                  {logoImagePreview && (
                    <div className="mt-4">
                      <p className="text-sm text-gray-500 mb-2">Vista previa:</p>
                      <img 
                        src={logoImagePreview} 
                        alt="Logo" 
                        className="w-24 h-24 object-cover rounded-md"
                        onError={() => setLogoImagePreview('')}
                      />
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader>
                <CardTitle>Información de Entrega</CardTitle>
                <CardDescription>
                  Configura los detalles de entrega
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="minOrderAmount">Pedido Mínimo (€)</Label>
                    <Input
                      id="minOrderAmount"
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="0.00"
                      {...register('minOrderAmount', {
                        setValueAs: value => parseFloat(value || '0'),
                      })}
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="deliveryFee">Costo de Entrega (€)</Label>
                    <Input
                      id="deliveryFee"
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="0.00"
                      {...register('deliveryFee', {
                        setValueAs: value => parseFloat(value || '0'),
                      })}
                    />
                  </div>
                </div>
              </CardContent>
            </Card>
            
            <Card>
              <CardFooter className="flex justify-between">
                <Button type="button" variant="outline" asChild>
                  <Link to="/dashboard">Cancelar</Link>
                </Button>
                <Button 
                  type="submit" 
                  className="bg-food-600 hover:bg-food-700"
                  disabled={isUpdating || !isDirty}
                >
                  {isUpdating ? (
                    <>
                      <div className="animate-spin mr-2 h-4 w-4 border-t-2 border-b-2 border-white rounded-full"></div>
                      Guardando...
                    </>
                  ) : (
                    <>
                      <Save className="mr-2 h-4 w-4" />
                      Guardar Cambios
                    </>
                  )}
                </Button>
              </CardFooter>
            </Card>
          </div>
        </form>
      </div>
    </Layout>
  );
};

export default EditRestaurant;
