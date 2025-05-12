
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, Controller } from 'react-hook-form';
import { restaurantAPI, restaurantCuisinesAPI } from '@/services/api';
import { Restaurant } from '@/types/models';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { FileUpload } from '@/components/ui/file-upload';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { toast } from 'sonner';
import { ArrowLeft, Save } from 'lucide-react';
import { supabase } from '@/lib/supabaseClient';
import { v4 as uuidv4 } from 'uuid';
import { useAuth } from '@/hooks/use-auth'

// --- TIPOS (puedes mantener useForm<Restaurant> si Restaurant incluye todo) ---
// Opcional: Crear un tipo específico si es más limpio
// type EditRestaurantFormData = Omit<Restaurant, 'id' | 'ownerId' | 'createdAt' | 'updatedAt'>; // Ejemplo
type EditRestaurantApiPayload = Partial<Restaurant>;

type UploadResult = {
  url: string;
  path: string;
};
// -------------------------------------------------------------------------

const EditRestaurant = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const [coverImagePreview, setCoverImagePreview] = useState<string>('');
  const [logoImagePreview, setLogoImagePreview] = useState<string>('');
  const [logoFile, setLogoFile] = useState<File | null>(null);
  const [coverImageFile, setCoverImageFile] = useState<File | null>(null);
  const BUCKET_NAME = "fotos-c24-39-t-webapp";
  const TOKEN_STORAGE_KEY = "food_delivery_token";
  
  const { register, handleSubmit, setValue, watch, reset, control, formState: { errors, isDirty } } = useForm<Restaurant>();

  const { data: restaurant, isLoading } = useQuery({
    queryKey: ['restaurant', id],
    queryFn: () => restaurantAPI.getById(id as string),
    enabled: !!id,
  });

  const { data: cuisines } = useQuery<{ id: string; name: string }[]>({
    queryKey: ['restaurantCuisines'],
    queryFn: () => restaurantCuisinesAPI.getAll()
  });

  const { mutate: updateRestaurant, isPending: isUpdating } = useMutation({
    mutationFn: (data: EditRestaurantApiPayload) => restaurantAPI.update(id as string, data),
    onSuccess: () => {
      toast.success('Restaurante actualizado con éxito');
      queryClient.invalidateQueries({ queryKey: ['restaurant', id] });
      queryClient.invalidateQueries({ queryKey: ['userRestaurants'] });
      navigate('/dashboard');
    },
    onError: (error) => {
      toast.error('Error updating restaurant');
      console.error('Error updating restaurant:', error);
    }
  });

  useEffect(() => {
    if (restaurant) { // Modo edición: Rellenar el form con los datos
      // reset({
      //   name: restaurant.name || '',
      //   description: restaurant.description || '',
      //   phone: restaurant.phone || '',
      //   email: restaurant.email || '',
      //   address: restaurant.address || '',
      //   openingHours: restaurant.openingHours || '',
      //   logo: restaurant.logo || '',
      //   coverImage: restaurant.coverImage || '',
      //   cuisineId: restaurant.cuisineId ? restaurant.cuisineId : null,
      //   cuisineName: restaurant.cuisineName ? restaurant.cuisineName : ''
      // });
      reset(restaurant)

      // Handle minOrderAmount and deliveryFee specially since they might be optional
      if (restaurant.minOrderAmount !== undefined) {
        setValue('minOrderAmount', restaurant.minOrderAmount);
      }

      if (restaurant.deliveryFee !== undefined) {
        setValue('deliveryFee', restaurant.deliveryFee);
      }

      setCoverImagePreview(restaurant.coverImage || '');
      setLogoImagePreview(restaurant.logo || restaurant.logoImage || '');
    } else { // Modo creación: Limpiar el form (o poner valores por defecto)
      reset({
        name: '', description: '', phone: '', email: '', address: '',
        openingHours: '', logo: '', coverImage: '', cuisineId: null
      });
    }
  }, [restaurant, reset]);
  async function uploadWithFetch(
    file: File,
    filePath: string, // La ruta interna ej: 'imagenes-logos/3/uuid.webp'
    bucket: string,
    token: string, // Tu access_token del backend
    apiKey: string // Tu clave anon pública de Supabase
  ): Promise<UploadResult | null> { 
    // Construir la URL del endpoint de Supabase Storage
    const supabaseUrl = import.meta.env.VITE_SUPABASE_URL?.replace('/auth/v1', ''); // Quitar /auth/v1 si está presente
    if (!supabaseUrl) {
      toast.error('Supabase URL missing');
      return null;
  }
  const url = `${supabaseUrl}/storage/v1/object/${bucket}/${filePath}`;

    const headers = {
        'Authorization': `Bearer ${token}`,
        'apikey': apiKey,
        'Content-Type': file.type,
        // 'x-upsert': 'false' // Supabase usa 'false' por defecto si no se especifica
    };

    try {
      const response = await fetch(url, { method: 'POST', headers: headers, body: file });

      if (!response.ok) {
          const errorData = await response.json().catch(() => ({ message: response.statusText }));
          throw new Error(errorData.message || `HTTP error ${response.status}`);
      }

      const responseData = await response.json();

      // Si la subida fue bien, obtenemos la URL pública
      const { data: urlData } = supabase.storage.from(bucket).getPublicUrl(filePath);
      if (!urlData?.publicUrl) {
          throw new Error("Upload successful but failed to get public URL");
      }
      return { url: urlData.publicUrl, path: filePath };

  } catch (error: any) {
      console.error('Error en uploadWithFetch:', error);
      toast.error(`File upload failed: ${error.message || 'Unknown error'}`);
      return null;
  }
  }
  const handleLogoFileSelected = (file: File | null) => {
    setLogoFile(file);
    setLogoImagePreview(file ? URL.createObjectURL(file) : (restaurant?.logo || ''));
  };

  const handleCoverImageFileSelected = (file: File | null) => {
    setCoverImageFile(file);
    setCoverImagePreview(file ? URL.createObjectURL(file) : (restaurant?.coverImage || ''));
  };

  const onSubmit = async (data: Restaurant) => {
    if (!user) {
      toast.error("User not authenticated.");
      return;
    }
    if (!id) {
      toast.error("Restaurant ID is missing.");
      return;
    }
    if (data.cuisineId === null || data.cuisineId === undefined) {
      toast.error("Please select a cuisine type.");
      return;
    }
    const token = localStorage.getItem(TOKEN_STORAGE_KEY); 
    const apiKey = import.meta.env.VITE_SUPABASE_ANON_KEY;

    if (!token || !apiKey) {
         toast.error("Information missing. Please log in again.");
         return;
    }

    const submittingToast = toast.loading("Updating restaurant...");
    let logoUploadResult: { url: string; path: string } | null = null;
    let coverImageUploadResult: { url: string; path: string } | null = null;
    let uploadError = false;

    try {
      let logoFilePath: string | null = null;
        let coverImageFilePath: string | null = null;
        if (logoFile) {
          const fileExt = logoFile.name.split('.').pop();
          const uniqueFileName = `${uuidv4()}.${fileExt}`;
          logoFilePath = `imagenes-logos/${user?.id}/${uniqueFileName}`; // Guarda el path
          logoUploadResult = await uploadWithFetch(logoFile, logoFilePath, BUCKET_NAME, token, apiKey);
          if (!logoUploadResult) {
              uploadError = true;
              throw new Error("Upload logo failed.");
          }
            // const uploadResultObj = await uploadFileToSupabase(logoFile, 'imagenes-logos', user.id); // Asume una versión que usa supabase.storage...
            // if (!uploadResultObj) uploadError = true; else logoUploadResult = uploadResultObj;
         
      }
     // --- Subida de Cover Image (si existe y no hubo error previo) ---
     if (coverImageFile && !uploadError) {
      const fileExt = coverImageFile.name.split('.').pop();
      const uniqueFileName = `${uuidv4()}.${fileExt}`;
      coverImageFilePath = `restaurants-coverImages/${user?.id}/${uniqueFileName}`; // Guarda el path
      coverImageUploadResult = await uploadWithFetch(coverImageFile, coverImageFilePath, BUCKET_NAME, token, apiKey);
      // coverImageUploadResult = await uploadFileToSupabase(coverImageFile, "restaurants-coverImages", user.id);
       if (!coverImageUploadResult) {
          uploadError = true;
           throw new Error("Cover image upload failed. Please upload a cover image.");
      }
  }
      const payload: EditRestaurantApiPayload = {
        ...data, // Incluye todo: name, desc, fees, cuisineId, cuisineName, etc.
        // NOTA para el futuro: minOrderAmount y deliveryFee YA son números aquí
        id: parseInt(id),
        logo: logoUploadResult ? logoUploadResult.url : data.logo,
        coverImage: coverImageUploadResult ? coverImageUploadResult.url : data.coverImage,
      };

        await updateRestaurant(payload, {
          onError: (error: any) => {
             const filesToDelete = [];
              // Usamos los paths que guardamos antes de llamar a uploadWithFetch
              if (logoFilePath && logoUploadResult) filesToDelete.push(logoFilePath); // Solo si se intentó y tuvo éxito la subida
              if (coverImageFilePath && coverImageUploadResult) filesToDelete.push(coverImageFilePath); // Solo si se intentó y tuvo éxito la subida
              
              if (filesToDelete.length > 0) {
                supabase.storage.from(BUCKET_NAME).remove(filesToDelete)
                .then(/* ... manejo de éxito/error del borrado ... */)
                .catch(e => console.error("Error en llamada remove de Supabase", e));
              }
              throw new error("Error storing in DB:", error);
          },
          onSuccess: () => {
               toast.success('Restaurant updated successfully!', { id: submittingToast });
               setLogoFile(null);
               setCoverImageFile(null);
               // Navegación y queries ya se invalidan en la definición de useMutation
           }
       });
      //
    } catch (error: any) {
      console.error("Error en onSubmit:", error);
      toast.error(error.message || "Error updating restaurant", { id: submittingToast });
    }
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
                <div className="space-y-4">
                  <FileUpload
                    label="Logo del restaurante"
                    id="restaurant-logo-edit"
                    onFileSelected={handleLogoFileSelected}
                    maxSize={2} // 2MB max
                    accept="image/*"
                    currentImage={logoImagePreview}
                  />
                  <input type="hidden" {...register('logo')} />
                </div>

                <div className="space-y-4">
                  <FileUpload
                    label="Imagen de portada"
                    id="restaurant-cover-edit"
                    onFileSelected={handleCoverImageFileSelected}
                    maxSize={2} // 2MB max
                    accept="image/*"
                    currentImage={coverImagePreview}
                  />
                  <input type="hidden" {...register('coverImage')} />
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