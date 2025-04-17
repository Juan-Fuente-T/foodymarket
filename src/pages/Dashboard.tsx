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
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";
import { Skeleton } from "@/components/ui/skeleton";
import { 
  CheckCircle, Clock, PlusCircle, RefreshCcw, TrendingUp, 
  Users, DollarSign, ShoppingBag, User, Settings, 
  Edit, Trash2, Package, Star
} from "lucide-react";
import { Restaurant } from "@/types/models";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart";

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
                    <TableRow key={order?.id}>
                      <TableCell className="font-medium">{order.id.slice(0, 8)}</TableCell>
                      <TableCell>
                        {new Date(order.createdAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell>{order.restaurantId}</TableCell>
                      <TableCell>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${order?.status === 'delivered' ? 'bg-green-100 text-green-800' :
                            order.status === 'preparing' ? 'bg-blue-100 text-blue-800' :
                              order.status === 'pending' ? 'bg-yellow-100 text-yellow-800' :
                                'bg-gray-100 text-gray-800'
                          }`}>
                          {order.status.charAt(0).toUpperCase() + order.status.slice(1)}
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

  // Query for getting restaurants owned by the user
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

  // Auto-select first restaurant if there's only one or select the first one if there are multiple
  useEffect(() => {
    if (selectedRestaurant || isLoadingRestaurants || errorRestaurants) return;

    if (ownedRestaurants.length >= 1) {
      console.log("Auto-selecting restaurant:", ownedRestaurants[0]);
      setSelectedRestaurant(ownedRestaurants[0]);
    } else {
      setSelectedRestaurant(null);
    }
  }, [ownedRestaurants, isLoadingRestaurants, errorRestaurants]);

  // Dependent queries based on the selected restaurant
  const { data: orders = [], isLoading: isLoadingOrders } = useQuery({
    queryKey: ["restaurantOrders", selectedRestaurant?.id],
    queryFn: () => orderAPI.getByRestaurant(selectedRestaurant!.id.toString()),
    enabled: !!selectedRestaurant?.id,
  });

  const { data: products = [], isLoading: isLoadingProducts } = useQuery({
    queryKey: ["restaurantProducts", selectedRestaurant?.id],
    // queryFn: () => productAPI.getByRestaurant(selectedRestaurant!.id.toString()),
    queryFn: () => productAPI.getByRestaurantAndCategory(selectedRestaurant!.id.toString()),
    enabled: !!selectedRestaurant?.id,
  });

  // Order and revenue calculations
  const pendingOrders = useMemo(() => orders.filter((o: any) => o.status === 'pending' || o.status === 'preparing'), [orders]);
  const completedOrders = useMemo(() => orders.filter((o: any) => o.status === 'delivered' || o.status === 'completed'), [orders]);
  const totalRevenue = useMemo(() => completedOrders.reduce((sum: number, o: any) => sum + (o.total || 0), 0), [completedOrders]);

  // Handler for restaurant selection change
  const handleRestaurantChange = (restaurantId: string) => {
    const restaurant = ownedRestaurants.find(r => String(r.id) === restaurantId) || null;
    console.log("User selected restaurant:", restaurant);
    setSelectedRestaurant(restaurant);
  };

  // Prepare data for charts
  const orderStatusData = useMemo(() => {
    const statusCounts: {[key: string]: number} = {
      pending: 0,
      preparing: 0,
      delivered: 0,
      completed: 0,
      cancelled: 0
    };
    
    orders.forEach((order: any) => {
      if (order?.status in statusCounts) {
        statusCounts[order.status]++;
      }
    });
    
    return Object.entries(statusCounts).map(([status, count]) => ({
      name: status.charAt(0).toUpperCase() + status.slice(1),
      value: count
    }));
  }, [orders]);

  const revenueData = useMemo(() => {
    // This is a simplified example - in a real app you might want to group by day/week/month
    const last7Days = Array.from({ length: 7 }, (_, i) => {
      const date = new Date();
      date.setDate(date.getDate() - i);
      return date.toISOString().split('T')[0];
    }).reverse();
    
    const dailyRevenue: {[key: string]: number} = {};
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

  // Colors for charts
  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

  // Loading state while fetching restaurants
  if (isLoadingRestaurants) {
    return <Layout><div className="p-8">Loading your restaurants...</div></Layout>;
  }

  // Error handling
  if (errorRestaurants) {
    return <Layout><div className="p-8 text-red-600">Error loading your restaurants: {errorRestaurants.message}</div></Layout>;
  }

  // No restaurants message
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

  // Main dashboard UI
  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header and Restaurant Selector */}
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

          {/* Restaurant Selector Dropdown */}
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

          {/* Action Buttons */}
          <div className="mt-4 md:mt-0 space-x-2 self-end md:self-center">
            <Button asChild variant="outline" disabled={!selectedRestaurant}>
              <Link to={selectedRestaurant ? `/restaurants/${selectedRestaurant.id}` : '#'}>
                View Restaurant
              </Link>
            </Button>
            <Button asChild className="bg-food-600 hover:bg-food-700" disabled={!selectedRestaurant}>
              <Link to={selectedRestaurant ? `/dashboard/edit-restaurant/${selectedRestaurant.id}` : '#'}>
                Edit Restaurant
              </Link>
            </Button>
          </div>
        </div>

        {/* Dashboard Content */}
        {!selectedRestaurant ? (
          <div className="text-center py-16 text-gray-500">
            Please select one of your restaurants to see the dashboard.
          </div>
        ) : (
          <>
            {/* Metric Cards */}
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
                      {isLoadingProducts ? <Skeleton className="h-8 w-12 inline-block" /> : products.length}
                    </span>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Main Content Tabs */}
            <Tabs defaultValue="overview" className="w-full">
              <TabsList className="mb-6">
                <TabsTrigger value="overview">Overview</TabsTrigger>
                <TabsTrigger value="orders">Orders</TabsTrigger>
                <TabsTrigger value="products">Products</TabsTrigger>
                <TabsTrigger value="analytics">Analytics</TabsTrigger>
              </TabsList>

              {/* Overview Tab */}
              <TabsContent value="overview">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Revenue Chart */}
                  <Card>
                    <CardHeader>
                      <CardTitle>Revenue (Last 7 Days)</CardTitle>
                    </CardHeader>
                    <CardContent className="h-80">
                      {isLoadingOrders ? (
                        <Skeleton className="w-full h-full" />
                      ) : (
                        <ResponsiveContainer width="100%" height="100%">
                          <BarChart data={revenueData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="date" />
                            <YAxis />
                            <RechartsTooltip />
                            <Bar dataKey="amount" fill="#9b87f5" radius={[4, 4, 0, 0]} />
                          </BarChart>
                        </ResponsiveContainer>
                      )}
                    </CardContent>
                  </Card>

                  {/* Order Status Chart */}
                  <Card>
                    <CardHeader>
                      <CardTitle>Order Status</CardTitle>
                    </CardHeader>
                    <CardContent className="h-80">
                      {isLoadingOrders ? (
                        <Skeleton className="w-full h-full" />
                      ) : (
                        <ResponsiveContainer width="100%" height="100%">
                          <PieChart>
                            <Pie
                              data={orderStatusData}
                              cx="50%"
                              cy="50%"
                              labelLine={false}
                              outerRadius={80}
                              fill="#8884d8"
                              dataKey="value"
                              label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                            >
                              {orderStatusData.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                              ))}
                            </Pie>
                            <RechartsTooltip />
                          </PieChart>
                        </ResponsiveContainer>
                      )}
                    </CardContent>
                  </Card>
                </div>

                {/* Recent Orders Table */}
                <Card className="mt-6">
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
                            <TableHead>Customer</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead className="text-right">Amount</TableHead>
                            <TableHead></TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {orders.slice(0, 5).map((order: any) => (
                            <TableRow key={order?.id}>
                              <TableCell className="font-medium">{order.id.slice(0, 8)}</TableCell>
                              <TableCell>
                                {new Date(order.createdAt).toLocaleDateString()}
                              </TableCell>
                              <TableCell>{order.clientId || "Customer"}</TableCell>
                              <TableCell>
                                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                  order.status === 'delivered' ? 'bg-green-100 text-green-800' :
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
                        <p className="text-gray-600 mb-4">This restaurant hasn't received any orders yet.</p>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              {/* Orders Tab */}
              <TabsContent value="orders">
                <Card>
                  <CardHeader>
                    <CardTitle>All Orders</CardTitle>
                    <CardDescription>Manage your restaurant orders</CardDescription>
                  </CardHeader>
                  <CardContent>
                    {isLoadingOrders ? (
                      <div className="space-y-4">
                        {Array(8).fill(0).map((_, i) => (
                          <Skeleton key={i} className="h-12 w-full" />
                        ))}
                      </div>
                    ) : orders.length > 0 ? (
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Order ID</TableHead>
                            <TableHead>Date</TableHead>
                            <TableHead>Customer</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead className="text-right">Amount</TableHead>
                            <TableHead>Actions</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {orders.map((order: any) => (
                            <TableRow key={order?.id}>
                              <TableCell className="font-medium">{order.id.slice(0, 8)}</TableCell>
                              <TableCell>
                                {new Date(order.createdAt).toLocaleDateString()}
                              </TableCell>
                              <TableCell>{order.clientId || "Customer"}</TableCell>
                              <TableCell>
                                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                  order.status === 'delivered' ? 'bg-green-100 text-green-800' :
                                  order.status === 'preparing' ? 'bg-blue-100 text-blue-800' :
                                  order.status === 'pending' ? 'bg-yellow-100 text-yellow-800' :
                                  'bg-gray-100 text-gray-800'
                                }`}>
                                  {order.status.charAt(0).toUpperCase() + order.status.slice(1)}
                                </span>
                              </TableCell>
                              <TableCell className="text-right">${order.total.toFixed(2)}</TableCell>
                              <TableCell>
                                <div className="flex gap-2">
                                  <Button variant="ghost" size="sm">View</Button>
                                  {(order.status === 'pending' || order.status === 'preparing') && (
                                    <Button variant="outline" size="sm">Update Status</Button>
                                  )}
                                </div>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    ) : (
                      <div className="text-center py-8">
                        <p className="text-gray-600 mb-4">This restaurant hasn't received any orders yet.</p>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              {/* Products Tab */}
              <TabsContent value="products">
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between">
                    <div>
                      <CardTitle>Menu Products</CardTitle>
                      <CardDescription>Manage your restaurant's menu items</CardDescription>
                    </div>
                    <Button className="bg-food-600 hover:bg-food-700">
                      <PlusCircle className="mr-2 h-4 w-4" /> Add New Product
                    </Button>
                  </CardHeader>
                  <CardContent>
                    {isLoadingProducts ? (
                      <div className="space-y-4">
                        {Array(8).fill(0).map((_, i) => (
                          <Skeleton key={i} className="h-12 w-full" />
                        ))}
                      </div>
                    ) : products.length > 0 ? (
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Image</TableHead>
                            <TableHead>Name</TableHead>
                            <TableHead>Category</TableHead>
                            <TableHead>Price</TableHead>
                            <TableHead>Available</TableHead>
                            <TableHead>Actions</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {products.map((product: any) => (
                            <TableRow key={product.id}>
                              <TableCell>
                                <div className="w-10 h-10 rounded-md overflow-hidden bg-gray-100">
                                  {product.image ? (
                                    <img 
                                      src={product.image} 
                                      alt={product.name} 
                                      className="w-full h-full object-cover"
                                    />
                                  ) : (
                                    <div className="flex items-center justify-center w-full h-full text-gray-400">
                                      <Package className="h-5 w-5" />
                                    </div>
                                  )}
                                </div>
                              </TableCell>
                              <TableCell className="font-medium">{product.name}</TableCell>
                              <TableCell>{product.category || "Uncategorized"}</TableCell>
                              <TableCell>${product.price?.toFixed(2)}</TableCell>
                              <TableCell>
                                {product.available ? (
                                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                                    Available
                                  </span>
                                ) : (
                                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                                    Unavailable
                                  </span>
                                )}
                              </TableCell>
                              <TableCell>
                                <div className="flex items-center gap-2">
                                  <Button variant="ghost" size="sm">
                                    <Edit className="h-4 w-4" />
                                  </Button>
                                  <Button variant="ghost" size="sm">
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
                        <p className="text-gray-600 mb-4">This restaurant doesn't have any products yet.</p>
                        <Button className="bg-food-600 hover:bg-food-700">
                          <PlusCircle className="mr-2 h-4 w-4" /> Add Your First Product
                        </Button>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              {/* Analytics Tab */}
              <TabsContent value="analytics">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <Card>
                    <CardHeader>
                      <CardTitle>Popular Products</CardTitle>
                    </CardHeader>
                    <CardContent className="h-80">
                      {isLoadingProducts || isLoadingOrders ? (
                        <Skeleton className="w-full h-full" />
                      ) : products.length > 0 ? (
                        <ResponsiveContainer width="100%" height="100%">
                          <BarChart
                            data={products.slice(0, 5).map((p: any, index: number) => ({
                              name: p.name,
                              orders: Math.floor(Math.random() * 50) + 5 // Example random data
                            }))}
                            layout="vertical"
                          >
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis type="number" />
                            <YAxis dataKey="name" type="category" width={100} />
                            <RechartsTooltip />
                            <Bar dataKey="orders" fill="#8884d8" radius={[0, 4, 4, 0]} />
                          </BarChart>
                        </ResponsiveContainer>
                      ) : (
                        <div className="flex items-center justify-center h-full text-gray-500">
                          No product data available
                        </div>
                      )}
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle>Customer Growth</CardTitle>
                    </CardHeader>
                    <CardContent className="h-80">
                      {isLoadingOrders ? (
                        <Skeleton className="w-full h-full" />
                      ) : (
                        <ResponsiveContainer width="100%" height="100%">
                          <BarChart
                            data={[
                              { month: 'Jan', customers: 10 },
                              { month: 'Feb', customers: 15 },
                              { month: 'Mar', customers: 25 },
                              { month: 'Apr', customers: 30 },
                              { month: 'May', customers: 40 },
                              { month: 'Jun', customers: 55 },
                            ]}
                          >
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="month" />
                            <YAxis />
                            <RechartsTooltip />
                            <Bar dataKey="customers" fill="#82ca9d" radius={[4, 4, 0, 0]} />
                          </BarChart>
                        </ResponsiveContainer>
                      )}
                    </CardContent>
                  </Card>
                </div>
              </TabsContent>
            </Tabs>
          </>
        )}
      </div>
    </Layout>
  );
};

export default Dashboard;
