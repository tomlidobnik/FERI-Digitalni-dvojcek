const ConfirmPopup = ({ open, message, onConfirm, onCancel }) => {
    if (!open) return null;
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="bg-black/10 p-6 rounded-xl shadow-xl flex flex-col items-center gap-4 max-w-xs w-full">
                <div className="text-lg font-semibold text-center text-black">{message}</div>
                <div className="flex gap-4 mt-2">
                    <button
                        className="px-4 py-2 btn-nav-active"
                        onClick={onConfirm}
                    >
                        Izbriši
                    </button>
                    <button
                        className="px-4 py-2 btn-nav"
                        onClick={onCancel}
                    >
                        Prekliči
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmPopup;