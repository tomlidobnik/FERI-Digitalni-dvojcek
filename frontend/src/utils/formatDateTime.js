export const formatDateTime = (dateString) => {
    const currentDate = new Date();
    const date = new Date(dateString);
    if (currentDate.getDay() === date.getDay() &&
        currentDate.getMonth() === date.getMonth() &&
        currentDate.getFullYear() === date.getFullYear()) {
        return `Danes ob ${date.getHours().toString().padStart(2, "0")}:${date.getMinutes().toString().padStart(2, "0")}`;
    };
    return `${date.getDate()}. ${date.getMonth() + 1}. ${date.getFullYear()} ob ${date.getHours().toString().padStart(2, "0")}:${date.getMinutes().toString().padStart(2, "0")}`;
};