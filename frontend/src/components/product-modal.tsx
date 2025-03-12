"use client";

import { Modal } from "flowbite-react";
import { Product } from "../utils/types";
import Image from "next/image";
import React from "react";

// Define las props del modal
type ProductModalProps = {
  product: Product;
  isOpen: boolean;
  onClose: () => void;
  handleAddToCart: (product: Product) => void;
  handleRemoveFromCart: (product: Product) => void;
};

export default function ProductModal({
  product,
  isOpen,
  onClose,
  handleAddToCart,
  handleRemoveFromCart
}: ProductModalProps) {
  return (
    <Modal show={isOpen} onClose={onClose} >
      <Modal.Header>{product.name}</Modal.Header>
      <Modal.Body>
        <div className="space-y-6">
          <Image
            src={product.image}
            alt={product.name}
            width={500}
            height={300}
            className="object-cover w-full"
          />
          <p className="text-base leading-relaxed text-gray-500 dark:text-gray-400">
            {product.description}
          </p>
          <p className="text-base leading-relaxed text-gray-500 dark:text-gray-400">
            Precio: ${product.price}
          </p>
        </div>
      </Modal.Body>
      <Modal.Footer>
        <button
          onClick={onClose}
          className="bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-700 transition"
        >
          Cerrar
        </button>
        <button
          onClick={() => handleAddToCart(product)}
          className="flex items-end bg-green-500 text-white px-3 py-1 rounded hover:bg-green-700 transition"
        >
          Agregar
        </button>
        <button
          onClick={() => handleRemoveFromCart(product)}
          className="flex items-end bg-red-500 text-white px-3 py-1 rounded hover:bg-red-700 transition"
        >
          Eliminar
        </button>
      </Modal.Footer>
    </Modal>
  );
}