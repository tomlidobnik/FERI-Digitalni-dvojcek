export function setItem(key, value) {
    try{
        window.localStorage.setItem(key, JSON.stringify(value));
    }catch (error) {
        console.error("Error setting item in localStorage:", error);
    }
}

export function getItem(key) {
    try{
        const value = window.localStorage.getItem(key);
        return value ? JSON.parse(value) : undefined;
    }catch (error) {
        console.error("Error getting item from localStorage:", error);
        return undefined;
    }
}