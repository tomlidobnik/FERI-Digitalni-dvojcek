import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Cookies from "js-cookie";

function App() {
  const token = Cookies.get("token");
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 10);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);


  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-100 via-white to-gray-200">
      {/* Navbar */}
      <nav className="fixed top-6 left-10 right-10 bg-white/60 backdrop-blur-md rounded-2xl shadow-lg flex items-center justify-between px-8 py-3 z-50">
        <div className="text-xl font-bold text-black">
          Digitalni Dvojček
        </div>
        <div className="flex space-x-4">
          <Link to="/home" className="text-gray-900 hover:text-quaternary font-medium transition">Domov</Link>
          {token ? (
            <Link to="/logout" className="text-gray-900 hover:text-quaternary font-medium transition">Odjava</Link>
          ) : (
            <>
              <Link to="/login" className="text-gray-900 hover:text-quaternary font-medium transition">Prijava</Link>
              <Link to="/register" className="text-gray-900 hover:text-quaternary font-medium transition">Registracija</Link>
            </>
          )}
        </div>
      </nav>

      {/* Hero Section */}
      <header
        className={`transition-all duration-700 ease-in-out ${
          isScrolled
            ? "mx-6 mt-6 rounded-2xl overflow-hidden"
            : "mx-0 mt-0 rounded-none"
        } bg-cover bg-center h-screen flex items-center justify-center relative`}
      >
        <div className="absolute inset-0 bg-tertiary bg-opacity-40" />
        <div className="relative z-10 text-white text-center px-4">
          <h1 className="text-4xl md:text-6xl font-bold drop-shadow-lg">Organiziraj. Ustvari. Doživi.</h1>
          <p className="mt-4 text-lg md:text-xl drop-shadow">Dogodki, kjerkoli, kadarkoli.</p>
          <Link
            to="/TODO"
            className="inline-block mt-6 px-6 py-3 bg-white text-black font-semibold rounded-full shadow hover:bg-quaternary hover:text-white transition"
          >
            Razišči Dogodke
          </Link>
        </div>
      </header>

        {/* Main Content */}
        <main className="mt-20 mx-4 md:mx-16 lg:mx-7 space-y-16">
        {/* Who */}
        <section className="flex flex-col md:flex-row items-center gap-10 bg-white/70 backdrop-blur-md p-8 rounded-2xl shadow-xl border">
            <img src="landing/team.svg" alt="team" className="rounded-xl w-full md:w-1/2 object-cover" />
            <div>
            <h2 className="text-3xl font-bold mb-3 text-black">Kdo smo</h2>
            <p className="text-gray-700">
                Smo ekipa treh študentov FERI-ja, ki verjamemo v moč povezovanja ljudi skozi dogodke. Naš cilj je omogočiti preprosto in intuitivno platformo za ustvarjanje in iskanje dogodkov.
            </p>
            </div>
        </section>

        {/* What */}
        <section className="flex flex-col-reverse md:flex-row items-center gap-10 bg-white/70 backdrop-blur-md p-8 rounded-2xl shadow-xl border">
            <div>
            <h2 className="text-3xl font-bold mb-3 text-black">Kaj počnemo</h2>
            <p className="text-gray-700">
                Omogočamo ti ustvarjanje dogodkov po meri, brskanje po prihajajočih dogodkih v tvoji bližini ter enostavno brskanje po aplikaciji.
            </p>
            </div>
            <img src="landing/plan.svg" alt="event" className="rounded-xl w-full md:w-1/2 object-cover" />
        </section>

        {/* How to */}
        <section className="flex flex-col md:flex-row items-center gap-10 bg-white/70 backdrop-blur-md p-8 rounded-2xl shadow-xl border">
            <div className="w-full md:w-1/2">
            <img
            src="landing/user.svg"
            alt="app"
            className="rounded-xl w-full h-auto object-cover max-w-150"
            />
            </div>

            <div className="w-full md:w-1/2">
                <h2 className="text-3xl font-bold mb-3 text-black">Kako uporabljati aplikacijo</h2>
                <ol className="list-decimal list-inside text-gray-700 space-y-2">
                    <li>Registriraj se ali se prijavi.</li>
                    <li>Ustvari ali poišči dogodek, ki te zanima.</li>
                    <li>Udeleži se dogodka in deli izkušnjo z drugimi.</li>
                </ol>
            </div>
        </section>
        </main>

        {/* Footer */}
        <footer className="mt-20 bg-tertiary backdrop-blur-md p-6 text-center">
        <p className="text-sm">&copy; {new Date().getFullYear()} Digitalni Dvojček</p>
        </footer>

    </div>
  );
}

export default App;
