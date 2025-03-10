import Menu from "@/components/menu";
import { api } from "@/server/service";
import Link from "next/link";
import { HiArrowLeft } from "react-icons/hi";
import Image from "next/image";

type Props = {
  params: Promise<{
    id: string;
  }>
}

export default async function Page({ params }: Props) {

  const { id } = await params;

  // Hago la llamada en paralelo para que sea más rápido
  const [restaurant, products] = await Promise.all([
    api.restaurant.get(Number(id)),
    api.product.getAllByRestaurant(Number(id)),
  ]);

  return (
    <main className="min-h-screen w-full md:w-[760px] text-black p-2 md:p-6 mx-auto flex flex-col align-center shadow-lg relative">
      <div>
        <Link
          className="flex gap-1 w-fit items-center justify-center transition-colors hover:bg-gray-200/60 hover:border-black/80 px-2 py-1 text-black decoration-none rounded-full border border-black"
          href="/"
        >
          <HiArrowLeft className="size-3" />
          Volver
        </Link>
      </div>

      {restaurant && (
        <>
          <div className="w-full flex mb-2 justify-center items-center">
            <Image
              className="object-cover w-[50%] md:w-[30%]"
              src={restaurant.logo}
              alt={`Logo de ${restaurant.name}`}
              width={300}
              height={300}
            />
          </div>
          <h1 className="leading-2 text-center text-2xl md:text-5xl font-bold">{restaurant.name}</h1>
          <p className="text-center md:text-2xl text-black/70">{restaurant.description}</p>
        </>
      )}

      {/* Menú (componente Client) */}
      <section className="mt-6">
        <Menu menu={products} />
      </section>
    </main>
  );
}
