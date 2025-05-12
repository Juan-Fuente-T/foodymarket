
import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { 
  Dialog, 
  DialogContent, 
  DialogHeader, 
  DialogTitle,
  DialogFooter
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { 
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { FileUpload } from '@/components/ui/file-upload';
import { Product, Category } from '@/types/models';
import { toast } from 'sonner';
import { uploadFileToSupabase } from '@/lib/supabaseStorage';
import { useAuth } from '@/hooks/use-auth';

interface CategoryInfo {
  id: string | number;
  name: string;
}

interface ProductEditModalProps {
  product: Product | null;
  categories: CategoryInfo[];
  isOpen: boolean;
  onClose: () => void;
  onSave: (product: Product, isNew: boolean) => void;
}
type UploadResult = {
  url: string; // La URL pública de Supabase
  path: string; // La ruta interna del archivo en Supabase (para borrarlo si es necesario)
};

export const ProductEditModal = ({ 
  product, 
  categories = [], 
  isOpen, 
  onClose, 
  onSave
}: ProductEditModalProps) => {
  const { register, handleSubmit, setValue, reset, formState: { errors } } = useForm<Product>();
  const [isActive, setIsActive] = useState(true);
  const [isNewProduct, setIsNewProduct] = useState(false);
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>('');
  const [productImage, setProductImage] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);

  // const { user, isAuthenticated, isLoading } = useAuth();
  const { user } = useAuth();

  useEffect(() => {
    if (product) {
      setValue('name', product.name || '');
      setValue('description', product.description || '');
      setValue('price', product.price || '0');
      setValue('image', product.image || '');
      setValue('quantity', product.quantity || 1);
      setIsActive(product.isActive !== false);
      setIsNewProduct(false);
      setImagePreview(product.image || null);
      
      // Convert the categoryId to string to match the select component expectations
      const initialCategoryId = product.categoryId ? String(product.categoryId) : '';
      // console.log("Setting categoryId for existing product:", initialCategoryId);
      setSelectedCategoryId(initialCategoryId);
    } else {
      reset({
        name: '',
        description: '',
        price: '0',
        image: '',
      });
      setIsActive(true);
      setIsNewProduct(true);
      setImagePreview(null);
      
      // Set default category if available
      const defaultCategoryId = categories.length > 0 ? String(categories[0].id) : '';
      // console.log("Setting default categoryId for new product:", defaultCategoryId);
      setSelectedCategoryId(defaultCategoryId);
    }
  }, [product, categories, setValue, reset]);

  const handleFileSelected = (imageFile: File | null) => {
    setProductImage(imageFile);

    if (!imageFile) {
        setImagePreview(product?.image || null); // Volver a la original o null
    }
  };
  
  const handleFileOptimized = (url: string) => {
    setImagePreview(url);
  };

  const onSubmit = async (data: Product) => {
    if (!user) { toast.error("User not authenticated."); return; } 

    if (!selectedCategoryId) {
      toast.error("Por favor, selecciona una categoría válida.");
      return; 
    }
  
    const submittingToast = toast.loading("Guardando producto..."); 
    let uploadResult: UploadResult | null = null; // Usamos el tipo consistente { url, path } | null
    let uploadAttempted = false;

    try {
        // --- Subida de Imagen (SOLO si se seleccionó una NUEVA) ---
        if (productImage) { // <-- Comprueba si hay un archivo nuevo en el estado
            uploadAttempted = true;
            // console.log("Intentando subir nueva imagen de producto...");
            uploadResult = await uploadFileToSupabase(productImage, 'fotos-productos', user.id);
            if (!uploadResult) {
                // El error ya se muestra en uploadFileToSupabase, lanza para ir al catch
                 throw new Error("Fallo al subir la imagen del producto.");
            }
             // TODO Opcional: Borrar imagen antigua si la subida fue exitosa y se esta editando
             // if (uploadResult && !isNewProduct && product?.image) { /* Lógica borrar */ }
        }
    
    const productData = {
      ...product,
      ...data,
      image: uploadResult ? uploadResult.url : data.image,
      isActive,
      available: isActive, 
      categoryId: selectedCategoryId,
      restaurantId: product?.restaurantId,
    };
     // Quitar campos que no deben enviarse (si EditRestaurantApiPayload fuera más estricto)
     // delete productData.categoryName; // Si el tipo Product lo tiene y la API no lo quiere
 
    const finalProductData = isNewProduct
      ? productData 
      : { ...productData, id: product!.id };
      
    // console.log("Sending product data to save:", finalProductData);
    onSave(finalProductData, isNewProduct);
    toast.success(isNewProduct ? 'Producto Creado' : 'Producto Actualizado', { id: submittingToast });
    onClose();
  }catch (error: any) {
    console.error("Error en onSubmit del producto:", error);
    toast.error(error.message || "Error al guardar el producto.", { id: submittingToast });
    // Aquí NO se hace rollback de Supabase porque onSave es quien confirma la operación final
    // El rollback iría DENTRO de la lógica de onSave si la llamada a la API backend falla DESPUÉS de subir a Supabase.
    // Pero como aquí onSave viene después, si upload falla, ya ha parado.
}
};
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>
            {isNewProduct ? 'Crear Nuevo Producto' : 'Editar Producto'}
          </DialogTitle>
        </DialogHeader>
        
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="name">Nombre</Label>
              <Input
                id="name"
                placeholder="Nombre del producto"
                {...register('name', { required: 'El nombre es obligatorio' })}
              />
              {errors.name && (
                <p className="text-sm text-red-500">{errors.name.message}</p>
              )}
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="price">Precio</Label>
              <Input
                id="price"
                type="number"
                step="0.01"
                placeholder="0.00"
                {...register('price', { 
                  required: 'El precio es obligatorio',
                  min: { value: 0, message: 'El precio debe ser mayor o igual a 0' }
                })}
              />
              {errors.price && (
                <p className="text-sm text-red-500">{errors.price.message}</p>
              )}
            </div>
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="description">Descripción</Label>
            <Textarea
              id="description"
              placeholder="Descripción del producto"
              rows={3}
              {...register('description')}
            />
          </div>
          
          <div className="space-y-2">
            <FileUpload 
              label="Imagen del producto" 
              id="product-image"
              onFileSelected={handleFileSelected}
              onFileOptimized={handleFileOptimized}
              currentImage={imagePreview}
              maxSize={2} // 2MB max
            />
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="category">Categoría</Label>
              <Select 
                value={selectedCategoryId}
                onValueChange={(value) => setSelectedCategoryId(value)} 
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecciona una categoría" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map(category => (
                    <SelectItem key={category.id} value={String(category.id)}>
                      {category.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="quantity">Cantidad</Label>
              <Input
                id="quantity"
                type="number"
                step="1"
                placeholder="1"
                min={1}
                className="w-full h-10"
                {...register('quantity', { 
                  min: { value: 1, message: 'La cantidad debe ser mayor o igual a 1' }
                })}
              />
              {errors.quantity && (
                <p className="text-sm text-red-500">{errors.quantity.message}</p>
              )}
            </div>
          </div>
          
          <div className="flex items-center space-x-2">
            <Switch 
              id="isActive" 
              checked={isActive} 
              onCheckedChange={setIsActive} 
            />
            <Label htmlFor="isActive">Disponible</Label>
          </div>
          
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" className="bg-food-600 hover:bg-food-700">
              {isNewProduct ? 'Crear Producto' : 'Guardar Cambios'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
