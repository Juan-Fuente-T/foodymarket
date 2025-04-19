
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
  const { register, handleSubmit, setValue, watch, reset, formState: { errors } } = useForm<Product>();
  const [isActive, setisActive] = useState(true);
  const [isNewProduct, setIsNewProduct] = useState(false);
  const [selectedCategoryId, setselectedCategoryId] = useState<string>();

  useEffect(() => {
    console.log("CATEGORIAS MODAL: ", categories);
    console.log('PASO 4 - Modal useEffect - Recibida Prop product:', product);    
    console.log("MODAL USE EFFECT - CategoriesData Prop:", categories);
    if (product) {
      setValue('name', product.name || '');
      setValue('description', product.description || '');
      setValue('price', product.price || 0);
      setValue('image', product.image || '');
      setValue('quantity', product.quantity || 1);
      // Use categoryId if isActive, otherwise fallback to category for compatibility
      setisActive(product.isActive !== false);
      setIsNewProduct(false);
      const initialCategoryId = product.categoryId != null ? String(product.categoryId) : '';
      console.log("PASO 5 - Modal useEffect - Calculado initialCategoryId:", initialCategoryId);
      setselectedCategoryId(initialCategoryId);
    } else {
      reset({
        name: '',
        description: '',
        price: 0,
        image: '',
        categoryId: categories.length > 0 ? String(categories[0].id) : '',
      });
      setisActive(true);
      setIsNewProduct(true);
      // setselectedCategoryId(categories.length > 0 ? String(categories[0].id) : '');
      const defaultCategoryId = categories.length > 0 ? String(categories[0].id) : '';
       console.log("MODAL USE EFFECT - Setting selectedCategoryId to (default new):", defaultCategoryId);
      setselectedCategoryId(defaultCategoryId);
      console.log("Setting initial category ID (new):", selectedCategoryId, categories);
      console.log("Setting initial category ID (new)X:", categories.length > 0 ? String(categories[0].id) : '');
      console.log("Setting initial category ID (new)XX:", String(categories[0].id));
    }
  }, [product, categories, setValue, reset]);

  const onSubmit = (data: Product) => {
    if (!selectedCategoryId) {
      toast.error("Por favor, selecciona una categoría válida.");
      return; 
  }
  const numericCategoryId = parseInt(selectedCategoryId, 10); // Convierte a número
  if (isNaN(numericCategoryId)) { // Comprueba si la conversión falló
       toast.error("El ID de categoría seleccionado no es válido.");
       return;
  }
    const productData = {
      ...product,
      ...data,
      isActive,
      categoryId: selectedCategoryId 
      // id: product?.id,
    };

    const finalProductData = isNewProduct
    ? productData 
    : { ...productData, id: product!.id };
    console.log("Datos del producto a guardar/actualizar:", productData);
    console.log("Modal onSubmit - Enviando a onSave:", finalProductData);
    onSave(productData, isNewProduct);
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
          
          <div className="flex space-y-2">
            <div>
            <Label htmlFor="category">Categoría</Label>
            <Select 
              value={selectedCategoryId}
              onValueChange={(value) => setselectedCategoryId(value)} 
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
                <div>
                <Label htmlFor="quantity">Cantidad</Label>
              <Input
                id="quantity"
                type="number"
                step="1"
                placeholder="1"
                min={1}
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
              onCheckedChange={setisActive} 
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
