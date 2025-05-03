import { useState } from 'react';
import React from 'react';

function App() {
  const [count, setCount] = useState(0);
  const [message, setMessage] = useState('');

  const fetchMessage = () => {
    fetch('http://localhost:8000/api/user')
      .then((response) => response.json())
      .then((data) => setMessage(data.message))
      .catch((error) => console.error('Error fetching message:', error));
  };

  return (
    <>
      <h1>CopyCats</h1>
      <button onClick={fetchMessage}>Fetch New Message</button>
      {message && <p>Message from backend: {message}</p>}
    </>
  );
}

export default App;

