
import React, { useState } from 'react';
import { 
  Card, 
  CardContent, 
  CardDescription, 
  CardHeader, 
  CardTitle 
} from '@/components/ui/card';
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Edit, PlusCircle, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

interface Category {
  id: string;
  name: string;
  description?: string;
}

interface CategoryManagementProps {
  categories: string[];
  onAddCategory: (category: string) => void;
  onEditCategory: (oldName: string, newName: string) => void;
  onDeleteCategory: (category: string) => void;
}

export const CategoryManagement = ({
  categories,
  onAddCategory,
  onEditCategory,
  onDeleteCategory
}: CategoryManagementProps) => {
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [currentCategory, setCurrentCategory] = useState('');
  
  const { register, handleSubmit, reset, formState: { errors } } = useForm<{ name: string }>();
  
  const handleAdd = (data: { name: string }) => {
    onAddCategory(data.name);
    toast.success(`Categoría "${data.name}" creada con éxito`);
    setIsAddDialogOpen(false);
    reset();
  };
  
  const handleEdit = (data: { name: string }) => {
    onEditCategory(currentCategory, data.name);
    toast.success(`Categoría actualizada con éxito`);
    setIsEditDialogOpen(false);
    reset();
  };
  
  const confirmDelete = (category: string) => {
    if (window.confirm(`¿Estás seguro de que deseas eliminar la categoría "${category}"?`)) {
      onDeleteCategory(category);
      toast.success(`Categoría "${category}" eliminada con éxito`);
    }
  };
  
  const openEditDialog = (category: string) => {
    setCurrentCategory(category);
    reset({ name: category });
    setIsEditDialogOpen(true);
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Categorías de Productos</CardTitle>
          <CardDescription>Gestiona las categorías de productos de tu restaurante</CardDescription>
        </div>
        <Button 
          className="bg-food-600 hover:bg-food-700"
          onClick={() => {
            reset({ name: '' });
            setIsAddDialogOpen(true);
          }}
        >
          <PlusCircle className="mr-2 h-4 w-4" /> Añadir Categoría
        </Button>
      </CardHeader>
      <CardContent>
        {categories.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nombre</TableHead>
                <TableHead className="text-right">Acciones</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {categories.map((category) => (
                <TableRow key={category}>
                  <TableCell className="font-medium">{category}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button 
                        variant="ghost" 
                        size="sm"
                        onClick={() => openEditDialog(category)}
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button 
                        variant="ghost" 
                        size="sm"
                        onClick={() => confirmDelete(category)}
                      >
                        <Trash2 className="h-4 w-4 text-red-500" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : (
          <div className="text-center py-8">
            <p className="text-gray-600 mb-4">No hay categorías definidas</p>
            <Button 
              className="bg-food-600 hover:bg-food-700"
              onClick={() => setIsAddDialogOpen(true)}
            >
              <PlusCircle className="mr-2 h-4 w-4" /> Añadir Tu Primera Categoría
            </Button>
          </div>
        )}
      </CardContent>

      {/* Add Category Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Añadir Nueva Categoría</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(handleAdd)} className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="name">Nombre de la Categoría</Label>
              <Input
                id="name"
                placeholder="Ej: Entradas, Platos Principales, Bebidas"
                {...register('name', { required: 'El nombre es obligatorio' })}
              />
              {errors.name && (
                <p className="text-sm text-red-500">{errors.name.message}</p>
              )}
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsAddDialogOpen(false)}>
                Cancelar
              </Button>
              <Button type="submit" className="bg-food-600 hover:bg-food-700">
                Añadir Categoría
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Edit Category Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Editar Categoría</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(handleEdit)} className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="edit-name">Nombre de la Categoría</Label>
              <Input
                id="edit-name"
                placeholder="Nombre de la categoría"
                {...register('name', { required: 'El nombre es obligatorio' })}
              />
              {errors.name && (
                <p className="text-sm text-red-500">{errors.name.message}</p>
              )}
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsEditDialogOpen(false)}>
                Cancelar
              </Button>
              <Button type="submit" className="bg-food-600 hover:bg-food-700">
                Guardar Cambios
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </Card>
  );
};
