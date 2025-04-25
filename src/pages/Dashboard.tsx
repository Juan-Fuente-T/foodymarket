import React, { useEffect, useMemo, useState } from "react";
import { Layout } from "@/components/layout/Layout";
import { useAuth } from "../hooks/use-auth";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { restaurantAPI, orderAPI, productAPI, categoryAPI } from "@/services/api";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";
import { Skeleton } from "@/components/ui/skeleton";
import {
  CheckCircle, Clock, PlusCircle, RefreshCcw, TrendingUp,
  Users, DollarSign, ShoppingBag, User, Settings,
  Edit, Trash2, Package, Star, Building
} from "lucide-react";
import { Product, Restaurant, GroupedProduct } from "@/types/models";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { ProductEditModal } from "@/components/product/ProductEditModal";
import { CategoryManagement } from "@/components/category/CategoryManagement";
import { toast } from "sonner";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";
import { supabase } from "@/lib/supabaseClient";


const Dashboard = () => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  if (isLoading) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Skeleton className="h-12 w-1/3 mb-6" />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            {Array(3).fill(0).map((_, i) => (
              <Skeleton key={i} className="h-32 w-full rounded-xl" />
            ))}
          </div>
          <Skeleton className="h-64 w-full rounded-xl" />
        </div>
      </Layout>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role === "RESTAURANTE") {
    return <RestaurantDashboard />;
  }

  return <CustomerDashboard />;
};

