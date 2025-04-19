
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
import { Product, Category } from '@/types/models';
import { toast } from 'sonner';

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

  useEffect(() => {
    console.log("Categories in modal:", categories);
    console.log("Product in modal:", product);
    
    if (product) {
      setValue('name', product.name || '');
      setValue('description', product.description || '');
      setValue('price', product.price || 0);
      setValue('image', product.image || '');
      setValue('quantity', product.quantity || 1);
      setIsActive(product.isActive !== false);
      setIsNewProduct(false);
      
      // Convert the categoryId to string to match the select component expectations
      const initialCategoryId = product.categoryId ? String(product.categoryId) : '';
      console.log("Setting categoryId for existing product:", initialCategoryId);
      setSelectedCategoryId(initialCategoryId);
    } else {
      reset({
        name: '',
        description: '',
        price: 0,
        image: '',
      });
      setIsActive(true);
      setIsNewProduct(true);
      
      // Set default category if available
      const defaultCategoryId = categories.length > 0 ? String(categories[0].id) : '';
      console.log("Setting default categoryId for new product:", defaultCategoryId);
      setSelectedCategoryId(defaultCategoryId);
    }
  }, [product, categories, setValue, reset]);

  const onSubmit = (data: Product) => {
    if (!selectedCategoryId) {
      toast.error("Por favor, selecciona una categoría válida.");
      return; 
    }
    
    const productData = {
      ...product,
      ...data,
      isActive,
      categoryId: selectedCategoryId,
    };

    const finalProductData = isNewProduct
      ? productData 
      : { ...productData, id: product!.id };
      
    console.log("Sending product data to save:", finalProductData);
    onSave(finalProductData, isNewProduct);
    onClose();
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
            <Label htmlFor="image">URL de la imagen</Label>
            <Input
              id="image"
              placeholder="https://example.com/imagen.jpg"
              {...register('image')}
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
