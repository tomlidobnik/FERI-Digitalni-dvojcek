import { Link } from "react-router-dom";
import FuzzyText from '../blocks/TextAnimations/FuzzyText/FuzzyText';


function NotFoundPage() {
    return(
        <div className="flex flex-col items-center justify-center h-screen select-none ">
            <div className="flex items-center flex-col justify-center mb-4 bg-primary w-fit h-96 p-16 rounded-4xl shadow-2xl">
                <FuzzyText 
                baseIntensity={0.2} 
                color={"#A55"}
                >
                404
                </FuzzyText>
                <div className="pt-2">
                    <FuzzyText 
                    baseIntensity={0.1}
                    fontSize={"2rem"}
                    color={"#333"}
                    >
                    Stran ni bila najdena
                    </FuzzyText>
                </div>
                    <Link
                        to="/"
                        className="mt-6 inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-black/20 text-text font-semibold text-xl sm:text-2xl shadow hover:bg-quaternary/80 transition group"
                    >
                    <img
                        src="/icons/angle-left.svg"
                        className="w-6 h-6 xl:w-8 xl:h-8 transition-transform duration-300 group-hover:-translate-x-2"
                        alt="home"
                    />
                        Pojdi na domačo stran
                    </Link>
            </div>
        </div>
    )
}

export default NotFoundPage;