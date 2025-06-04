const ShowUserInfo = ({ user }) => {
    return (
        <div className="flex flex-col items-center gap-6 align-center p-6">
            <div className="w-24 h-24 rounded-full bg-quaternary flex items-center justify-center text-white text-4xl font-bold shadow-lg">
                {user.username ? user.username.charAt(0).toUpperCase() : "U"}
            </div>
            <div className="w-full max-w-md bg-white/40 rounded-xl shadow p-6 flex flex-col gap-3">
                            <div className="flex">
                                <div className="flex-fit">
                                    <span className="block text-gray-500 text-xs">Uporabniško ime</span>
                                    <span className="text-base text-black">{user.username}</span>
                                </div>
                                <div className="relative flex-shrink-0 ml-auto mb-auto">
                                    <button
                                        onClick={() => setEventToEdit()}
                                        className="flex-shrink-0 text-right bg-black/10 rounded-2xl p-2 flex items-center justify-center hover:bg-quaternary/70 hover:text-white transition group"
                                    >
                                        <img
                                            src="icons/pencil.svg"
                                            className="w-4 h-4 transition-transform duration-300 group-hover:scale-125"
                                            alt="edit"
                                        />
                                    </button>
                                </div>
                            </div>
                            <div className="flex gap-4">
                                <div className="flex-1">
                                    <span className="block text-gray-500 text-xs">Ime</span>
                                    <span className="text-base text-black">{user.first_name}</span>
                                </div>
                                <div className="flex-1">
                                    <span className="block text-gray-500 text-xs">Priimek</span>
                                    <span className="text-base text-black">{user.last_name}</span>
                                </div>
                            </div>
                            <div>
                                <span className="block text-gray-500 text-xs">Email</span>
                                <span className="text-base text-black">{user.email}</span>
                            </div>
            </div>
        </div>
    );
}
export default ShowUserInfo;