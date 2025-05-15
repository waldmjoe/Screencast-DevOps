import { useState } from 'react';
import './App.css'; // We'll create this file next for styling

function App() {
  const [prompt, setPrompt] = useState('');
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!prompt.trim()) return;

    const userMessage = { sender: 'user', text: prompt };
    // Use functional update to ensure we have the latest messages state
    setMessages(prevMessages => [...prevMessages, userMessage]);
    setIsLoading(true);
    const currentPrompt = prompt; // Store prompt as it will be cleared
    setPrompt(''); // Clear input after sending

    try {
      // Backend is running on port 8080
      // Note: Later, when using Docker and Nginx, this URL will change to '/api/chat'
      const response = await fetch('http://localhost:8080/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ prompt: currentPrompt }), // Use stored prompt
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(`Network response was not ok: ${response.status} - ${errorData}`);
      }

      const data = await response.json();
      const aiMessage = { sender: 'ai', text: data.response || "Sorry, I couldn't get a response." };
      setMessages(prevMessages => [...prevMessages, aiMessage]);

    } catch (error) {
      console.error('Failed to send message:', error);
      const errorMessage = { sender: 'ai', text: `Error: ${error.message}` };
      setMessages(prevMessages => [...prevMessages, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="chat-container">
      <h1>DevOps Chat with Ollama</h1>
      <div className="message-area">
        {messages.map((msg, index) => (
          <div key={index} className={`message ${msg.sender}`}>
            <strong>{msg.sender === 'user' ? 'You' : 'AI'}: </strong>{msg.text}
          </div>
        ))}
        {isLoading && <div className="message ai"><em>AI is thinking...</em></div>}
      </div>
      <form onSubmit={handleSubmit} className="input-area">
        <input
          type="text"
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="Ask something..."
          disabled={isLoading}
        />
        <button type="submit" disabled={isLoading}>
          {isLoading ? 'Sending...' : 'Send'}
        </button>
      </form>
    </div>
  );
}

export default App;