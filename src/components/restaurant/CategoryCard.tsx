
import React from "react";
import { Link } from "react-router-dom";
import { Category } from "@/types/models";

interface CategoryCardProps {
  category: Category;
}

export function CategoryCard({ category }: CategoryCardProps) {
  return (
    <Link 
      to={`/categories/${category.id}`}
      className="group relative overflow-hidden rounded-xl bg-white shadow-sm transition-all duration-300 hover:shadow-lg"
    >
      <div className="aspect-square overflow-hidden">
        <img 
          src={category.image || "https://via.placeholder.com/300?text=Category"}
          alt={category.name}
          className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/30 to-transparent"></div>
      </div>
      <div className="absolute bottom-0 left-0 right-0 p-4">
        <h3 className="text-xl font-bold text-white drop-shadow-md">
          {category.name}
        </h3>
        {category.description && (
          <p className="mt-1 text-sm text-white/90 line-clamp-2">
            {category.description}
          </p>
        )}
      </div>
    </Link>
  );
}
