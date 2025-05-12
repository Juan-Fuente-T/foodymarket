
import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Edit, Trash2, Plus } from "lucide-react";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";
import { toast } from 'sonner';

export interface CategoryManagementProps {
  categories: Array<{ id: number | string, name: string }>;
  onAdd: (categoryData: { name: string; description: string }) => void;
  // onEdit: (oldName: string, newName: string) => void;
  onDelete: (categoryId: string) => void;
}

// export function CategoryManagement({ categories, onAdd, onEdit, onDelete }: CategoryManagementProps) {
export function CategoryManagement({ categories, onAdd, onDelete }: CategoryManagementProps) {
  const [newCategoryName, setNewCategoryName] = useState("");
  const [newCategoryDescription, setNewCategoryDescription] = useState(""); 
  // const [editingCategory, setEditingCategory] = useState<{name: string, newName: string} | null>(null);
  const [categoryToDelete, setCategoryToDelete] = useState<string | null>(null);

  const handleAddCategory = () => {
    // console.log("START ADDING", newCategoryName);
    const trimmedName = newCategoryName.trim();
    const trimmedDescription = newCategoryDescription.trim()
    if (!trimmedName) {
      console.warn("El nombre de la categoría no puede estar vacío.");
      toast.warning("El nombre de la categoría no puede estar vacío.");
      ;
      return;
  }
  // console.log("DATA ADD CATEGORY EN CATEGORY MAnagement: ", trimmedName, trimmedDescription);
  const categoryData = { name: trimmedName, description: trimmedDescription };
  // console.log("DATA ADD CATEGORY EN CATEGORY MAnagement (sending object): ", categoryData);
    onAdd(categoryData);
    setNewCategoryName("");
    setNewCategoryDescription("");
  };

  // const handleStartEdit = (categoryName: string) => {
  //   console.log("START EDITING", categoryName);
  //   setEditingCategory({ name: categoryName, newName: categoryName });
  // };

  // const handleSaveEdit = () => {
  //   if (editingCategory && editingCategory.name !== editingCategory.newName) {
  //     console.log("EDITING", editingCategory);
  //     onEdit(editingCategory.name, editingCategory.newName);
  //   }
  //   setEditingCategory(null);
  // };
  
  const handleConfirmDelete = () => {
    if (categoryToDelete) {
      onDelete(categoryToDelete);
      setCategoryToDelete(null);
    } else {
      console.error('Error: categoryToDelete is null or undefined.');
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Category Management</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="flex space-x-2 mb-6">
          <Input
            placeholder="New category name"
            value={newCategoryName}
            onChange={(e) => setNewCategoryName(e.target.value)}
            // onKeyDown={(e) => e.key === 'Enter' && handleAddCategory()}
          />
          <Input
            placeholder="New category description(Optional)"
            value={newCategoryDescription}
            onChange={(e) => setNewCategoryDescription(e.target.value)}
            // onKeyDown={(e) => e.key === 'Enter' && handleAddCategory()}
          />
          <Button onClick={handleAddCategory}>
            <Plus className="mr-2 h-4 w-4" />
            Add
          </Button>
        </div>

        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead className="w-[100px]">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {categories.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={2} className="text-center py-4 text-gray-500">
                    No categories yet
                  </TableCell>
                </TableRow>
              ) : (
                categories.map((category) => {
                   return(
                  <TableRow key={category.id}>
                    <TableCell>{category.name}</TableCell>
                    <TableCell className="text-right">
                       {/* {editingCategory?.name === category.name ? (
                        <Input
                          value={editingCategory?.newName}
                          onChange={(e) => setEditingCategory({
                            ...editingCategory,
                            newName: e.target.value
                          })}
                          onKeyDown={(e) => e.key === 'Enter' && handleSaveEdit()}
                          autoFocus
                        />
                      ) : (
                        category.name
                      )} */}
                    {/* </TableCell>
                    <TableCell> */}
                      <div className="flex space-x-2 justify-end">
                        {/* {editingCategory?.name === category.name ? (
                          <Button onClick={handleSaveEdit} size="sm">
                            Save
                          </Button>
                        ) : (
                          <Button
                            onClick={() => handleStartEdit(category.name)}
                            variant="ghost"
                            size="sm"
                          >
                            <Edit className="h-4 w-4" />
                          </Button>
                        )} */}
                       <AlertDialog>
                              <AlertDialogTrigger asChild>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="text-red-500 hover:text-red-700"
                                  onClick={() => {
                                    if (category && category.id !== undefined && category.id !== null) {
                                      const categoryIdStr = category.id.toString();
                                      setCategoryToDelete(categoryIdStr);
                                    } else {
                                      console.error('ERROR al intentar borrar: category o category.id es inválido.', category);
                                    }
                                  }}
                                >
                                  <Trash2 className="h-4 w-4" />
                                </Button>
                              </AlertDialogTrigger>
                              <AlertDialogContent>
                                <AlertDialogHeader>
                                  <AlertDialogTitle>Delete Category</AlertDialogTitle>
                                  <AlertDialogDescription>
                                    Are you sure you want to delete "{category?.name || 'this category'}"? This action cannot be undone.
                                  </AlertDialogDescription>
                                </AlertDialogHeader>
                                <AlertDialogFooter>
                                  <AlertDialogCancel onClick={() => setCategoryToDelete(null)}>Cancel</AlertDialogCancel>
                                  <AlertDialogAction onClick={handleConfirmDelete} className="bg-red-500 hover:bg-red-600">
                                    Delete
                                  </AlertDialogAction>
                                </AlertDialogFooter>
                              </AlertDialogContent>
                            </AlertDialog>
                          </div>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  ); 
}
