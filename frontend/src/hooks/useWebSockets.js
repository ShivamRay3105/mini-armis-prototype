import { useEffect, useState } from 'react';
// import SockJS from 'sockjs-client'; // <--- Fix: use sockjs-client
import SockJS from 'sockjs-client/dist/sockjs.js';
import { Client } from '@stomp/stompjs';

export const useWebSockets = (topic) => {
  const [messages, setMessages] = useState([]);

  useEffect(() => {
    const socket = new SockJS('http://localhost:8765/ws-alerts'); // Gateway Endpoint
    const stompClient = new Client({
      webSocketFactory: () => socket,
     onConnect: () => {
    console.log("Connected to STOMP Broker!");
    stompClient.subscribe(topic, (message) => {
        console.log("RAW DATA RECEIVED FROM WEBSOCKET:", message.body); // Add this!
        const newAlert = JSON.parse(message.body);
        setMessages((prev) => [newAlert, ...prev].slice(0, 50));
    });
},
    });

    stompClient.activate();
    return () => stompClient.deactivate();
  }, [topic]);

  return messages;
};