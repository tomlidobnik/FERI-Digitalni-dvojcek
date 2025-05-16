import React from "react";
import { Link} from "react-router-dom";
import Cookies from 'js-cookie';

function App() {
    const token = Cookies.get("token");

     return (
    <div className="min-h-screen bg-gradient-to-br">
        {/* Navbar */}
        <nav className="fixed top-6 left-5 right-5 bg-white/60 backdrop-blur-md rounded-2xl shadow-lg flex items-center justify-between px-8 py-3">
            <div className="text-xl font-bold">
                Digitalni Dvojček
            </div>
            <div className="flex space-x-4">
                <Link to="/" className="text-gray-900 hover:text-quaternary font-medium transition">Domov</Link>
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
        {/* Main content */}
        <div className="flex flex-col items-center justify-center h-[100vh]">
            <div className="flex flex-col p-8 bg-primary rounded-3xl shadow-2xl w-full max-w-md text-gray-800 mt-32">
                <h1 className="text-4xl font-bold text-center mb-8 ">Digitalni Dvojček</h1>
                {token ? 
                    <Link to="/logout" className="flex items-center justify-center w-full py-3 text-lg font-semibold rounded-lg bg-quaternary text-white">
                        Odjava
                    </Link>
                :
                    <>
                        <Link to="/login" className="flex items-center justify-center w-full py-3 mb-4 text-lg font-semibold rounded-lg bg-quaternary text-white">
                            Prijava
                        </Link>
                        <Link to="/register" className="flex items-center justify-center w-full py-3 mb-4 text-lg font-semibold rounded-lg bg-quaternary text-white">
                            Registracija
                        </Link>
                    </>
                }
            </div>
        </div>
    </div>
);
}

export default App;
