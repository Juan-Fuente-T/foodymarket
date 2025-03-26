
import React from "react";
import { useLocation, Link } from "react-router-dom";
import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Home } from "lucide-react";
import { Layout } from "@/components/layout/Layout";

const NotFound = () => {
  const location = useLocation();

  useEffect(() => {
    console.error(
      "404 Error: User attempted to access non-existent route:",
      location.pathname
    );
  }, [location.pathname]);

  return (
    <Layout>
      <div className="flex flex-col items-center justify-center min-h-[70vh] px-4 py-16 text-center">
        <h1 className="text-9xl font-bold text-food-500 mb-4 animate-float">404</h1>
        <p className="text-2xl text-gray-700 mb-8">Oops! We couldn't find that page.</p>
        <p className="text-lg text-gray-500 max-w-lg mb-8">
          The page you were looking for doesn't exist or might have been moved.
        </p>
        <Button asChild size="lg" className="animate-fade-in">
          <Link to="/" className="flex items-center">
            <Home className="mr-2 h-5 w-5" /> Return to Home
          </Link>
        </Button>
      </div>
    </Layout>
  );
};

export default NotFound;
