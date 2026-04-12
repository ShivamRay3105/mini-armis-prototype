import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client/dist/sockjs.js';
import { Client } from '@stomp/stompjs';

export const useWebSockets = (topic) => {
  const [messages, setMessages] = useState([]);

  useEffect(() => {
    const socket = new SockJS('http://localhost:8765/ws-alerts');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        console.log("Connected to STOMP Broker!");
        stompClient.subscribe(topic, (message) => {
          console.log("RAW DATA RECEIVED FROM WEBSOCKET:");
          const incomingAlert = JSON.parse(message.body);

          setMessages((prev) => {
            // 1. Remove any old version of this alert to prevent duplicate keys
            const filtered = prev.filter(a => a.alertId !== incomingAlert.alertId);

            // 2. If it's resolved, don't add it back to the state (it will disappear)
            // Note: Check both 'resolved' and 'isResolved' based on your JSON log
            if (incomingAlert.resolved || incomingAlert.isResolved) {
              return filtered;
            }

            // 3. If it's a new/active alert, add it to the top
            return [incomingAlert, ...filtered].slice(0, 50);
          });
        });
      },
    });

    stompClient.activate();
    return () => stompClient.deactivate();
  }, [topic]);

  return messages;
};