const CustomerDashboard = () => {
  const { user } = useAuth();

  const { data: orders = [], isLoading: isLoadingOrders } = useQuery({
    queryKey: ["customerOrders", user?.id],
    queryFn: () => orderAPI.getByClient(user?.id as string),
    enabled: !!user?.id,
  });

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">My Dashboard</h1>
            <p className="text-gray-600 mt-1">Welcome back, {user?.name}</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-gray-500">Total Orders</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center">
                <ShoppingBag className="h-6 w-6 text-blue-500 mr-2" />
                <span className="text-3xl font-bold">{orders.length}</span>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-gray-500">Profile</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center">
                <User className="h-6 w-6 text-purple-500 mr-2" />
                <span className="text-lg font-medium">{user?.email}</span>
              </div>
            </CardContent>
            <CardFooter>
              <Button variant="outline" size="sm" asChild>
                <Link to="/profile">Edit Profile</Link>
              </Button>
            </CardFooter>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-gray-500">Settings</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center">
                <Settings className="h-6 w-6 text-gray-500 mr-2" />
                <span className="text-lg font-medium">Account Settings</span>
              </div>
            </CardContent>
            <CardFooter>
              <Button variant="outline" size="sm" asChild>
                <Link to="/settings">Manage Settings</Link>
              </Button>
            </CardFooter>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <div className="flex justify-between">
              <CardTitle>Recent Orders</CardTitle>
              <Button variant="outline" size="sm" asChild>
                <Link to="/orders">View All Orders</Link>
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {isLoadingOrders ? (
              <div className="space-y-4">
                {Array(5).fill(0).map((_, i) => (
                  <Skeleton key={i} className="h-12 w-full" />
                ))}
              </div>
            ) : orders.length > 0 ? (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Order ID</TableHead>
                    <TableHead>Date</TableHead>
                    <TableHead>Restaurant</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Amount</TableHead>
                    <TableHead></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {orders.slice(0, 5).map((order) => (
                    <TableRow key={order?.id}>
                      <TableCell className="font-medium">{order.id.slice(0, 8)}</TableCell>
                      <TableCell>
                        {new Date(order.createdAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell>{order.restaurantId}</TableCell>
                      <TableCell>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${order?.status === 'delivered' ? 'bg-green-100 text-green-800' :
                          order?.status === 'preparing' ? 'bg-blue-100 text-blue-800' :
                            order?.status === 'pending' ? 'bg-yellow-100 text-yellow-800' :
                              'bg-gray-100 text-gray-800'
                          }`}>
                          {order?.status.charAt(0).toUpperCase() + order?.status.slice(1)}
                        </span>
                      </TableCell>
                      <TableCell className="text-right">${order?.total.toFixed(2)}</TableCell>
                      <TableCell>
                        <Button variant="ghost" size="sm">
                          View
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            ) : (
              <div className="text-center py-8">
                <p className="text-gray-600 mb-4">You haven't placed any orders yet.</p>
                <Button asChild className="bg-food-600 hover:bg-food-700">
                  <Link to="/restaurants">Browse Restaurants</Link>
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </Layout>
  );
};

const RestaurantDashboard = () => {
  const { user } = useAuth();
  const [selectedRestaurant, setSelectedRestaurant] = useState<Restaurant | null>(null);
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const [isProductModalOpen, setIsProductModalOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [productToDelete, setProductToDelete] = useState<Product | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const { data: ownedRestaurants = [], isLoading: isLoadingRestaurants, error: errorRestaurants } =
    useQuery<Restaurant[], Error>({
      queryKey: ["userRestaurants", user?.id],
      queryFn: async (): Promise<Restaurant[]> => {
        if (!user?.id) return [];
        try {
          const allRestaurants = await restaurantAPI.getAll();
          console.log("Data received from API getAll:", allRestaurants);
          if (!Array.isArray(allRestaurants)) {
            console.error("API did not return an array for getAll:", allRestaurants);
            return [];
          }
          const ownerIdString = String(user.id);
          const filtered = allRestaurants.filter((restaurant: Restaurant) => String(restaurant.ownerId) === ownerIdString);
          return filtered;
        } catch (apiError) {
          console.error("Error fetching or filtering restaurants", apiError);
          return [];
        }
      },
      enabled: !!user?.id,
    });

  useEffect(() => {
    if (selectedRestaurant || isLoadingRestaurants || errorRestaurants) return;

    if (ownedRestaurants.length >= 1) {
      console.log("Auto-selecting restaurant:", ownedRestaurants[0]);
      setSelectedRestaurant(ownedRestaurants[0]);
    } else {
      setSelectedRestaurant(null);
    }
  }, [ownedRestaurants, isLoadingRestaurants, errorRestaurants]);

  const { data: orders = [], isLoading: isLoadingOrders } = useQuery({
    queryKey: ["restaurantOrders", selectedRestaurant?.id],
    queryFn: () => orderAPI.getByRestaurant(selectedRestaurant!.id.toString()),
    enabled: !!selectedRestaurant?.id,
  });

  const { data: groupedProductsData = [], isLoading: isLoadingProducts } = useQuery({
    queryKey: ["groupedProducts", selectedRestaurant?.id], // Clave específica para productos agrupados
    queryFn: () => productAPI.getByRestaurantAndCategory(selectedRestaurant!.id.toString()),
    enabled: !!selectedRestaurant?.id,
  });

  const allProducts = useMemo((): Product[] => {
    // Usa 'groupedProductsData' COMO INPUT!!
    if (!groupedProductsData || !Array.isArray(groupedProductsData)) {
      return [];
    }
    const flattenedProducts: Product[] = [];
    console.log("FLATTENED: ", flattenedProducts);
    // Itera sobre la estructura agrupada que devuelve getByRestaurantAndCategory
    groupedProductsData.forEach((categoryGroup: any) => {
      const currentGroupId = categoryGroup.categoryId ?? null;
      const currentGroupName = categoryGroup.categoryName || '';

      if (!categoryGroup.products || !Array.isArray(categoryGroup.products)) {
        return;
      }

      categoryGroup.products.forEach((backendProduct: any) => {
        // Mapea los campos del backendProduct a tu tipo Product del frontend
        const frontendProduct = {
          id: String(backendProduct.prd_id ?? ''),
          name: backendProduct.name || '',
          description: backendProduct.description || '',
          price: Number(backendProduct.price ?? 0),
          image: backendProduct.image || '',
          categoryId: String(backendProduct.categoryId ?? currentGroupId ?? ''),
          isActive: backendProduct.isActive === true,
          quantity: Number(backendProduct.quantity || 0),
          restaurantId: String(backendProduct.restaurantId || ''),
          createdAt: backendProduct.createdAt || '',
          updatedAt: backendProduct.updatedAt || '',
          categoryName: backendProduct.categoryName || currentGroupName
        };
        flattenedProducts.push(frontendProduct as Product);
      });
    });
    console.log("--- Fin cálculo allProducts. Resultado:", flattenedProducts);
    return flattenedProducts;
  }, [groupedProductsData]);

  const { data: offeredCategories = [], isLoading: isLoadingCategories } = useQuery({
    queryKey: ["restaurantProductCategories", selectedRestaurant?.id], // Clave más clara
    queryFn: () => restaurantAPI.getProductCategories(selectedRestaurant!.id.toString()),
    enabled: !!selectedRestaurant?.id,
  });
  console.log("restaurantProductCategories:", offeredCategories ? offeredCategories : 'NADA');

  const categoriesDataForModal = useMemo(() => {
    if (!Array.isArray(offeredCategories)) return [];
    return offeredCategories.map(category => ({
      id: category.id,
      name: category.name
    }));
  }, [offeredCategories]);
  console.log("CATEGORIAS UNICAS", categoriesDataForModal);

  const createProductMutation = useMutation({
    mutationFn: (createData: any) => productAPI.create(createData),
    onSuccess: () => {
      toast.success("Producto creado con éxito");
      queryClient.invalidateQueries({ queryKey: ['groupedProducts', selectedRestaurant?.id] });
      setIsProductModalOpen(false);
    },
    onError: (error: Error) => {
      console.error("Error creating product:", error);
      toast.error(`Error creando producto: ${error.message}`);
    }
  });

  const updateProductMutation = useMutation({
    mutationFn: (vars: { id: string | number, data: any }) => productAPI.update(vars.id.toString(), vars.data),
    onSuccess: () => {
      toast.success("Producto actualizado con éxito");
      queryClient.invalidateQueries({ queryKey: ['groupedProducts', selectedRestaurant?.id] });
      setIsProductModalOpen(false);
    },
    onError: (error: Error) => {
      console.error("Error updating product:", error);
      toast.error(`Error actualizando producto: ${error.message}`);
    }
  });

  const { mutate: deleteProduct } = useMutation({
    mutationFn: (productId: string) => {
      console.log("Deleting product with ID:", productId);
      if (!productId || productId === "undefined" || productId === "") {
        throw new Error("Invalid product ID for deletion");
      }
      return productAPI.delete(productId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["groupedProducts", selectedRestaurant?.id] });
      toast.success("Producto eliminado con éxito");
    },
    onError: (error) => {
      console.error("Error deleting product:", error);
      toast.error("Error al eliminar el producto");
    }
  });

  const createCategoryMutation = useMutation({
    mutationFn: (vars: { restaurantId: string; categoryData: { name: string; description?: string } }) => {
      console.log(`Attempting POST /api/restaurants/${vars.restaurantId}/categories`);
      if (!vars.restaurantId) throw new Error("Restaurant ID es necesario");
      if (!vars.categoryData || !vars.categoryData.name) throw new Error("Nombre de categoría es necesario");

      // Asume que tienes esta función en tu API client que hace el POST correcto
      return restaurantAPI.addProductCategory(vars.restaurantId, vars.categoryData);
    },
    onSuccess: (returnedCategory, vars) => { // data es la CategoryResponseDto devuelta por el POST
      toast.success(`Categoría '${returnedCategory.name}' creada con éxito.`);
      queryClient.invalidateQueries({ queryKey: ['restaurantProductCategories', vars.restaurantId] });
      queryClient.invalidateQueries({ queryKey: ['groupedProducts', vars.restaurantId] });

      console.log(`Queries invalidadas después de añadir/asociar categoría a restaurante ${vars.restaurantId}`);
      // setIsAddCategoryModalOpen(false);
    },
    onError: (error: Error) => {
      console.error("Error creating category:", error);
      toast.error(`Error al añadir categoría: ${error.message}`);
    }
  });

  const { mutate: deleteCategory } = useMutation({
    mutationFn: (vars: { categoryId: string; restaurantId: string }) => {
      console.log("Deleting category with ID:", vars.categoryId);
      if (!vars.categoryId || vars.categoryId === "undefined" || vars.categoryId === "") {
        throw new Error("Invalid categoryId ID for category deletion");
      }
      if (!vars.restaurantId || vars.restaurantId  === "undefined" || vars.restaurantId  === "") {
        throw new Error("Invalid restaurantId ID for category deletion");
      }
      return categoryAPI.delete(vars.categoryId, vars.restaurantId);
    },
    onSuccess: (data, vars) => { // El segundo argumento es la variable pasada a mutate()
      toast.success(`Categoría global (ID: ${vars.categoryId}) eliminada con éxito`);
      queryClient.invalidateQueries({ queryKey: ['restaurantProductCategories', vars.restaurantId] });
      queryClient.invalidateQueries({ queryKey: ['groupedProducts', vars.restaurantId] });
    },
    onError: (error: Error) => {
      console.error("Error deleting global category:", error);
      // El backend debería devolver un error claro si la categoría está en uso (ej: 409 Conflict o 400 Bad Request)
      toast.error(`Error al eliminar categoría global: ${error.message}`);
    }
  });

  const pendingOrders = useMemo(() => orders.filter((o: any) => o.status === 'pending' || o.status === 'preparing'), [orders]);
  const completedOrders = useMemo(() => orders.filter((o: any) => o.status === 'delivered' || o.status === 'completed'), [orders]);
  const totalRevenue = useMemo(() => completedOrders.reduce((sum: number, o: any) => sum + (o.total || 0), 0), [completedOrders]);

  const handleRestaurantChange = (restaurantId: string) => {
    const restaurant = ownedRestaurants.find(r => String(r.id) === restaurantId) || null;
    console.log("User selected restaurant:", restaurant);
    setSelectedRestaurant(restaurant);
  };

  const handleAddProduct = () => {
    setSelectedProduct(null);
    setIsProductModalOpen(true);
  };

  const handleEditProduct = (product: Product) => {
    // <<< --- AÑADE ESTE LOG AQUÍ --- >>>
    console.log('PRODUCTO QUE LLEGA A handleEditProduct:', JSON.stringify(product, null, 2));
    // <<< --- FIN DEL LOG AÑADIDO --- >>>
    console.log('PASO 1 - handleEditProductClick - Recibido productToEdit:', product);
    // Make sure we have a valid product with all required fields
    if (!product.id) {
      console.error("Product missing ID:", product);
      toast.error("Error: ID de producto no encontrado");
      return;
    }
    setSelectedProduct(product);
    console.log('PASO 2 - handleEditProductClick - Después de setSelectedProduct, abriendo modal...');
    setIsProductModalOpen(true);
  };

  const handleDeleteProduct = (product: Product) => {
    console.log("Product to delete:", product);
    if (!product.id) {
      console.error("Cannot delete product without ID:", product);
      toast.error("Error: ID de producto no encontrado");
      return;
    }
    setProductToDelete(product);
    setIsDeleteDialogOpen(true);
  };

  const confirmDeleteProduct = () => {
    if (productToDelete && productToDelete.id) {
      console.log("Confirming delete for product:", productToDelete.id);
      deleteProduct(productToDelete.id);
    } else {
      console.error("Attempted to delete product without ID");
      toast.error("Error: No se puede eliminar un producto sin ID");
    }
    setIsDeleteDialogOpen(false);
  };

  const handleSaveProduct = (productData: any, isNew: boolean) => {
    console.log("handleSaveProduct received:", productData)
    console.log("isNew???:", isNew);
    // --- Validación y parseo de tipos ANTES de enviar ---
    let categoryIdNum: number | null = null;
    if (productData.categoryId != null && productData.categoryId !== '') {
      categoryIdNum = parseInt(String(productData.categoryId), 10);
      if (isNaN(categoryIdNum)) {
        toast.error("ID de categoría inválido."); return;
      }
    } else if (isNew) { // Es obligatorio para crear
      toast.error("La categoría es obligatoria."); return;
    }

    let priceNum: number | null = null;
    if (productData.price != null && String(productData.price).trim() !== '') {
      priceNum = parseFloat(String(productData.price));
      if (isNaN(priceNum) || priceNum < 0) {
        toast.error("Precio inválido."); return;
      }
    } else {
      toast.error("El precio es obligatorio."); return;
    }

    let quantityNum: number | null = null;
    if (productData.quantity != null && String(productData.quantity).trim() !== '') {
      quantityNum = parseInt(String(productData.quantity), 10);
      if (isNaN(quantityNum) || quantityNum < 0) {
        toast.error("Cantidad inválida."); return;
      }
    } else {
      quantityNum = 1; // Default 1 si no viene? O error?
    }
    console.log("PRODUCTDATA:", productData);
    const finalData: any = {
      name: productData.name,
      description: productData.description,
      price: priceNum, // Usa el número parseado (o BigDecimal si usas eso)
      image: productData.image,
      isActive: productData.isActive,
      quantity: quantityNum, // Usa el número parseado
      categoryId: categoryIdNum
    };

    if (selectedRestaurant?.id) {
      finalData.restaurantId = selectedRestaurant.id;
    }

    if (isNew) {
      console.log("Calling CREATE mutation with:", finalData);
      createProductMutation.mutate(finalData);
    } else {
      if (!productData.id) {
        toast.error("Error: ID de producto necesario para actualizar.");
        return;
      }
      updateProductMutation.mutate({ id: String(productData.id), data: finalData });
      setIsProductModalOpen(false); // Cierra el modal después de guardar (o en onSuccess)
      setSelectedProduct(null);
    }
  };

  // const handleAddCategory = (categoryName: string) => {
  const handleAddCategory = (formData: { name: string; description: string }) => {
    console.log("DATA addCategory: ", formData, formData.name, formData.description);
    createCategoryMutation.mutate({
          restaurantId: selectedRestaurant!.id.toString(),
          categoryData: { name: formData.name, description: formData.description }
        });
    // toast.success(`Categoría "${categoryName}" agregada con éxito`);
  };

  // const handleEditCategory = (oldName: string, newName: string) => {
  //   toast.success(`Categoría actualizada de "${oldName}" a "${newName}"`);
  // };

  const handleDeleteCategory = (categoryId: string) => {
    const restaurantId = selectedRestaurant?.id;
    if (restaurantId === undefined || restaurantId === null) {
      console.error("ERROR: selectedRestaurant.id es undefined o null.");
      toast.error("Error: No se ha seleccionado un restaurante válido.");
      return;
    }
    deleteCategory({
      categoryId: categoryId,
      restaurantId: restaurantId.toString()
    });
    // toast.success(`Categoría "${categoryId}" eliminada con éxito`);
  };

  const orderStatusData = useMemo(() => {
    const statusCounts: { [key: string]: number } = {
      pending: 0,
      preparing: 0,
      delivered: 0,
      completed: 0,
      cancelled: 0
    };

    orders.forEach((order: any) => {
      if (order?.status in statusCounts) {
        statusCounts[order?.status]++;
      }
    });

    return Object.entries(statusCounts).map(([status, count]) => ({
      name: status.charAt(0).toUpperCase() + status.slice(1),
      value: count
    }));
  }, [orders]);

  const revenueData = useMemo(() => {
    const last7Days = Array.from({ length: 7 }, (_, i) => {
      const date = new Date();
      date.setDate(date.getDate() - i);
      return date.toISOString().split('T')[0];
    }).reverse();

    const dailyRevenue: { [key: string]: number } = {};
    last7Days.forEach(day => {
      dailyRevenue[day] = 0;
    });

    completedOrders.forEach((order: any) => {
      const orderDate = new Date(order?.createdAt).toISOString().split('T')[0];
      if (orderDate in dailyRevenue) {
        dailyRevenue[orderDate] += order?.total || 0;
      }
    });

    return Object.entries(dailyRevenue).map(([date, amount]) => ({
      date: new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
      amount
    }));
  }, [completedOrders]);

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

  if (isLoadingRestaurants) {
    return <Layout><div className="p-8">Loading your restaurants...</div></Layout>;
  }

  if (errorRestaurants) {
    return <Layout><div className="p-8 text-red-600">Error loading your restaurants: {errorRestaurants.message}</div></Layout>;
  }

  if (!isLoadingRestaurants && ownedRestaurants.length === 0) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center py-12 bg-white rounded-lg shadow-sm border border-gray-100">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">You don't have any restaurants yet</h2>
            <p className="text-gray-600 mb-6">Register your restaurant to start managing orders and menu items.</p>
            <Button asChild className="bg-food-600 hover:bg-food-700">
              <Link to="/partner">Register Your Restaurant</Link>
            </Button>
          </div>
        </div>
      </Layout>
    );
  }

  const deleteProduct = async (productId: string, imagePath?: string) => {
    try {
      // Delete the product first
      await productAPI.delete(productId);
      
      // If there's an image associated, delete it from storage
      if (imagePath) {
        const imageUrl = new URL(imagePath);
        const pathParts = imageUrl.pathname.split('/');
        const bucketPath = pathParts.slice(2).join('/'); // Remove /storage/v1/object/
        
        const { error } = await supabase.storage
          .from('fotos-c24-39-t-webapp')
          .remove([bucketPath]);
          
        if (error) {
          console.error('Error deleting image:', error);
          toast.error('Product deleted but failed to delete image');
        }
      }
      
      toast.success('Product deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['groupedProducts'] });
    } catch (error) {
      console.error('Error deleting product:', error);
      toast.error('Failed to delete product');
    }
  };

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Restaurant Dashboard</h1>
            {selectedRestaurant && (
              <p className="text-gray-600 mt-1">{selectedRestaurant.name}</p>
            )}
            {!selectedRestaurant && ownedRestaurants.length > 1 && (
              <p className="text-gray-500 mt-1">Select a restaurant to manage</p>
            )}
          </div>

          {ownedRestaurants.length > 0 && (
            <div className="w-full md:w-auto md:min-w-[280px] lg:min-w-[320px] space-y-1.5 mt-4 md:mt-0">
              <Label htmlFor="restaurant-select" className="text-sm font-medium text-gray-700">
                Managing Restaurant:
              </Label>
              <Select
                value={selectedRestaurant ? String(selectedRestaurant.id) : ""}
                onValueChange={handleRestaurantChange}
              >
                <SelectTrigger id="restaurant-select" className="h-10 text-base">
                  <SelectValue placeholder="Select your restaurant..." />
                </SelectTrigger>
                <SelectContent>
                  {ownedRestaurants.map((r) => (
                    <SelectItem key={r.id} value={String(r.id)} className="text-base">
                      {r.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}

          <div className="mt-4 md:mt-0 space-x-2 self-end md:self-center">
            <Button asChild variant="outline" className="border-food-500 text-food-500 hover:bg-food-50">
              <Link to="/partner">
                <Building className="mr-2 h-4 w-4" />
                Add New Restaurant
              </Link>
            </Button>
            {selectedRestaurant && (
              <>
                <Button asChild variant="outline" disabled={!selectedRestaurant}>
                  <Link to={selectedRestaurant ? `/restaurants/${selectedRestaurant.id}` : '#'}>
                    View Restaurant
                  </Link>
                </Button>
                <Button asChild className="bg-food-600 hover:bg-food-700" disabled={!selectedRestaurant}>
                  <Link to={selectedRestaurant ? `/edit-restaurant/${selectedRestaurant.id}` : '#'}>
                    Edit Restaurant
                  </Link>
                </Button>
              </>
            )}
          </div>
        </div>

        {!selectedRestaurant ? (
          <div className="text-center py-16 text-gray-500">
            Please select one of your restaurants to see the dashboard.
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium text-gray-500">Pending Orders</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center">
                    <Clock className="h-6 w-6 text-yellow-500 mr-2" />
                    <span className="text-3xl font-bold">
                      {isLoadingOrders ? <Skeleton className="h-8 w-12 inline-block" /> : pendingOrders.length}
                    </span>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium text-gray-500">Completed Orders</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center">
                    <CheckCircle className="h-6 w-6 text-green-500 mr-2" />
                    <span className="text-3xl font-bold">
                      {isLoadingOrders ? <Skeleton className="h-8 w-12 inline-block" /> : completedOrders.length}
                    </span>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium text-gray-500">Total Revenue</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center">
                    <DollarSign className="h-6 w-6 text-green-500 mr-2" />
                    <span className="text-3xl font-bold">
                      {isLoadingOrders ? <Skeleton className="h-8 w-20 inline-block" /> : `$${totalRevenue.toFixed(2)}`}
                    </span>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium text-gray-500">Total Products</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center">
                    <Package className="h-6 w-6 text-blue-500 mr-2" />
                    <span className="text-3xl font-bold">
                      {isLoadingProducts ? <Skeleton className="h-8 w-12 inline-block" /> : allProducts.length}
                    </span>
                  </div>
                </CardContent>
              </Card>
            </div>
