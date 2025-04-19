
import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Edit, Trash2, Plus } from "lucide-react";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";

export interface CategoryManagementProps {
  categories: Array<{ id: number | string, name: string }>;
  onAdd: (categoryName: string) => void;
  onEdit: (oldName: string, newName: string) => void;
  onDelete: (categoryName: string) => void;
}

export function CategoryManagement({ categories, onAdd, onEdit, onDelete }: CategoryManagementProps) {
  const [newCategoryName, setNewCategoryName] = useState("");
  const [editingCategory, setEditingCategory] = useState<{name: string, newName: string} | null>(null);
  const [categoryToDelete, setCategoryToDelete] = useState<string | null>(null);

  const handleAddCategory = () => {
    if (!newCategoryName.trim()) return;
    onAdd(newCategoryName);
    setNewCategoryName("");
  };

  const handleStartEdit = (categoryName: string) => {
    setEditingCategory({ name: categoryName, newName: categoryName });
  };

  const handleSaveEdit = () => {
    if (editingCategory && editingCategory.name !== editingCategory.newName) {
      onEdit(editingCategory.name, editingCategory.newName);
    }
    setEditingCategory(null);
  };

  const handleConfirmDelete = () => {
    if (categoryToDelete) {
      onDelete(categoryToDelete);
      setCategoryToDelete(null);
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
            onKeyDown={(e) => e.key === 'Enter' && handleAddCategory()}
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
                categories.map((category) => (
                  <TableRow key={category.id}>
                    <TableCell>
                      {editingCategory?.name === category.name ? (
                        <Input
                          value={editingCategory.newName}
                          onChange={(e) => setEditingCategory({
                            ...editingCategory,
                            newName: e.target.value
                          })}
                          onKeyDown={(e) => e.key === 'Enter' && handleSaveEdit()}
                          autoFocus
                        />
                      ) : (
                        category.name
                      )}
                    </TableCell>
                    <TableCell>
                      <div className="flex space-x-2">
                        {editingCategory?.name === category.name ? (
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
                        )}
                        <AlertDialog>
                          <AlertDialogTrigger asChild>
                            <Button
                              variant="ghost"
                              size="sm"
                              className="text-red-500 hover:text-red-700"
                              onClick={() => setCategoryToDelete(category.name)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </AlertDialogTrigger>
                          <AlertDialogContent>
                            <AlertDialogHeader>
                              <AlertDialogTitle>Delete Category</AlertDialogTitle>
                              <AlertDialogDescription>
                                Are you sure you want to delete "{category.name}"? This action cannot be undone.
                              </AlertDialogDescription>
                            </AlertDialogHeader>
                            <AlertDialogFooter>
                              <AlertDialogCancel>Cancel</AlertDialogCancel>
                              <AlertDialogAction onClick={handleConfirmDelete} className="bg-red-500 hover:bg-red-600">
                                Delete
                              </AlertDialogAction>
                            </AlertDialogFooter>
                          </AlertDialogContent>
                        </AlertDialog>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  );
}
