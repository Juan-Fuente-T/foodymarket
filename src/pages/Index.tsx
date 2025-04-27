
import React from "react";
import { Layout } from "@/components/layout/Layout";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { restaurantAPI } from "@/services/api";
import { RestaurantCard } from "@/components/restaurant/RestaurantCard";
import { Skeleton } from "@/components/ui/skeleton";
import "../index.css";

export default function Home() {
  const { data: restaurants = [], isLoading: isLoadingRestaurants } = useQuery({
    queryKey: ["restaurants"],
    queryFn: () => restaurantAPI.getAll(),
  });

  return (
    <Layout>
      {/* Hero section */}
      {/* <section className="relative bg-food-50 overflow-hidden">*/}
      <section className="relative bg-gray-800 overflow-hidden">
        {/* Imagen de fondo (ajusta la URL y el posicionamiento) */}
        <div
          // className="absolute inset-0 bg-[url('/images/hero-background-food.webp')] bg-cover bg-center" 
          className="absolute inset-0 bg-[url('/public/hero-background-food.webp')] bg-cover bg-center h-120"
          aria-hidden="true"
        />
        {/* Overlay semitransparente para legibilidad del texto */}
        <div
          className="absolute inset-0 bg-black/40 sm:bg-gradient-to-r from-black/60 via-black/30 to-transparent"
          aria-hidden="true"
        />
        <div className="custom-shape-divider-bottom">
          <svg data-name="Layer 1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 120" preserveAspectRatio="none">
            <path d="M0,0V46.29c47.79,22.2,103.59,32.17,158,28,70.36-5.37,136.33-33.31,206.8-37.5C438.64,32.43,512.34,53.67,583,72.05c69.27,18,138.3,24.88,209.4,13.08,36.15-6,69.85-17.84,104.45-29.34C989.49,25,1113-14.29,1200,52.47V0Z" opacity=".25" className="shape-fill"></path>
            <path d="M0,0V15.81C13,36.92,27.64,56.86,47.69,72.05,99.41,111.27,165,111,224.58,91.58c31.15-10.15,60.09-26.07,89.67-39.8,40.92-19,84.73-46,130.83-49.67,36.26-2.85,70.9,9.42,98.6,31.56,31.77,25.39,62.32,62,103.63,73,40.44,10.79,81.35-6.69,119.13-24.28s75.16-39,116.92-43.05c59.73-5.85,113.28,22.88,168.9,38.84,30.2,8.66,59,6.17,87.09-7.5,22.43-10.89,48-26.93,60.65-49.24V0Z" opacity=".5" className="shape-fill"></path>
            <path d="M0,0V5.63C149.93,59,314.09,71.32,475.83,42.57c43-7.64,84.23-20.12,127.61-26.46,59-8.63,112.48,12.24,165.56,35.4C827.93,77.22,886,95.24,951.2,90c86.53-7,172.46-45.71,248.8-84.81V0Z" className="shape-fill"></path>
          </svg>
        </div>
        <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="pt-16 pb-56 sm:pb-64 lg:pb-72">
            <main className="mt-8 sm:mt-16 md:mt-24 lg:mt-32">
              <div className="text-center">
                <h1 className="text-4xl tracking-tight font-extrabold text-gray-900 sm:text-5xl md:text-6xl">
                  <span className="block xl:inline text-food-200">Delicious food, delivered to </span>
                  <span className="block text-food-600 xl:inline">your door</span>
                </h1>
                <p className="mt-3 max-w-md mx-auto text-base text-food-100 sm:text-lg md:mt-5 md:text-xl md:max-w-3xl">
                  Order from the best restaurants in town and get your food delivered quickly and easily.
                </p>
                <div className="mt-5 sm:mt-8 flex justify-center mb-36">
                  <div className="rounded-md shadow-lg">
                    <Link to="/restaurants">
                      <Button className="w-full flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-white bg-food-600 hover:bg-food-700 md:py-4 md:text-lg md:px-10">
                        Order Now
                      </Button>
                    </Link>
                  </div>
                </div>
              </div>
            </main>
          </div>
        </div>
        {/* <div className="absolute inset-x-0 bottom-0 h-32 bg-gray-100"></div> */}
      </section>
      {/* Featured restaurants section */}
      <section className="py-12 md:py-20 bg-white mt-[5rem]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-extrabold text-gray-900 mb-8">Featured Restaurants</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {isLoadingRestaurants ? (
              Array(6).fill(0).map((_, i) => (
                <Skeleton key={i} className="h-72 rounded-xl" />
              ))
            ) : (
              restaurants.slice(0, 6).map(restaurant => (
                <RestaurantCard key={restaurant.id} restaurant={restaurant} />
              ))
            )}
          </div>
          <div className="mt-8 text-center ">
            <Link to="/restaurants">
              <Button className="px-8 py-3 text-base font-medium rounded-md text-white bg-food-600 hover:bg-food-700 md:py-4 md:text-lg md:px-10">
                View All Restaurants
                </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* How it works section */}
      <section className="relative py-12 md:py-20 overflow-hidden">
        {/* --- Capa 1 (Fondo): Imagen --- */}
        <div
          className="absolute inset-0 bg-[url('/public/hero-background-food.webp')] bg-cover bg-center -z-10" // URL de tu imagen. z-index negativo para estar detrás
          aria-hidden="true"
        />
        {/* --- Capa 2 (Intermedia): Overlay de Color Semi-Transparente --- */}
        <div
          // (ej: /90 = 90% opaco, /80 = 80% opaco)
          className="absolute inset-0 bg-food-50/90 z-0" // z-index 0 (entre imagen y contenido)
          aria-hidden="true"
        />
        <div className="relative zx-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-12">
            <h2 className="text-3xl font-extrabold text-gray-900 mb-4">How It Works</h2>
            <p className="text-xl font-semibold text-gray-600">
              Get your favorite food in just a few simple steps.
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center p-6 bg-food-50/20 rounded-lg shadow-md">
              <div className="w-28 h-28 bg-food-300 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="h-12 w-12 text-food-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold mb-2">Find a Restaurant</h3>
              <p className="text-gray-600 text-lg">Browse restaurants near you and explore their menus.</p>
            </div>
            <div className="text-center p-6 bg-food-50/20 rounded-lg shadow-md">
              <div className="w-28 h-28 bg-food-300 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="h-12 w-12 text-food-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold mb-2">Place Your Order</h3>
              <p className="text-gray-600 text-lg">Add your favorite items to your cart and place your order.</p>
            </div>
            <div className="text-center p-6 bg-food-50/20 rounded-lg shadow-md">
              <div className="w-28 h-28 bg-food-300 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="h-12 w-12 text-food-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold mb-2">Enjoy Your Meal</h3>
              <p className="text-gray-600 text-lg">Your order will be delivered to your door in no time.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Partner section */}
      <section className="py-12 md:py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-12">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              Become a Restaurant Partner
            </h2>
            <p className="text-lg text-gray-600">
              Grow your business, reach new customers, and increase your revenue by partnering with us.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-white rounded-lg shadow-md p-6 text-center">
              <div className="w-20 h-20 bg-food-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-food-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold mb-2">Reach More Customers</h3>
              <p className="text-gray-600">Expand your customer base and serve more people in your area.</p>
            </div>

            <div className="bg-white rounded-lg shadow-md p-6 text-center">
              <div className="w-20 h-20 bg-food-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8  text-food-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold mb-2">Increase Revenue</h3>
              <p className="text-gray-600">Boost your sales with online ordering and delivery services.</p>
            </div>

            <div className="bg-white rounded-lg shadow-md p-6 text-center">
              <div className="w-20 h-20 bg-food-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8  text-food-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold mb-2">Easy Management</h3>
              <p className="text-gray-600">Manage your menu, orders, and customer feedback all in one place.</p>
            </div>
          </div>

          <div className="text-center mt-10">
          <Button className="px-8 py-3 text-base font-medium rounded-md text-white bg-food-600 hover:bg-food-700 md:py-4 md:text-lg md:px-10">
              <Link to="/partner">
                Join as a Partner
              </Link>
            </Button>
          </div>
        </div>
      </section>

      {/* Testimonials section */}
      <section className="py-12 md:py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-12">
            <h2 className="text-3xl font-extrabold text-gray-900 mb-4">What Our Customers Say</h2>
            <p className="text-lg text-gray-600">
              Read what our customers have to say about their experience with FoodDelivery.
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div className="bg-white rounded-lg shadow-md p-6">
              <div className="flex items-center mb-4">
                <img
                  className="w-20 h-20 rounded-full mr-4 object-cover"
                  src="public/JaneDoe_Photo.webp"
                  alt="Customer avatar"
                />
                <div>
                  <h4 className="text-lg font-semibold">Jane Doe</h4>
                  <p className="text-gray-500">Customer</p>
                </div>
              </div>
              <p className="text-gray-700">
                "I love FoodDelivery! It's so easy to order from my favorite restaurants and the delivery is always fast."
              </p>
            </div>
            <div className="bg-white rounded-lg shadow-md p-6">
              <div className="flex items-center mb-4">
                <img
                  className="w-20 h-20 rounded-full mr-4 object-cover"
                  src="public/JohnSmith_Photo.webp"
                  alt="Customer avatar"
                />
                <div>
                  <h4 className="text-lg font-semibold">John Smith</h4>
                  <p className="text-gray-500">Customer</p>
                </div>
              </div>
              <p className="text-gray-700">
                "FoodDelivery has made my life so much easier. I can order food from anywhere and have it delivered right to my door."
              </p>
            </div>
          </div>
        </div>
      </section>

    </Layout>
  );
}
