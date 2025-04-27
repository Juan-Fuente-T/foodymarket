import React from 'react';
import { Layout } from "@/components/layout/Layout";
import { useAuth } from "@/hooks/use-auth"; // Asegúrate que la ruta a useAuth es correcta
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Navigate } from "react-router-dom";
import { UserCircle, Mail, Phone, MapPin, UserCog, Calendar } from 'lucide-react'; // Importa iconos

const Profile = () => {
    // Obtener información del usuario y estado de autenticación
    const { user, isAuthenticated, isLoading: authLoading } = useAuth();

    // 1. Manejar estado de carga de autenticación
    if (authLoading) {
        return (
            <Layout>
                <div className="flex justify-center items-center h-screen">
                    {/* Puedes usar un Skeleton más grande o el spinner como en Restaurants */}
                    <Skeleton className="w-32 h-8" /> 
                </div>
            </Layout>
        );
    }

    // 2. Redirigir si no está autenticado
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    // 3. Si está autenticado pero 'user' aún no está disponible (poco probable si isAuthenticated=true)
    //    O si necesitamos cargar más datos del perfil con useQuery (lo añadiremos si es necesario)
    if (!user) {
         return (
            <Layout>
                <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-8">User Profile</h1>
                    <Card>
                        <CardHeader>
                            <Skeleton className="w-48 h-6"/>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <Skeleton className="w-full h-4"/>
                            <Skeleton className="w-full h-4"/>
                            <Skeleton className="w-3/4 h-4"/>
                        </CardContent>
                    </Card>
                </div>
            </Layout>
        );
    }

    // 4. Mostrar la información del perfil (asumiendo que 'user' tiene estos datos)
    //    Ajusta los nombres de campo (user.name, user.email, etc.) según tu objeto 'user' real
    return (
        <Layout>
            <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <h1 className="text-3xl font-bold text-gray-900 mb-8">User Profile</h1>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-xl">Account Information</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="flex items-center space-x-3">
                            <UserCircle className="w-5 h-5 text-gray-500" />
                            <span className="font-medium text-gray-700">Name:</span>
                            <span>{user.name || 'N/A'}</span> 
                        </div>
                        <div className="flex items-center space-x-3">
                            <Mail className="w-5 h-5 text-gray-500" />
                            <span className="font-medium text-gray-700">Email:</span>
                            <span>{user.email || 'N/A'}</span>
                        </div>
                        <div className="flex items-center space-x-3">
                            <UserCog className="w-5 h-5 text-gray-500" />
                            <span className="font-medium text-gray-700">Role:</span>
                            <span className="capitalize">{user.role?.toLowerCase() || 'N/A'}</span> 
                        </div>
                         {/* Asumiendo que la fecha de registro está disponible (puede que necesite API) */}
                         {user.createdAt && ( 
                            <div className="flex items-center space-x-3">
                                <Calendar className="w-5 h-5 text-gray-500" />
                                <span className="font-medium text-gray-700">Member Since:</span>
                                <span>{new Date(user.createdAt).toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' })}</span>
                            </div>
                         )}
                    </CardContent>
                </Card>

                <Card className="mt-6">
                    <CardHeader>
                        <CardTitle className="text-xl">Contact Information</CardTitle>
                        {/* Aquí añadiremos el botón "Edit" más tarde */}
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="flex items-center space-x-3">
                            <Phone className="w-5 h-5 text-gray-500" />
                            <span className="font-medium text-gray-700">Phone:</span>
                            {/* Asumiendo que el teléfono está en user.phone */}
                            <span>{user.phone || 'Not provided'}</span> 
                        </div>
                        <div className="flex items-center space-x-3">
                            <MapPin className="w-5 h-5 text-gray-500" />
                            <span className="font-medium text-gray-700">Address:</span>
                            {/* Asumiendo que la dirección está en user.address */}
                            <span>{user.address || 'Not provided'}</span> 
                        </div>
                    </CardContent>
                </Card>

                {/* Aquí se podrían añadir otras secciones: Cambiar contraseña, Mis pedidos, etc. */}

            </div>
        </Layout>
    );
};

export default Profile;