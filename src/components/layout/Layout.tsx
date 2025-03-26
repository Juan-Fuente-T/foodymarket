
import React, { ReactNode } from "react";
import { Navbar } from "./Navbar";
import { Footer } from "./Footer";

interface LayoutProps {
  children: ReactNode;
  hideFooter?: boolean;
}

export function Layout({ children, hideFooter = false }: LayoutProps) {
  return (
    <div className="flex flex-col min-h-screen">
      <Navbar />
      <main className="flex-grow pt-16">{children}</main>
      {!hideFooter && <Footer />}
    </div>
  );
}
