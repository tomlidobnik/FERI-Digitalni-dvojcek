import {createSlice} from '@reduxjs/toolkit';

const initialState = {
    id: 0,
    user_fk: 0,
    title: '',
    description: '',
    start_date: '2023-01-01T00:00:00',
    end_date: '2023-01-01T00:00:00',
    public: true,
    location_fk: null,
};

const eventSlice = createSlice({
    name: 'event',
    initialState,
    reducers: {
        setEvent(state, action) {
            return {...state, ...action.payload};
        },
    },
});

export const {setEvent} = eventSlice.actions;
export default eventSlice.reducer;