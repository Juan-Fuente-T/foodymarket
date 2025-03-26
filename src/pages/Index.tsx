
import React, { useState } from "react";
import { Layout } from "@/components/layout/Layout";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { RestaurantCard } from "@/components/restaurant/RestaurantCard";
import { CategoryCard } from "@/components/restaurant/CategoryCard";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { useQuery } from "@tanstack/react-query";
import { restaurantAPI, categoryAPI } from "@/services/api";
import { Restaurant, Category } from "@/types/models";

const Index = () => {
  const [searchTerm, setSearchTerm] = useState("");

  const { data: featuredRestaurants = [], isLoading: isLoadingRestaurants } = useQuery({
    queryKey: ["featuredRestaurants"],
    queryFn: () => restaurantAPI.getFeatured(),
  });

  const { data: categories = [], isLoading: isLoadingCategories } = useQuery({
    queryKey: ["categories"],
    queryFn: () => categoryAPI.getAll(),
  });

  // Mock data for demonstration
  const mockRestaurants: Restaurant[] = [
    {
      id: "1",
      name: "Burger Palace",
      description: "The best burgers in town, made with premium ingredients and love.",
      address: "123 Main St, Anytown",
      phone: "555-1234",
      email: "info@burgerpalace.com",
      logo: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      coverImage: "https://images.unsplash.com/photo-1561758033-d89a9ad46330?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      rating: 4.8,
      reviewCount: 234,
      categories: [
        { id: "1", name: "Burgers", description: "Juicy burgers for all tastes" },
        { id: "2", name: "American", description: "Classic American cuisine" }
      ],
      ownerId: "owner1",
      openingHours: "9 AM - 10 PM",
      createdAt: "2023-01-01",
      updatedAt: "2023-05-15"
    },
    {
      id: "2",
      name: "Pizza Heaven",
      description: "Authentic Italian pizzas made in wood-fired ovens imported from Naples.",
      address: "456 Elm St, Anytown",
      phone: "555-5678",
      email: "hello@pizzaheaven.com",
      logo: "https://images.unsplash.com/photo-1590947132387-155cc02f3212?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      coverImage: "https://images.unsplash.com/photo-1513104890138-7c749659a591?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      rating: 4.6,
      reviewCount: 198,
      categories: [
        { id: "3", name: "Pizza", description: "Delicious pizzas from around the world" },
        { id: "4", name: "Italian", description: "Traditional Italian cuisine" }
      ],
      ownerId: "owner2",
      openingHours: "11 AM - 11 PM",
      createdAt: "2023-02-15",
      updatedAt: "2023-06-20"
    },
    {
      id: "3",
      name: "Sushi Ocean",
      description: "Fresh and delicious sushi prepared by expert Japanese chefs.",
      address: "789 Oak St, Anytown",
      phone: "555-9012",
      email: "info@sushiocean.com",
      logo: "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      coverImage: "https://images.unsplash.com/photo-1611143669185-af224c5e3252?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80",
      rating: 4.9,
      reviewCount: 312,
      categories: [
        { id: "5", name: "Sushi", description: "Fresh and authentic sushi" },
        { id: "6", name: "Japanese", description: "Traditional Japanese cuisine" }
      ],
      ownerId: "owner3",
      openingHours: "12 PM - 10 PM",
      createdAt: "2023-03-10",
      updatedAt: "2023-07-05"
    }
  ];

  const mockCategories: Category[] = [
    {
      id: "1",
      name: "Burgers",
      description: "Juicy burgers for all tastes",
      image: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80"
    },
    {
      id: "3",
      name: "Pizza",
      description: "Delicious pizzas from around the world",
      image: "https://images.unsplash.com/photo-1513104890138-7c749659a591?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80"
    },
    {
      id: "5",
      name: "Sushi",
      description: "Fresh and authentic sushi",
      image: "https://images.unsplash.com/photo-1611143669185-af224c5e3252?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80"
    },
    {
      id: "7",
      name: "Mexican",
      description: "Spicy and flavorful Mexican dishes",
      image: "https://images.unsplash.com/photo-1615870216519-2f9fa575fa5c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80"
    }
  ];

  const displayedRestaurants = 
    featuredRestaurants.length > 0 ? featuredRestaurants : mockRestaurants;
  
  const displayedCategories = 
    categories.length > 0 ? categories : mockCategories;

  return (
    <Layout>
      {/* Hero Section */}
      <section className="hero-section">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 md:py-24">
          <div className="max-w-3xl mx-auto text-center">
            <h1 className="animate-fade-in text-4xl md:text-5xl lg:text-6xl font-bold text-gray-900 mb-6">
              Delicious Food, 
              <span className="text-food-600"> Delivered Fast</span>
            </h1>
            <p className="animate-fade-in animation-delay-100 text-xl text-gray-600 mb-8">
              Order from your favorite local restaurants with free delivery on your first order.
            </p>
            
            <div className="animate-fade-in animation-delay-200 flex flex-col sm:flex-row justify-center space-y-4 sm:space-y-0 sm:space-x-4 mb-8">
              <div className="relative w-full sm:w-auto sm:flex-grow max-w-xl">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <Input
                  type="text"
                  placeholder="Search for restaurants or food..."
                  className="pl-10 pr-4 py-6 w-full rounded-full shadow-sm border border-gray-200 focus:border-food-500 focus:ring-food-500"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <Button 
                asChild
                size="lg" 
                className="bg-food-600 hover:bg-food-700 text-white rounded-full py-6 button-hover"
              >
                <Link to={`/restaurants${searchTerm ? `?search=${searchTerm}` : ''}`}>
                  Find Food
                </Link>
              </Button>
            </div>
          </div>
        </div>
      </section>

      {/* Categories Section */}
      <section className="py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900">
              Browse By Category
            </h2>
            <Button asChild variant="ghost" className="text-food-600 hover:text-food-700">
              <Link to="/categories">View All</Link>
            </Button>
          </div>
          
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 md:gap-6">
            {displayedCategories.map((category) => (
              <CategoryCard key={category.id} category={category} />
            ))}
          </div>
        </div>
      </section>

      {/* Featured Restaurants Section */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900">
              Popular Restaurants
            </h2>
            <Button asChild variant="ghost" className="text-food-600 hover:text-food-700">
              <Link to="/restaurants">View All</Link>
            </Button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {displayedRestaurants.map((restaurant) => (
              <RestaurantCard key={restaurant.id} restaurant={restaurant} />
            ))}
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="py-16 bg-gradient-to-b from-white to-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900 mb-4">
              How It Works
            </h2>
            <p className="text-lg text-gray-600 max-w-3xl mx-auto">
              Order your favorite food in just a few easy steps
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Step 1 */}
            <div className="flex flex-col items-center text-center p-6 hover:transform hover:scale-105 transition-transform duration-300">
              <div className="w-16 h-16 bg-food-100 rounded-full flex items-center justify-center mb-4">
                <span className="text-food-600 text-2xl font-bold">1</span>
              </div>
              <h3 className="text-xl font-semibold mb-2">Browse Restaurants</h3>
              <p className="text-gray-600">
                Find your favorite restaurants or discover new ones nearby
              </p>
            </div>

            {/* Step 2 */}
            <div className="flex flex-col items-center text-center p-6 hover:transform hover:scale-105 transition-transform duration-300">
              <div className="w-16 h-16 bg-food-100 rounded-full flex items-center justify-center mb-4">
                <span className="text-food-600 text-2xl font-bold">2</span>
              </div>
              <h3 className="text-xl font-semibold mb-2">Choose Your Food</h3>
              <p className="text-gray-600">
                Select from a wide variety of delicious meals and add to cart
              </p>
            </div>

            {/* Step 3 */}
            <div className="flex flex-col items-center text-center p-6 hover:transform hover:scale-105 transition-transform duration-300">
              <div className="w-16 h-16 bg-food-100 rounded-full flex items-center justify-center mb-4">
                <span className="text-food-600 text-2xl font-bold">3</span>
              </div>
              <h3 className="text-xl font-semibold mb-2">Enjoy Your Meal</h3>
              <p className="text-gray-600">
                Your food will be prepared and delivered right to your door
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-food-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
            <div className="grid grid-cols-1 md:grid-cols-2">
              <div className="p-8 md:p-12 flex flex-col justify-center">
                <h2 className="text-2xl md:text-3xl font-bold text-gray-900 mb-4">
                  Become a Restaurant Partner
                </h2>
                <p className="text-lg text-gray-600 mb-6">
                  Reach more customers, grow your business and let us handle the delivery. Join our network of restaurant partners today.
                </p>
                <div>
                  <Button 
                    asChild
                    size="lg"
                    className="bg-food-600 hover:bg-food-700 text-white button-hover"
                  >
                    <Link to="/partner">
                      Join Now
                    </Link>
                  </Button>
                </div>
              </div>
              <div className="relative h-64 md:h-auto">
                <img
                  src="https://images.unsplash.com/photo-1484972759836-b93f9b778036?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1000&q=80"
                  alt="Restaurant kitchen"
                  className="absolute inset-0 w-full h-full object-cover"
                />
              </div>
            </div>
          </div>
        </div>
      </section>
    </Layout>
  );
};

export default Index;
