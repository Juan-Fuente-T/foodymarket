import { Button } from "flowbite-react";
import { auth } from "../../auth";

export default async function Home() {
  const session = await auth();

  return (
    <div className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
      <main className="flex flex-col gap-8 row-start-2 items-center sm:items-start">
        <div className="flex flex-row gap-50">
          <h1 className="text-2x1 font-size: var(--text-2xl);">Foody</h1>
          {session?.access_token ?? "no autenticado"}
          <Button>Click dale</Button>
        </div>
      </main>
      <footer className="row-start-3 flex gap-6 flex-wrap items-center justify-center">footer</footer>
    </div>
  );
}
