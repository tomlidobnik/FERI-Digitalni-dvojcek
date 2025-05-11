import React from "react";
import { Link} from "react-router-dom";
import Cookies from 'js-cookie';

function App() {
    const token = Cookies.get("token");

    return (
        <div className="flex flex-col items-center justify-center h-screen bg-gradient-to-br">
            <div className="flex flex-col p-8 bg-primary rounded-3xl shadow-2xl w-full max-w-md text-gray-800">
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
    );
}

export default App;
