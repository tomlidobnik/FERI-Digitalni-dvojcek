import {createSlice} from '@reduxjs/toolkit';

const initialState = {
    value: 0
};

const eventFilterSlice = createSlice({
    name: 'eventFilter',
    initialState,
    reducers: {
        setEventFilter(state, action) {
            return {...state, ...action.payload};
        },
    },
});

export const {setEventFilter} = eventFilterSlice.actions;
export default eventFilterSlice.reducer;