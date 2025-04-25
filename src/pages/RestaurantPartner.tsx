
import React, { useState } from 'react';
import { Layout } from '@/components/layout/Layout';
import { useAuth } from '@/hooks/use-auth';
import { Navigate, useNavigate } from 'react-router-dom';
import { Controller, useForm } from 'react-hook-form';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';
import { restaurantAPI, restaurantCuisinesAPI } from '@/services/api';
import { FileUpload } from '@/components/ui/file-upload';
import { useQuery } from '@tanstack/react-query';
import { supabase } from '@/lib/supabaseClient';
// import { supabase } from '../lib/supabaseClient';
import { v4 as uuidv4 } from 'uuid';

type RestaurantFormData = {
  restaurantId: number;
  name: string;
  description: string;
  phone: string;
  email: string;
  address: string;
  openingHours: string;
  logo: string;
  coverImage: string;
  cuisineId: number;
  cuisineName: string;
  minOrderAmount?: number;
  deliveryFee?: number;
};

type RestaurantApiPayload = Omit<RestaurantFormData, 'cuisineId'> & {
  cuisineId: number; // Aseguramos tipo number
  logo: string | null;
  coverImage: string | null;
  ownerId: string | null; 
};

const RestaurantPartner = () => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [logoFile, setLogoFile] = useState<File | null>(null);
  const [coverImageFile, setCoverImageFile] = useState<File | null>(null);
  const BUCKET_NAME = "fotos-c24-39-t-webapp";

  const { register, handleSubmit, setValue, control, watch, setError, formState: { errors } } = useForm<RestaurantFormData>({
    defaultValues: {
      // cuisineName: 'Familiar',
      cuisineId: null,
      minOrderAmount: 0,
      deliveryFee: 0,
    }
  });
  const { data: cuisines } = useQuery<{ id: string; name: string }[]>({
    queryKey: ['restaurantCuisines'],
    queryFn: () => restaurantCuisinesAPI.getAll()
  });

  const uploadFileToSupabase = async (file: File, folder: string, userId: string | number): Promise<string | null> => {
    // Crear un nombre de archivo único para evitar colisiones
    const fileExt = file.name.split('.').pop();
    const uniqueFileName = `${uuidv4()}.${fileExt}`;
    const filePath = `${folder}/${userId}/${uniqueFileName}`;
  
    console.log(`Subiendo ${file.name} a ${BUCKET_NAME}/${filePath}...`);  

    try {
      const { data, error: uploadError } = await supabase.storage
        .from(BUCKET_NAME)
        .upload(filePath, file, {
          cacheControl: '3600',
          upsert: false, // Poner 'true' si se quisiera reemplazar con el mismo nombre exacto (menos probable con UUID)
        });
  
      if (uploadError) {
        console.error('Error al subir el archivo:', uploadError);
        toast.error(`Fallo al subir ${folder === 'logos' ? 'logo' : 'imagen de portada'}: ${uploadError.message}`);
        return null;
      }
  
      console.log('Subida exitosa:', data);
  
      // Obtener la URL pública
      const { data: urlData } = supabase.storage
        .from(BUCKET_NAME)
        .getPublicUrl(filePath);
  
      if (!urlData || !urlData.publicUrl) {
          console.error('Error al obtener la URL pública para:', filePath);
          toast.error(`Fallo al obtener la URL pública para ${folder === 'logos' ? 'logo' : 'imagen de portada'}.`);
          // Considerar borrar el archivo si la URL falla? (Opcional)
          // await supabase.storage.from(BUCKET_NAME).remove([filePath]);
          return null;
      }
  
      console.log('URL Pública:', urlData.publicUrl);
      return urlData.publicUrl;
  
    } catch (error) {
      console.error('Error inesperado durante la subida:', error);
      toast.error('Error inesperado durante la subida del archivo.');
      return null;
    }
  };
  
  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: '/partner' }} />;
  }


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

  const handleLogoFileSelected = (file: File) => {
    setLogoFile(file);
};
  const handleCoverImageFileSelected = (file: File) => {
    setCoverImageFile(file);
  };

  const onSubmit = async (data: RestaurantFormData) => {
    if (!user) {
      toast.error("User not authenticated.");
      return;
  }
  if (!logoFile) {
      toast.error('Por favor, sube un logo para el restaurante.');
      setError(
        'logo', // Nombre del campo registrado en RHF (el del input hidden)
        {
            type: 'manual', // Tipo de error (puede ser 'required' o uno proio a elegir)
            message: 'The logo is required.'
        },
        {
            shouldFocus: true // Opcional: Intenta poner el foco en el campo (puede que no funcione bien con FileUpload)
        }
    );
      return;
  }
  if (data.cuisineId === null || data.cuisineId === undefined) {
      toast.error('Por favor, selecciona un tipo de cocina.');
      return;
  }
  setSubmitting(true);
      let logoUrl: string | null = null;
      let coverImageUrl: string | null = null;
      let uploadError = false;
      try {
        console.log("Intentando subir logo...");
        logoUrl = await uploadFileToSupabase(logoFile, 'imagenes-logos', user.id);
        if (!logoUrl) uploadError = true;

        if (coverImageFile && !uploadError) {
          console.log("Intentando subir imagen de portada...");
          coverImageUrl = await uploadFileToSupabase(coverImageFile, 'restaurants-coverImages', user.id);
          if (!coverImageUrl) uploadError = true;
        }

        if (uploadError) {
            console.error("Envío detenido debido a error(es) en la subida.");
            toast.error("Fallo al subir una o más imágenes. Inténtalo de nuevo.");
            setSubmitting(false);
            return;
        }

        const restaurantApiPayload: RestaurantApiPayload = {
          ...data, // name, description, phone, email, address, openingHours, minOrderAmount, deliveryFee
          logo: logoUrl,
          coverImage: coverImageUrl,
          ownerId: user.id
        };

        console.log("Enviando datos al backend:", JSON.stringify(restaurantApiPayload, null, 2));

        await restaurantAPI.create(restaurantApiPayload);

      toast.success('Restaurant registered successfully!');
      navigate('/dashboard');
    } catch (error) {
      console.error('Error registering restaurant:', error);
      const errorMessage = error?.response?.data?.message || error?.message || 'Failed to register restaurant.';
      toast.error(`Error trying to register restaurant: ${errorMessage}. Please try again.`);
    } finally {
      setSubmitting(false);
    }
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
                    <Label htmlFor="cuisineSelect">Categoría</Label>
                    <Controller
                      name="cuisineId"
                      control={control}
                      rules={{ required: 'La categoria es obligatoria' }}
                      render={({ field, fieldState: { error } }) => (
                        <>
                          <Select
                            value={field.value !== null && field.value !== undefined ? String(field.value) : undefined}
                            onValueChange={(valueAsString) => {
                              const numericValue = parseInt(valueAsString, 10);
                              field.onChange(isNaN(numericValue) ? null : numericValue);
                            }}
                          >
                            <SelectTrigger id="cuisineSelect" ref={field.ref} onBlur={field.onBlur}>
                              <SelectValue placeholder="Selecciona un tipo de cocina" />
                            </SelectTrigger>
                            <SelectContent>
                              {cuisines?.map(cuisine => (
                                <SelectItem key={cuisine.id} value={String(cuisine.id)}>
                                  {cuisine.name}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                          {error && (
                            <p className="text-sm font-medium text-destructive">{error.message}</p>
                          )}
                        </>
                      )}
                    />
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
                  <Label htmlFor="openingHours">Opening Hours</Label>
                  <Input
                    id="openingHours"
                    placeholder="The hours that your restaurant is open. Optional"
                    {...register('openingHours')}
                  />
                </div>
              </CardContent>
            </Card>

            {/* Restaurant Images */}
            <Card>
              <CardHeader>
                <CardTitle>Restaurant Images</CardTitle>
                <CardDescription>
                  Upload images for your restaurant
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-4">
                  <div>
                    <FileUpload
                      label="Restaurant Logo *"
                      id="restaurant-logo"
                      onFileSelected={handleLogoFileSelected}
                      maxSize={2} // 2MB max
                      accept="image/*"
                    />
                    {/* <input type="hidden" {...register('logo', { required: 'Logo is required' })} /> */}
                    <input type="hidden" {...register('logo')} />
                    {errors.logo && <p className="text-red-500 text-sm">{errors.logo.message}</p>}
                  </div>

                  <div>
                    <FileUpload
                      label="Cover Image (Optional)"
                      id="restaurant-cover"
                      onFileSelected={handleCoverImageFileSelected}
                      maxSize={2} // 2MB max
                      accept="image/*"
                    />
                    <input type="hidden" {...register('coverImage')} />
                    {/* <input type="hidden" {...register('coverImage', { required: 'Cover image is required' })} />
                    {errors.coverImage && <p className="text-red-500 text-sm">{errors.coverImage.message}</p>} */}
                  </div>
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
function setError(arg0: string, arg1: {
  type: string; // Tipo de error (puede ser 'required' o uno proio a elegir)
  message: string;
}, arg2: {
  shouldFocus: boolean; // Opcional: Intenta poner el foco en el campo (puede que no funcione bien con FileUpload)
}) {
  throw new Error('Function not implemented.');
}

