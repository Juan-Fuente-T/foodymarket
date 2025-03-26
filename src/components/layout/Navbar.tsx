
import React, { useState, useEffect } from "react";
import { Link, useLocation } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useAuth } from "@/contexts/AuthContext";
import { useCart } from "@/contexts/CartContext";
import { Menu, X, ShoppingBag, User, LogOut, Home, ChefHat, Settings } from "lucide-react";
import { Badge } from "@/components/ui/badge";

export function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const { user, logout, isAuthenticated } = useAuth();
  const { totalItems } = useCart();
  const location = useLocation();

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 10);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const navLinks = [
    { name: "Home", path: "/" },
    { name: "Restaurants", path: "/restaurants" },
    { name: "Categories", path: "/categories" },
  ];

  // Add owner dashboard link for restaurant owners
  if (user?.role === "owner") {
    navLinks.push({ name: "My Restaurant", path: "/dashboard" });
  }

  const closeMenu = () => setIsMenuOpen(false);

  return (
    <header
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        isScrolled ? "bg-white/90 backdrop-blur-md shadow-sm" : "bg-transparent"
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link 
            to="/" 
            className="flex items-center space-x-2 text-food-600 font-bold text-xl"
            onClick={closeMenu}
          >
            <span className="flex items-center">
              <ChefHat className="h-6 w-6 mr-2" />
              FoodDelivery
            </span>
          </Link>

          {/* Desktop navigation */}
          <nav className="hidden md:flex items-center space-x-8">
            {navLinks.map((link) => (
              <Link
                key={link.path}
                to={link.path}
                className={`nav-item text-base font-medium transition-colors ${
                  location.pathname === link.path
                    ? "text-food-600 active" 
                    : "text-gray-700 hover:text-food-500"
                }`}
              >
                {link.name}
              </Link>
            ))}
          </nav>

          {/* Desktop right actions */}
          <div className="hidden md:flex items-center space-x-4">
            <Link to="/cart">
              <Button
                variant="ghost"
                className="relative p-2"
                aria-label="Shopping cart"
              >
                <ShoppingBag className="h-5 w-5" />
                {totalItems > 0 && (
                  <Badge
                    variant="destructive"
                    className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs"
                  >
                    {totalItems}
                  </Badge>
                )}
              </Button>
            </Link>

            {isAuthenticated ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="rounded-full p-0">
                    <Avatar className="h-8 w-8">
                      <AvatarImage src={user?.avatar} alt={user?.name} />
                      <AvatarFallback className="bg-food-200 text-food-700">
                        {user?.name
                          ?.split(" ")
                          .map((n) => n[0])
                          .join("")
                          .toUpperCase() || "U"}
                      </AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56">
                  <div className="flex items-center justify-start gap-2 p-2">
                    <div className="flex flex-col space-y-1 leading-none">
                      <p className="font-medium">{user?.name}</p>
                      <p className="text-sm text-muted-foreground">
                        {user?.email}
                      </p>
                    </div>
                  </div>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem asChild>
                    <Link to="/profile" className="w-full cursor-pointer">
                      <User className="mr-2 h-4 w-4" />
                      <span>Profile</span>
                    </Link>
                  </DropdownMenuItem>
                  {user?.role === "owner" && (
                    <DropdownMenuItem asChild>
                      <Link
                        to="/dashboard"
                        className="w-full cursor-pointer"
                      >
                        <ChefHat className="mr-2 h-4 w-4" />
                        <span>Restaurant Dashboard</span>
                      </Link>
                    </DropdownMenuItem>
                  )}
                  <DropdownMenuItem asChild>
                    <Link to="/orders" className="w-full cursor-pointer">
                      <ShoppingBag className="mr-2 h-4 w-4" />
                      <span>My Orders</span>
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild>
                    <Link to="/settings" className="w-full cursor-pointer">
                      <Settings className="mr-2 h-4 w-4" />
                      <span>Settings</span>
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    className="cursor-pointer"
                    onClick={() => logout()}
                  >
                    <LogOut className="mr-2 h-4 w-4" />
                    <span>Logout</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <div className="flex items-center space-x-2">
                <Button
                  variant="ghost"
                  className="text-gray-700 hover:text-food-600"
                  asChild
                >
                  <Link to="/login">Log in</Link>
                </Button>
                <Button asChild>
                  <Link to="/register">Sign up</Link>
                </Button>
              </div>
            )}
          </div>

          {/* Mobile menu button */}
          <div className="flex md:hidden">
            <button
              type="button"
              className="p-2 rounded-md text-gray-700"
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              aria-label="Toggle menu"
            >
              {isMenuOpen ? (
                <X className="h-6 w-6" aria-hidden="true" />
              ) : (
                <Menu className="h-6 w-6" aria-hidden="true" />
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      <div
        className={`md:hidden transition-all duration-300 ease-in-out ${
          isMenuOpen
            ? "opacity-100 translate-x-0"
            : "opacity-0 -translate-x-full pointer-events-none"
        } fixed inset-0 z-40 bg-white`}
      >
        <div className="pt-20 pb-6 px-4 space-y-4">
          {navLinks.map((link) => (
            <Link
              key={link.path}
              to={link.path}
              className={`block py-2 px-3 text-base font-medium rounded-md ${
                location.pathname === link.path
                  ? "bg-food-50 text-food-600"
                  : "text-gray-700 hover:bg-gray-50 hover:text-food-500"
              }`}
              onClick={closeMenu}
            >
              {link.name}
            </Link>
          ))}

          <div className="pt-4 border-t border-gray-200">
            <Link
              to="/cart"
              className="flex items-center py-2 px-3 text-base font-medium rounded-md text-gray-700 hover:bg-gray-50"
              onClick={closeMenu}
            >
              <ShoppingBag className="h-5 w-5 mr-3 text-gray-500" />
              Cart
              {totalItems > 0 && (
                <Badge variant="outline" className="ml-auto">
                  {totalItems}
                </Badge>
              )}
            </Link>

            {isAuthenticated ? (
              <>
                <Link
                  to="/profile"
                  className="flex items-center py-2 px-3 text-base font-medium rounded-md text-gray-700 hover:bg-gray-50"
                  onClick={closeMenu}
                >
                  <User className="h-5 w-5 mr-3 text-gray-500" />
                  Profile
                </Link>
                {user?.role === "owner" && (
                  <Link
                    to="/dashboard"
                    className="flex items-center py-2 px-3 text-base font-medium rounded-md text-gray-700 hover:bg-gray-50"
                    onClick={closeMenu}
                  >
                    <ChefHat className="h-5 w-5 mr-3 text-gray-500" />
                    Restaurant Dashboard
                  </Link>
                )}
                <button
                  className="flex items-center w-full py-2 px-3 text-base font-medium rounded-md text-gray-700 hover:bg-gray-50"
                  onClick={() => {
                    logout();
                    closeMenu();
                  }}
                >
                  <LogOut className="h-5 w-5 mr-3 text-gray-500" />
                  Logout
                </button>
              </>
            ) : (
              <div className="flex flex-col space-y-2 pt-2">
                <Button
                  variant="ghost"
                  className="justify-start"
                  asChild
                >
                  <Link to="/login" onClick={closeMenu}>
                    Log in
                  </Link>
                </Button>
                <Button
                  className="justify-start"
                  asChild
                >
                  <Link to="/register" onClick={closeMenu}>
                    Sign up
                  </Link>
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
