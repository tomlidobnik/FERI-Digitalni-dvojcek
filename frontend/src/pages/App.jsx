import { useRef, useState, useEffect } from "react";
import { Link, useLocation } from "react-router-dom";
import Cookies from "js-cookie";
import "../index.css";
import { FaHome, FaSignInAlt, FaUserPlus, FaSignOutAlt } from "react-icons/fa";
import { Bar, Doughnut } from "react-chartjs-2";
import { Chart as ChartJS, BarElement, ArcElement, CategoryScale, LinearScale, Tooltip, Legend } from "chart.js";
ChartJS.register(BarElement, ArcElement, CategoryScale, LinearScale, Tooltip, Legend);
import ScrollReveal from '../blocks/TextAnimations/ScrollReveal/ScrollReveal';
import RotatingText from '../blocks/TextAnimations/RotatingText/RotatingText'
import Beams from '../blocks/TextAnimations/Beams/Beams'
import ScrollFloat from '../blocks/TextAnimations/ScrollFloat/ScrollFloat';
import ShinyText from '../blocks/ShinyText';
import { LayoutGroup, motion } from 'framer-motion';
import { gsap } from "gsap";
import Aurora from '../blocks/Aurora';
import AnimatedContent from "../blocks/AnimatedContent";
import TiltedCard from "../blocks/TiltedCard";
import ProfileCard from "../blocks/ProfileCard";
import SpotlightCard from '../blocks/SpotlightCard';
import BlurText from "../blocks/BlurText";
import CountUp from "../blocks/CountUp";
import CircularGallery from "../blocks/CircularGallery";
import { useNavigate } from "react-router-dom";

