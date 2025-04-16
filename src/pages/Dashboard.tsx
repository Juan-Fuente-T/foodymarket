
import React, { useEffect, useMemo, useState } from "react";
import { Layout } from "@/components/layout/Layout";
import { useAuth } from "../hooks/use-auth";
import { useQuery } from "@tanstack/react-query";
import { restaurantAPI, orderAPI, productAPI } from "@/services/api";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import { Skeleton } from "@/components/ui/skeleton";
import { CheckCircle, Clock, PlusCircle, RefreshCcw, TrendingUp, Users, DollarSign, ShoppingBag, User, Settings } from "lucide-react";
import { Restaurant } from "@/types/models";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";

const Dashboard = () => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  // Show loading state while auth is being checked
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

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Render different dashboards based on user role
  if (user?.role === "restaurante") {
    return <RestaurantDashboard />;
  }

  // Default to customer dashboard
  return <CustomerDashboard />;
};

const CustomerDashboard = () => {
  const { user } = useAuth();

  // Fetch customer orders
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
                    <TableRow key={order.id}>
                      <TableCell className="font-medium">{order.id.slice(0, 8)}</TableCell>
                      <TableCell>
                        {new Date(order.createdAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell>{order.restaurantId}</TableCell>
                      <TableCell>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${order.status === 'delivered' ? 'bg-green-100 text-green-800' :
                            order.status === 'preparing' ? 'bg-blue-100 text-blue-800' :
                              order.status === 'pending' ? 'bg-yellow-100 text-yellow-800' :
                                'bg-gray-100 text-gray-800'
                          }`}>
                          {order.status.charAt(0).toUpperCase() + order.status.slice(1)}
                        </span>
                      </TableCell>
                      <TableCell className="text-right">${order.total.toFixed(2)}</TableCell>
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
  const { user } = useAuth(); // Obtiene el usuario logueado del contexto

  // --- Estado para guardar el restaurante seleccionado ---
  const [selectedRestaurant, setSelectedRestaurant] = useState<Restaurant | null>(null);

  // --- Query para obtener los restaurantes PROPIEDAD del usuario ---
  const { data: ownedRestaurants = [], isLoading: isLoadingRestaurants, error: errorRestaurants } =
    useQuery<Restaurant[], Error>({ // <-- TIPADO EXPLÍCITO AQUÍ
      queryKey: ["userRestaurants", user?.id],
      // Asegúrate que la función SIEMPRE devuelve Promise<Restaurant[]>
      queryFn: async (): Promise<Restaurant[]> => { // <-- Tipa el retorno de la función
        if (!user?.id) return [];
        try {
          // Asume que getAll devuelve Promise<Restaurant[]> o Promise<any>
          const allRestaurants = await restaurantAPI.getAll();
          console.log("Data received from API getAll:", allRestaurants);
          // Comprobación robusta por si la API devuelve algo inesperado
          if (!Array.isArray(allRestaurants)) {
            console.error("API did not return an array for getAll:", allRestaurants);
            return []; // Devuelve array vacío en caso de respuesta inesperada
          }
          const ownerIdString = String(user.id);
          // El filter siempre devuelve un array
          const filtered = allRestaurants.filter((restaurant: Restaurant) => String(restaurant.ownerId) === ownerIdString);
          return filtered;
        } catch (apiError) {
          console.error("Error fetching or filtering restaurants", apiError);
          return []; // Devuelve array vacío en caso de error en la llamada/filtro
        }
      },
      enabled: !!user?.id,
    });

  // --- Efecto para auto-seleccionar si solo hay un restaurante ---
  useEffect(() => {
    // No hacer nada si ya hay uno seleccionado o si aún está cargando/error
    if (selectedRestaurant || isLoadingRestaurants || errorRestaurants) return;

    if (ownedRestaurants.length === 1) {
      // Si solo hay uno, selecciónalo
      console.log("Auto-selecting the only restaurant:", ownedRestaurants[0]);
      setSelectedRestaurant(ownedRestaurants[0]);
    } else if (ownedRestaurants.length > 1) {
      // Si hay varios, selecciona el primero por defecto (o null si prefieres)
      console.log("Multiple restaurants found, selecting first by default:", ownedRestaurants[0]);
      setSelectedRestaurant(ownedRestaurants[0]);
    } else {
      // Si no hay restaurantes, asegúrate que no hay nada seleccionado
      setSelectedRestaurant(null);
    }
    // OJO: Añadir selectedRestaurant a las dependencias puede causar bucles si no se maneja bien.
    // Lo quitamos para que solo se ejecute cuando cambien los restaurantes o el estado de carga/error.
  }, [ownedRestaurants, isLoadingRestaurants, errorRestaurants]); // Dependencias clave


  // --- Queries dependientes del restaurante SELECCIONADO ---
  const { data: orders = [], isLoading: isLoadingOrders } = useQuery({
    queryKey: ["restaurantOrders", selectedRestaurant?.id], // Key depende del ID seleccionado
    queryFn: () => orderAPI.getByRestaurant(selectedRestaurant!.id.toString()), // Llama solo si selectedRestaurant tiene ID
    enabled: !!selectedRestaurant?.id, // Habilitado solo si hay un restaurante seleccionado
  });

  const { data: products = [], isLoading: isLoadingProducts } = useQuery({
    queryKey: ["restaurantProducts", selectedRestaurant?.id], // Key depende del ID seleccionado
    queryFn: () => productAPI.getByRestaurant(selectedRestaurant!.id.toString()), // Llama solo si selectedRestaurant tiene ID
    enabled: !!selectedRestaurant?.id, // Habilitado solo si hay un restaurante seleccionado
  });

  // --- Cálculos basados en 'orders' (ajusta según tu estructura de datos real) ---
  const pendingOrders = useMemo(() => orders.filter((o: any) => o.status === 'pending' || o.status === 'preparing'), [orders]);
  const completedOrders = useMemo(() => orders.filter((o: any) => o.status === 'delivered' || o.status === 'completed'), [orders]);
  const totalRevenue = useMemo(() => completedOrders.reduce((sum: number, o: any) => sum + (o.total || 0), 0), [completedOrders]);

  // --- Manejador para cambiar el restaurante seleccionado ---
  const handleRestaurantChange = (restaurantId: string) => {
    // Busca el restaurante completo en la lista de los poseídos
    // Compara como string por si acaso los IDs son numéricos
    const restaurant = ownedRestaurants.find(r => String(r.id) === restaurantId) || null;
    console.log("User selected restaurant:", restaurant);
    setSelectedRestaurant(restaurant);
  };

  // --- Renderizado ---

  // Estado de carga inicial (mientras se obtienen los restaurantes del usuario)
  if (isLoadingRestaurants) {
    return <Layout><div className="p-8">Loading your restaurants...</div></Layout>;
  }

  // Si hubo un error cargando los restaurantes del usuario
  if (errorRestaurants) {
    return <Layout><div className="p-8 text-red-600">Error loading your restaurants: {(errorRestaurants as Error).message}</div></Layout>;
  }

  // Si el usuario no tiene restaurantes
  if (!isLoadingRestaurants && ownedRestaurants.length === 0) {
    return <Layout><div className="p-8">You currently don't manage any restaurants. <Link to="/partner/create-restaurant" className="text-blue-600 hover:underline">Register your first one?</Link></div></Layout>; // Enlace a crear restaurante
  }

  // UI Principal (cuando ya sabemos qué restaurantes tiene)
  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* --- Cabecera y Selector (si aplica) --- */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">          {/* Título y nombre del restaurante seleccionado */}
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Restaurant Dashboard</h1>
            {selectedRestaurant && (
              <p className="text-gray-600 mt-1">{selectedRestaurant.name}</p>
            )}
            {!selectedRestaurant && ownedRestaurants.length > 1 && (
              <p className="text-gray-500 mt-1">Select a restaurant to manage</p>
            )}
          </div>

          {/* Selector si hay más de 1 restaurante */}
          {ownedRestaurants.length > 1 && (
           <div className="w-full md:w-auto md:min-w-[280px] lg:min-w-[320px] space-y-1.5 mt-4 md:mt-0"> {/* Ajusta ancho mínimo y añade margen superior en móvil */}
           {/* 1. Añadir Etiqueta (Label) */}
           <Label htmlFor="restaurant-select" className="text-sm font-medium text-gray-700">
             Gestionando Restaurante:
           </Label>
           <Select
               value={selectedRestaurant ? String(selectedRestaurant.id) : ""}
               onValueChange={handleRestaurantChange}
           >
             {/* 2. Trigger con ID para el Label y altura estándar */}
             <SelectTrigger id="restaurant-select" className="h-10 text-base"> {/* Aumentado text-base */}
               {/* 3. Placeholder (si llegara a usarse) */}
               <SelectValue placeholder="Selecciona tu restaurante..." />
             </SelectTrigger>
             <SelectContent>
               {ownedRestaurants.map((r) => (
                 <SelectItem key={r.id} value={String(r.id)} className="text-base"> {/* Aumentado text-base */}
                   {r.name}
                 </SelectItem>
               ))}
             </SelectContent>
           </Select>
         </div>
       )}

          {/* Botones de acción (deshabilitados si no hay selección) */}
          <div className="mt-4 md:mt-0 space-x-2 self-end md:self-center"> {/* Alineación botones */}
            <Button asChild variant="outline" disabled={!selectedRestaurant}>
              <Link to={selectedRestaurant ? `/restaurants/${selectedRestaurant.id}` : '#'}>
                View Restaurant
              </Link>
            </Button>
            <Button asChild className="bg-food-600 hover:bg-food-700" disabled={!selectedRestaurant}>
              {/* Considera si la ruta de edición necesita el ID */}
              <Link to={selectedRestaurant ? `/dashboard/edit-restaurant/${selectedRestaurant.id}` : '#'}>
                Edit Restaurant
              </Link>
            </Button>
          </div>
        </div> {/* Fin Cabecera */}

        {/* --- Contenido Principal del Dashboard (depende de selectedRestaurant) --- */}
        {!selectedRestaurant ? (
          // Mensaje si aún no se ha seleccionado (y hay > 1)
          <div className="text-center py-16 text-gray-500">
            Please select one of your restaurants above to see the dashboard.
          </div>
        ) : (
          // Muestra el dashboard una vez que hay un restaurante seleccionado
          <>
            {/* Cards de Resumen */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
              {/* Usa isLoadingOrders para mostrar Skeletons si los datos aún no están */}
              <Card>
                <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-gray-500">Pending Orders</CardTitle></CardHeader>
                <CardContent><div className="flex items-center"><Clock className="h-6 w-6 text-yellow-500 mr-2" /><span className="text-3xl font-bold">{isLoadingOrders ? <Skeleton className="h-8 w-12 inline-block" /> : pendingOrders.length}</span></div></CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-gray-500">Completed Orders</CardTitle></CardHeader>
                <CardContent><div className="flex items-center"><CheckCircle className="h-6 w-6 text-green-500 mr-2" /><span className="text-3xl font-bold">{isLoadingOrders ? <Skeleton className="h-8 w-12 inline-block" /> : completedOrders.length}</span></div></CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-gray-500">Total Revenue</CardTitle></CardHeader>
                <CardContent><div className="flex items-center"><DollarSign className="h-6 w-6 text-green-500 mr-2" /><span className="text-3xl font-bold">{isLoadingOrders ? <Skeleton className="h-8 w-20 inline-block" /> : `$${totalRevenue.toFixed(2)}`}</span></div></CardContent>
              </Card>
            </div>

            {/* Pestañas (Tabs) */}
            <Tabs defaultValue="orders" className="w-full">
              <TabsList className="mb-6">
                <TabsTrigger value="orders">Orders</TabsTrigger>
                <TabsTrigger value="products">Products</TabsTrigger>
                <TabsTrigger value="analytics">Analytics</TabsTrigger>
              </TabsList>

              {/* Contenido Pestaña Orders */}
              <TabsContent value="orders">
                {/* Adapta tu tabla/contenido aquí usando 'orders' y 'isLoadingOrders' */}
                <Card>
                  {/* ... tu CardHeader, CardContent con Table ... */}
                </Card>
              </TabsContent>

              {/* Contenido Pestaña Products */}
              <TabsContent value="products">
                {/* Adapta tu tabla/contenido aquí usando 'products' y 'isLoadingProducts' */}
                <Card>
                  {/* ... tu CardHeader, CardContent con Table ... */}
                </Card>
              </TabsContent>

              {/* Contenido Pestaña Analytics */}
              <TabsContent value="analytics">
                {/* Adapta tu contenido aquí */}
                <Card>
                  {/* ... tu CardHeader, CardContent con Gráfica ... */}
                </Card>
              </TabsContent>
            </Tabs>
          </>
        )} {/* Fin bloque condicional selectedRestaurant */}
      </div>
    </Layout>
  );
};

export default RestaurantDashboard; // O tu exportación
