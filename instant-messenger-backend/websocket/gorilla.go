package websocket

import (
	"encoding/json"
	"fmt"
	"instant-messenger-backend/models"
	"log"
	"net/http"
	"sync"

	"github.com/gorilla/websocket"
)

type InfoConnection struct {
	ID   string `json:"id"`
	Addr string `json:"address"`
}

type Server struct {
	conns    map[string][]*websocket.Conn // Map untuk menyimpan koneksi berdasarkan ID pengguna
	connsMu  sync.Mutex
	nextID   int
	upgrader websocket.Upgrader
}

func NewServer() *Server {
	return &Server{
		conns:    make(map[string][]*websocket.Conn),
		nextID:   1,
		upgrader: websocket.Upgrader{CheckOrigin: func(r *http.Request) bool { return true }},
	}
}

func (s *Server) handleWebSocket(w http.ResponseWriter, r *http.Request) {
	conn, err := s.upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println(err)
		return
	}
	defer conn.Close()

	// Read initial message to get ChatID
	_, msg, err := conn.ReadMessage()
	if err != nil {
		log.Println("Error reading user ID:", err)
		return
	}

	var ChatID struct {
		ID string `json:"id"`
	}
	if err := json.Unmarshal(msg, &ChatID); err != nil {
		log.Println("Error decoding user ID:", err)
		return
	}

	s.connsMu.Lock()
	connID := fmt.Sprintf("%s-%d", ChatID.ID, s.nextID)
	s.nextID++
	if _, exists := s.conns[ChatID.ID]; !exists {
		s.conns[ChatID.ID] = make([]*websocket.Conn, 0)
	}
	s.conns[ChatID.ID] = append(s.conns[ChatID.ID], conn)
	s.connsMu.Unlock()

	conn.WriteMessage(websocket.TextMessage, []byte(connID))

	for {
		_, msg, err := conn.ReadMessage()
		if err != nil {
			log.Println(err)
			s.connsMu.Lock()
			for i, c := range s.conns[ChatID.ID] {
				if c == conn {
					s.conns[ChatID.ID] = append(s.conns[ChatID.ID][:i], s.conns[ChatID.ID][i+1:]...)
					break
				}
			}
			if len(s.conns[ChatID.ID]) == 0 {
				delete(s.conns, ChatID.ID)
			}
			s.connsMu.Unlock()
			return
		}

		var deliveredReceipt models.Delivered
		if err := json.Unmarshal(msg, &deliveredReceipt); err != nil {
			log.Println("Error decoding delivered receipt:", err)
			continue
		}

		var clientMsg models.Message
		if err := json.Unmarshal(msg, &clientMsg); err != nil {
			log.Println("Error decoding message:", err)
			continue
		}

		var readReceipt models.Readed
		if err := json.Unmarshal(msg, &readReceipt); err != nil {
			log.Println("Error decoding read receipt:", err)
			continue
		}

		if readReceipt.Read {
			log.Printf("Received read receipt from client: %+v", readReceipt)
			// Handle read receipt
			s.handleReadReceipt(readReceipt, conn)
		} else if deliveredReceipt.Delivered {
			log.Printf("Received delivered receipt from client: %+v", deliveredReceipt)
			// Handle delivered receipt and get the ChatID
			chatID := s.handleDeliveredReceipt(deliveredReceipt, conn)
			log.Printf("Delivered receipt processed for ChatID: %s", chatID)
		} else {
			s.handleMessage(clientMsg, conn)
		}
	}
}

func (s *Server) handleMessage(clientMsg models.Message, conn *websocket.Conn) {
	s.connsMu.Lock()
	defer s.connsMu.Unlock()

	found := false
	for _, destConn := range s.conns[clientMsg.ChatID] {
		destConn.WriteMessage(websocket.TextMessage, []byte(clientMsg.Content))
		conn.WriteMessage(websocket.TextMessage, []byte("Pesan berhasil dikirim"))
		found = true
	}

	if !found {
		conn.WriteMessage(websocket.TextMessage, []byte("orangnya tidak ada"))
	}
}

func (s *Server) handleReadReceipt(readReceipt models.Readed, _ *websocket.Conn) {
	s.connsMu.Lock()
	defer s.connsMu.Unlock()

	for _, destConn := range s.conns[readReceipt.ChatID] {
		destConn.WriteMessage(websocket.TextMessage, []byte("Pesan telah dibaca"))
	}
}

func (s *Server) handleDeliveredReceipt(deliveredReceipt models.Delivered, _ *websocket.Conn) string {
	s.connsMu.Lock()
	defer s.connsMu.Unlock()

	for _, destConn := range s.conns[deliveredReceipt.ChatID] {
		message := fmt.Sprintf(deliveredReceipt.ChatID)
		destConn.WriteMessage(websocket.TextMessage, []byte(message))
	}

	return deliveredReceipt.ChatID
}
func (s *Server) getConnectionInfo() []InfoConnection {
	s.connsMu.Lock()
	defer s.connsMu.Unlock()

	var connections []InfoConnection

	for connID, connList := range s.conns {
		for _, conn := range connList {
			info := InfoConnection{
				ID:   connID,
				Addr: conn.RemoteAddr().String(),
			}
			connections = append(connections, info)
		}
	}

	return connections
}

func connectionsHandler(w http.ResponseWriter, _ *http.Request, server *Server) {
	connections := server.getConnectionInfo()

	// Mengembalikan informasi koneksi dalam format JSON
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(connections)
}

func (s *Server) SendChatToClient(message []byte) (bool, error) {
	log.Printf("Received a message: %s", message)

	var clientMsg models.Message
	if err := json.Unmarshal(message, &clientMsg); err != nil {
		log.Println("Error decoding message:", err)

	}

	s.connsMu.Lock()
	for _, destConn := range s.conns[clientMsg.ChatID] {
		destConn.WriteMessage(websocket.TextMessage, message)
	}
	s.connsMu.Unlock()

	return true, nil
}

var ClientServer *Server

func InitGorillaWebsocket() {

	ClientServer = NewServer()

	http.HandleFunc("/ws", ClientServer.handleWebSocket)
	http.HandleFunc("/connections", func(w http.ResponseWriter, r *http.Request) {
		connectionsHandler(w, r, ClientServer)
	})

	fmt.Println("Server is listening on port 8181")
	log.Fatal(http.ListenAndServe(":8181", nil))
}
