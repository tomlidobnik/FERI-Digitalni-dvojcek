import { configureStore } from '@reduxjs/toolkit';
import eventReducer from './event/eventSlice';
import eventFilterReducer from './window/eventFilterSlice';

export const store = configureStore({
  reducer: { 
    event: eventReducer,
    eventFilter: eventFilterReducer,
  },
});

export default store;