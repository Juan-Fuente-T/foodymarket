import {
  Avatar,
  Dropdown,
  DropdownDivider,
  DropdownHeader,
  DropdownItem,
  Navbar,
  NavbarBrand,
  NavbarCollapse,
  NavbarLink,
  NavbarToggle,
} from "flowbite-react";
import { auth } from "../../auth";
import 'tailwindcss'; // ??????
import LogoutButton from "@/components/logout-button";
import { Role } from "@/utils/constants";
import { api } from "@/server/service";
import RestaurantCard from "@/components/restaurant-card";
//import Header from "@/components/Header";
//import Head from "next/head";

export default async function Home() {

  const session = await auth();
  const restaurants = await api.restaurant.all()

  const categories = [
    { id: 1, name: "Italiana", image: "/images/italiana.jpg" },
    { id: 2, name: "Japonesa", image: "/images/japonesa.jpg" },
    { id: 3, name: "Mexicana", image: "/images/mexicana.jpg" },
    { id: 4, name: "Vegetariana", image: "/images/vegetariana.jpg" },
    { id: 5, name: "Fast Food", image: "/images/fastfood.jpg" },
  ];

  /*const restaurants = [
    {
      id: 1,
      name: "Pasta Bella",
      category: "Italiana",
      image:
        "https://ixbnnrlngacirjuxztxa.supabase.co/storage/v1/object/public/fotos-c24-39-t-webapp/imagenes-logos/Pizzeria_Italiana_logo.webp",
    },
    {
      id: 2,
      name: "Sushi World",
      category: "Japonesa",
      image:
        "https://ixbnnrlngacirjuxztxa.supabase.co/storage/v1/object/public/fotos-c24-39-t-webapp/imagenes-logos/Sushi_Samurai_logo.webp",
    },
    {
      id: 3,
      name: "Taco Loco",
      category: "Mexicana",
      image:
        "https://ixbnnrlngacirjuxztxa.supabase.co/storage/v1/object/public/fotos-c24-39-t-webapp/imagenes-logos/Sabor_Mexicano_logo.webp",
    },
    {
      id: 4,
      name: "Green Bites",
      category: "Vegetariana",
      image:
        "https://ixbnnrlngacirjuxztxa.supabase.co/storage/v1/object/public/fotos-c24-39-t-webapp/imagenes-logos/Delicias_Veganas_logo.webp",
    },
    {
      id: 5,
      name: "Burger House",
      category: "Fast Food",
      image:
        "https://ixbnnrlngacirjuxztxa.supabase.co/storage/v1/object/public/fotos-c24-39-t-webapp/imagenes-logos/Hamburgueseria_El_Sabor_logo.webp",
    },
  ];

  */
    
  
  return (
    <div className="bg-gray-300">
      <Navbar fluid rounded className=" text-white w-full">
        <NavbarBrand className="" href="/">
          <span className="self-center text-primary font-lobster whitespace-nowrap text-xl">Foody</span>
        </NavbarBrand>
        <div className="flex md:order-2">
          {
            session?.user && (
              <Dropdown arrowIcon={false} 
                  className="hidden md:block"
                  inline label={<Avatar alt="User settings" 
                  img="/user.png" rounded />}>
                <DropdownHeader>
                  <span className="block text-sm">{session?.user?.name}</span>
                  <span className="block truncate text-sm font-medium">{session?.user?.role}</span>
                </DropdownHeader>
                {
                  session?.user?.role === Role.Restaurant ? (
                    <DropdownItem href="/dashboard">
                      Panel de administración
                    </DropdownItem>) :
                    <DropdownItem href="/perfil">
                      Perfil
                    </DropdownItem>
                }
                <DropdownItem>Settings</DropdownItem>
                <DropdownDivider />
                <DropdownItem as={LogoutButton}>
                  Cerrar sesión
                </DropdownItem>
              </Dropdown>
            )
          }
          <NavbarToggle />
        </div>
        <NavbarCollapse className="text-3xl hover:text-accent font-bold">
          <NavbarLink href="#" active>
            Inicio
          </NavbarLink>
          <NavbarLink href="#">Categorías</NavbarLink>
          <NavbarLink href="#">Restaurantes</NavbarLink>
          <NavbarLink href="#">Contacto</NavbarLink>
          {session?.user?.role === Role.Restaurant ? 
            <NavbarLink className="block md:hidden" href="/dashboard">
              Panel de administración
            </NavbarLink>:
            <NavbarLink className="block md:hidden" href="/perfil">
              Perfil
            </NavbarLink>
          }
          {session?.user && <span className="block md:hidden"><LogoutButton /></span>}
        </NavbarCollapse>
      </Navbar>

      <main className="container mx-auto p-6">
        <h1 className="text-3xl text-primary p-10 font-bold text-center mb-6">Explora Categorías</h1>
        <div className="flex flex-wrap justify-center gap-4 p-10">
          {categories.map((category) => (
            <button
              key={category.id}
              className="px-20 py-10 bg-secondary text-primary text-lg rounded-lg hover:bg-primary hover:text-secondary transition"
              // aca colocar una funcion en onclick para el filtrado por categorias
            >
              {category.name}
            </button>
          ))}
        </div>

        <h1 className="text-3xl text-primary font-bold text-center my-6">Restaurantes</h1>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
          {restaurants.map((restaurant) => (
            <RestaurantCard restaurant={restaurant} key={restaurant.rst_id} />
          ))}
        </div>
      </main>

      <footer className="bg-accent w-full text-white text-center p-4 mt-6">
        &copy; 2025 Foody. Todos los derechos reservados.
      </footer>
    </div>
  );
}