function App() {
  const [menuOpen, setMenuOpen] = useState(false);
  const token = Cookies.get("token");
  const [isScrolled, setIsScrolled] = useState(false);
  const API_URL = import.meta.env.VITE_API_URL;
  const [response, setResponse] = useState([]);
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    document.body.setAttribute("data-path", location.pathname);
  }, [location.pathname]);

  useEffect(() => {
    if (token) {
      navigate("/home");
    }

    fetch(`https://${API_URL}/api/user/stats`)
      .then(res => res.json())
      .then(data => { setResponse(data); console.log(data); })
      .catch(() => setResponse(null));

    const handleScroll = () => {
      setIsScrolled(window.scrollY > 10);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // Inject scrollbar-hiding styles directly into the app
  useEffect(() => {
    let styleTag = document.getElementById("hide-scrollbar-style");
    if (!styleTag) {
      styleTag = document.createElement("style");
      styleTag.id = "hide-scrollbar-style";
      document.head.appendChild(styleTag);
    }

      styleTag.innerHTML = `
        ::-webkit-scrollbar {
            display: none !important;
            width: 0 !important;
            height: 0 !important;
        }
        ::-webkit-scrollbar-track,
        ::-webkit-scrollbar-thumb,
        ::-webkit-scrollbar-thumb:hover {
            background: transparent !important;
        }

        html, body, * {
            scrollbar-width: none !important; /* Firefox */
            -ms-overflow-style: none !important; /* IE and Edge */
        }
      `;

    // Clean up on unmount
    return () => {
      styleTag.innerHTML = "";
    };
  }, [location.pathname]);

  return (
 <div className="flex flex-col min-h-screen w-full justify-center items-center bg-black overflow-x-hidden">
      <div className="flex flex-1 flex-col min-h-screen w-full overflow-y-auto overflow-x-hidden">
        <div className="flex h-screen w-full flex-row items-center justify-center bg-black relative overflow-hidden">
          {/* Beams background */}
          <div style={{ position: 'absolute', inset: 0, zIndex: 0, width: '100%', height: '100%' }}>
            <Beams
              beamWidth={1.7}
              beamHeight={15}
              beamNumber={50}
              lightColor="#c2dac7"
              speed={2}
              noiseIntensity={0.75}
              scale={0.2}
              rotation={0}
            />
          </div>
          {/* Foreground content */}
          <div className="relative z-10 flex items-center justify-center w-full h-full">
            <LayoutGroup>
              <motion.p className="rotating-text-ptag flex" layout>
                <motion.span
                  className="pt-0.5 sm:pt-1 md:pt-2 text-3xl sm:text-5xl md:text-6xl lg:text-8xl mr-3 font-extrabold text-primary"
                  layout
                  transition={{ type: "spring", damping: 30, stiffness: 400 }}
                >
                  Dogodki so{" "}
                </motion.span>
                <RotatingText
                  texts={['zgodbe.', 'spomini.', 'priložnosti.', 'doživetja.']}
                  mainClassName="px-2 sm:px-2 md:px-3 bg-quaternary text-primary overflow-hidden py-0.5 sm:py-1 md:py-2 justify-left rounded-lg text-3xl sm:text-5xl md:text-6xl lg:text-8xl font-extrabold"
                  staggerFrom={"last"}
                  initial={{ y: "100%" }}
                  animate={{ y: 0 }}
                  exit={{ y: "-120%" }}
                  staggerDuration={0.025}
                  splitLevelClassName="rotating-text-split"
                  transition={{ type: "spring", damping: 30, stiffness: 400 }}
                  rotationInterval={2000}
                />
              </motion.p>
            </LayoutGroup>
          </div>
        </div>
        <div className="flex flex-col items-center justify-center h-fit bg-black/50">
          <div className="flex flex-col w-2/3 2xl:w-1/2 h-fit max-w-[1200px] pt-64">
            <ScrollFloat
              animationDuration={1}
              ease='back.inOut(4)'
              scrollStart='center bottom+=50%'
              scrollEnd='bottom bottom-=50%'
              stagger={0.03}
              containerClassName="text-primary text-4xl xs:text-6xl lg:text-8xl font-extrabold my-[10vh]"
            >
              Kdo smo?
            </ScrollFloat>
            <ScrollFloat
              animationDuration={1}
              ease='back.inOut(2)'
              scrollStart='center bottom+=50%'
              scrollEnd='bottom bottom-=50%'
              stagger={0.03}
              containerClassName="text-primary text-4xl xs:text-6xl lg:text-8xl font-extrabold ml-auto my-[10vh]"
            >
              Copycats.
            </ScrollFloat>
            <BlurText
              text="Smo ekipa treh študentov FERI-ja, ki verjamemo v moč povezovanja ljudi skozi dogodke. Naš cilj je omogočiti preprosto in intuitivno platformo za ustvarjanje in iskanje dogodkov."
              delay={50}
              stepDuration={0.45}
              animateBy="words"
              direction="top"

              className="text-primary text-xl xs:text-2xl lg:text-4xl font-extrabold ml-auto my-[25vh]"
            />
            <AnimatedContent
              distance="100vh"
              direction="horizontal"
              reverse={true}
              duration={1.5}
              ease="back.inOut"
              initialOpacity={0.2}
              animateOpacity
              scale={1.1}
              threshold={0.2}
              delay={0.2}
              containerClassName=""
            >
              <div className="flex flex-col lg:flex-row gap-8 lg:gap-16 justify-center mt-16 lg:mt-32 items-center w-full transition-all duration-300">
                <TiltedCard
                  imageSrc="https://avatars.githubusercontent.com/u/149946426?v=4"
                  altText=""
                  captionText="@anejbr"
                  containerHeight="300px"
                  containerWidth="300px"
                  imageHeight="300px"
                  imageWidth="300px"
                  rotateAmplitude={12}
                  scaleOnHover={1.2}
                  showMobileWarning={false}
                  showTooltip={false}
                  displayOverlayContent={true}
                  overlayContent={
                    <div className="text-2xl text-primary font-bold p-4 m-4 bg-black/50 rounded-lg">
                      Anej Bregant
                    </div>
                  }
                />
                <SpotlightCard className="w-full text-primary font-semibold text-xl items-center m-auto justify-center" spotlightColor="#626f47">
                  <div className="flex flex-col gap-2">
                    <span className="pb-4 block">
                      Sem razvijalec spletnih aplikacij, ki se osredotoča na čelni del in oblikovanje uporabniških vmesnikov. Delam z JavaScriptom, Pythonom, PHP-jem, C++ in drugimi programskimi jeziki. Imam tudi izkušnje z zalednim razvojem od integracije API-jev do upravljanja podatkovnih baz in strežniške logike.
                    </span>
                    <button className="flex">
                      <a className="github-button" href="https://github.com/anejbr" data-color-scheme="no-preference: light; light: light; dark: dark;" data-size="large" aria-label="Follow @anejbr on GitHub">
                        @anejbr
                      </a>
                    </button>
                  </div>
                </SpotlightCard>
              </div>
              <script async defer src="https://buttons.github.io/buttons.js"></script>
            </AnimatedContent>

            <AnimatedContent
              distance="100vh"
              direction="horizontal"
              reverse={true}
              duration={1.5}
              ease="back.inOut"
              initialOpacity={0.2}
              animateOpacity
              scale={1.1}
              threshold={0.2}
              delay={0.2}
            >
              <div className="flex flex-col lg:flex-row gap-8 lg:gap-16 justify-center mt-16 lg:mt-32 items-center w-full transition-all duration-300">
                <SpotlightCard
                  className="w-full text-primary font-semibold text-xl items-center m-auto justify-center hidden lg:flex"
                  spotlightColor="#626f47"
                >
                  <div className="flex flex-col gap-2">
                    <span className="pb-4 block">
                      Sem programer, ki se navdušuje nad zalednim razvojem in gradnjo robustne infrastrukture za aplikacije. Uživam v reševanju kompleksnih logičnih izzivov in optimizaciji sistemov za zmogljivost in stabilnost. Rad delam z bazami podatkov in API-ji. Stremim k pisanju čiste, vzdržljive kode ter sodelovanju v ekipah, kjer je znanje v ospredju.
                    </span>
                    <button className="flex">
                      <a className="github-button" href="https://github.com/tomlidobnik" data-color-scheme="no-preference: light; light: light; dark: dark;" data-size="large" aria-label="Follow @tomlidobnik on GitHub">
                        @tomlidobnik
                      </a>
                    </button>
                  </div>
                </SpotlightCard>
                <TiltedCard
                  imageSrc="https://avatars.githubusercontent.com/u/115782027?v=4"
                  altText=""
                  captionText="@tomlidobnik"
                  containerHeight="300px"
                  containerWidth="300px"
                  imageHeight="300px"
                  imageWidth="300px"
                  rotateAmplitude={12}
                  scaleOnHover={1.2}
                  showMobileWarning={false}
                  showTooltip={false}
                  displayOverlayContent={true}
                  overlayContent={
                    <div className="text-2xl text-primary font-bold p-4 m-4 bg-black/50 rounded-lg">
                      Tom Li Dobnik
                    </div>
                  }
                />
                <SpotlightCard
                  className="w-full text-primary font-semibold text-xl items-center m-auto justify-center block lg:hidden"
                  spotlightColor="#626f47"
                >
                  <div className="flex flex-col gap-2">
                    <span className="pb-4 block">
                      Sem programer, ki se navdušuje nad zalednim razvojem in gradnjo robustne infrastrukture za aplikacije. Uživam v reševanju kompleksnih logičnih izzivov in optimizaciji sistemov za zmogljivost in stabilnost. Rad delam z bazami podatkov in API-ji. Stremim k pisanju čiste, vzdržljive kode ter sodelovanju v ekipah, kjer je znanje v ospredju.
                    </span>
                    <button className="flex">
                      <a
                        className="github-button"
                        href="https://github.com/tomlidobnik"
                        data-color-scheme="no-preference: light; light: light; dark: dark;"
                        data-size="large"
                        aria-label="Follow @tomlidobnik on GitHub"
                      >
                        @tomlidobnik
                      </a>
                    </button>
                  </div>
                </SpotlightCard>
              </div>
            </AnimatedContent>
            <AnimatedContent
              distance="100vh"
              direction="horizontal"
              reverse={true}
              duration={1.5}
              ease="back.inOut"
              initialOpacity={0.2}
              animateOpacity
              scale={1.1}
              threshold={0.2}
              delay={0.2}
            >
            <div className="flex flex-col lg:flex-row gap-8 lg:gap-16 justify-center mt-16 lg:mt-32 items-center w-full transition-all duration-300">
              <TiltedCard
                imageSrc="https://avatars.githubusercontent.com/u/157404999?v=4"
                altText=""
                captionText="@alenk2"
                containerHeight="300px"
                containerWidth="300px"
                imageHeight="300px"
                imageWidth="300px"
                rotateAmplitude={12}
                scaleOnHover={1.2}
                showMobileWarning={false}
                showTooltip={false}
                displayOverlayContent={true}
                overlayContent={
                  <div className="text-2xl text-primary font-bold p-4 m-4 bg-black/50 rounded-lg">
                    Alen Kolmanič
                  </div>
                }
              />
              <SpotlightCard className="w-full text-primary font-semibold text-xl items-center m-auto justify-center" spotlightColor="#626f47">
                <div className="flex flex-col gap-2">
                  <span className="pb-4 block">
                    Delam na razvijanju aplikacije za urejanje podatkovne baze dogodkov. V skupini skrbim za razvoj namizne aplikacije – oblikujem in implementiram uporabniški vmesnik, ki omogoča enostavno in pregledno upravljanje podatkov. Navdušen sem tudi nad čelnim delom aplikacij in spletnih strani. Rad delam s programskimi jeziki kot so Kotlin, C++ in različni JavaScript ogrodji.
                  </span>
                  <button className="flex">
                    <a className="github-button" href="https://github.com/alenk2" data-color-scheme="no-preference: light; light: light; dark: dark;" data-size="large" aria-label="Follow @alenk2 on GitHub">
                      @alenk2
                    </a>
                  </button>
                </div>
              </SpotlightCard>
            </div>
            </AnimatedContent>
            <ScrollFloat
              animationDuration={1}
              ease='back.inOut'
              scrollStart='center bottom+=50%'
              scrollEnd='bottom bottom-=50%'
              stagger={0.03}
              containerClassName="text-primary text-4xl xs:text-6xl lg:text-8xl font-extrabold my-[10vh] mt-[20vh]"
            >
              Kaj počnemo?
            </ScrollFloat>
            <BlurText
              text="Omogočamo ti ustvarjanje dogodkov po meri, odkrivanje prihajajočih dogodkov v tvoji bližini ter enostavno povezovanje s prijatelji."
              delay={50}
              stepDuration={0.45}
              animateBy="words"
              direction="top"
              className="text-primary text-xl xs:text-2xl lg:text-4xl font-extrabold ml-auto"
            />
            {}
            <div className="flex flex-col sm:flex-row gap-6 sm:gap-12 justify-between mt-16 sm:mt-32 w-full h-fit">
              <div className="flex flex-col items-center justify-center w-full">
                <AnimatedContent
                  distance="100vh"
                  direction="horizontal"
                  reverse={true}
                  duration={1.5}
                  ease="back.inOut"
                  initialOpacity={0.2}
                  animateOpacity
                  scale={1.1}
                  threshold={0.2}
                  delay={0.2}
                >
                  <Link
                    className="w-full sm:w-auto px-6 py-2 rounded-lg bg-primary text-black font-extrabold text-xl xs:text-2xl lg:text-3xl shadow-md hover:bg-[#e5dcc5] hover:text-[#626f47] transition-all duration-200 border-2 border-primary text-center"
                    to={token ? ("/home") :("/login")}
                  >
                    {token ? ("Domov") :("Prijava")}
                  </Link>
                </AnimatedContent>
              </div>
              <div className="flex flex-col items-center justify-center w-full h-fit">
                <AnimatedContent
                  distance="100vh"
                  direction="horizontal"
                  reverse={true}
                  duration={1.5}
                  ease="back.inOut"
                  initialOpacity={0.2}
                  animateOpacity
                  scale={1.1}
                  threshold={0.2}
                  delay={0.2}
                >
                <Link
                  className="w-full sm:w-auto px-6 py-2 rounded-lg bg-primary text-black font-extrabold text-xl xs:text-2xl lg:text-3xl shadow-md hover:bg-[#e5dcc5] hover:text-[#626f47] transition-all duration-200 border-2 border-primary text-center transform hover:scale-110"
                  to={token ? ("/logout") :("/register")}
                >
                  {token ? ("Odjava") :("Registracija")}
                </Link>
                </AnimatedContent>
              </div>
            </div>
          </div>
        </div>
        <div className="relative flex flex-col items-center justify-center h-fit min-h-[60vh] bg-black/50 overflow-hidden mt-32">
          {/* Background Maribor map with inverted colors */}
          <img
            src="/assets/maribor_map.svg"
            alt="Maribor Map"
            className="absolute inset-0 w-full h-full object-cover filter invert opacity-40 pointer-events-none select-none"
            style={{ zIndex: 0 }}
          />
          {/* Foreground content goes here */}
          <div className="relative z-10 flex flex-col items-center justify-center w-full h-full">
            {/* ...your content... */}
          </div>
        </div>
        <div className="flex flex-col items-center justify-center h-fit bg-black/50">
          <div className="flex flex-row w-2/3 2xl:w-1/2 h-fit max-w-[1200px] mt-[25vh] items-center justify-center">
            <div className="flex flex-col items-center justify-center w-full h-full">
              <CountUp
                from={0}
                to={response?.total_users || 0}
                separator=","
                direction="up"
                duration={2}
                delay={0.1}
                className="text-primary text-6xl lg:text-8xl font-extrabold"
              />
              <div className="text-primary text-xl md:text-2xl">Uporabnikov</div>
            </div>
            <div className="flex flex-col items-center justify-center w-full h-full">
              <CountUp
                from={0}
                to={response?.total_events || 0}
                separator=","
                direction="up"
                duration={2}
                delay={0.2}
                className="text-primary text-6xl lg:text-8xl font-extrabold"
              />
              <div className="text-primary text-xl md:text-2xl">Dogodkov</div>
            </div>
            
          </div>

        </div>
        <div className="flex flex-col items-center justify-center h-96 mt-[5vh] bg-black rotate-180">
          <Aurora
            colorStops={["#626f47", "#e5dcc5", "#a4b465"]}
            blend={1}
            amplitude={0.4}
            speed={0.5}
          />
        </div>
      </div>
    </div>
  );
}

export default App;
