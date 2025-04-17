
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
import { Product } from '@/types/models';
import { toast } from 'sonner';

interface ProductEditModalProps {
  product: Product | null;
  categories: string[];
  isOpen: boolean;
  onClose: () => void;
  onSave: (product: Product) => void;
}

export const ProductEditModal = ({ 
  product, 
  categories, 
  isOpen, 
  onClose, 
  onSave
}: ProductEditModalProps) => {
  const { register, handleSubmit, setValue, watch, reset, formState: { errors } } = useForm<Product>();
  const [available, setAvailable] = useState(true);
  const [isNewProduct, setIsNewProduct] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<string>('');

  useEffect(() => {
    if (product) {
      setValue('name', product.name || '');
      setValue('description', product.description || '');
      setValue('price', product.price || 0);
      setValue('image', product.image || '');
      // Use categoryId if available, otherwise fallback to category for compatibility
      setSelectedCategory(product.categoryId || product.category || '');
      setAvailable(product.available !== false);
      setIsNewProduct(false);
    } else {
      reset({
        name: '',
        description: '',
        price: 0,
        image: '',
        categoryId: categories.length > 0 ? categories[0] : '',
      });
      setSelectedCategory(categories.length > 0 ? categories[0] : '');
      setAvailable(true);
      setIsNewProduct(true);
    }
  }, [product, categories, setValue, reset]);

  const onSubmit = (data: Product) => {
    const updatedProduct = {
      ...product,
      ...data,
      available,
      categoryId: selectedCategory, // Use categoryId for the database
      category: selectedCategory,  // Keep category for backward compatibility
      id: product?.id || Date.now().toString(),
    };
    
    onSave(updatedProduct);
    toast.success(isNewProduct ? 'Producto creado con éxito' : 'Producto actualizado con éxito');
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
          
          <div className="space-y-2">
            <Label htmlFor="category">Categoría</Label>
            <Select 
              value={selectedCategory}
              onValueChange={(value) => setSelectedCategory(value)} 
            >
              <SelectTrigger>
                <SelectValue placeholder="Selecciona una categoría" />
              </SelectTrigger>
              <SelectContent>
                {categories.map(category => (
                  <SelectItem key={category} value={category}>
                    {category}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          
          <div className="flex items-center space-x-2">
            <Switch 
              id="available" 
              checked={available} 
              onCheckedChange={setAvailable} 
            />
            <Label htmlFor="available">Disponible</Label>
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
