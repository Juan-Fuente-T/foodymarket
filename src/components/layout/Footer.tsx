
import React from "react";
import { Link } from "react-router-dom";
import { ChefHat, Facebook, Instagram, Twitter } from "lucide-react";

export function Footer() {
  return (
    <footer className="bg-gray-50 border-t border-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="space-y-4">
            <Link to="/" className="flex items-center text-food-600 font-bold text-xl">
              <ChefHat className="h-6 w-6 mr-2" />
              FoodDelivery
            </Link>
            <p className="text-gray-500 max-w-xs">
              Delicious food delivered to your doorstep from your favorite local restaurants.
            </p>
            <div className="flex space-x-4">
              <a
                href="#"
                className="text-gray-400 hover:text-food-500 transition-colors"
                aria-label="Facebook"
              >
                <Facebook className="h-5 w-5" />
              </a>
              <a
                href="#"
                className="text-gray-400 hover:text-food-500 transition-colors"
                aria-label="Twitter"
              >
                <Twitter className="h-5 w-5" />
              </a>
              <a
                href="#"
                className="text-gray-400 hover:text-food-500 transition-colors"
                aria-label="Instagram"
              >
                <Instagram className="h-5 w-5" />
              </a>
            </div>
          </div>

          <div>
            <h3 className="font-semibold text-gray-900 mb-4">Quick Links</h3>
            <ul className="space-y-2">
              <li>
                <Link
                  to="/"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Home
                </Link>
              </li>
              <li>
                <Link
                  to="/restaurants"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Restaurants
                </Link>
              </li>
              <li>
                <Link
                  to="/restaurants"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Browse Menu
                </Link>
              </li>
              <li>
                <Link
                  to="/cart"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Cart
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h3 className="font-semibold text-gray-900 mb-4">Support</h3>
            <ul className="space-y-2">
              <li>
                <Link
                  to="/contact"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Help Center
                </Link>
              </li>
              <li>
                <Link
                  to="/privacy"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Privacy Policy
                </Link>
              </li>
              <li>
                <Link
                  to="/terms"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Terms of Service
                </Link>
              </li>
              <li>
                <Link
                  to="/contact"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Contact Us
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h3 className="font-semibold text-gray-900 mb-4">For Restaurants</h3>
            <ul className="space-y-2">
              <li>
                <Link
                  to="/partner"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Become a Partner
                </Link>
              </li>
              <li>
                <Link
                  to="/dashboard"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Restaurant Dashboard
                </Link>
              </li>
              <li>
                <Link
                  to="/advertising"
                  className="text-gray-500 hover:text-food-500 transition-colors"
                >
                  Advertising
                </Link>
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-200 mt-8 pt-8 flex flex-col md:flex-row justify-between items-center">
          <p className="text-gray-500 text-sm">
            &copy; {new Date().getFullYear()} FoodDelivery. All rights reserved.
          </p>
          <div className="flex space-x-6 mt-4 md:mt-0">
            <Link
              to="/privacy"
              className="text-sm text-gray-500 hover:text-food-500 transition-colors"
            >
              Privacy
            </Link>
            <Link
              to="/terms"
              className="text-sm text-gray-500 hover:text-food-500 transition-colors"
            >
              Terms
            </Link>
            <Link
              to="/contact"
              className="text-sm text-gray-500 hover:text-food-500 transition-colors"
            >
              Contact
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
}
