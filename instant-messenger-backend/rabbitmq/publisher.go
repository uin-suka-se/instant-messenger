package rabbitmq

import (
	"encoding/json"
	"fmt"
	"log"
	"mime/multipart"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	amqp "github.com/rabbitmq/amqp091-go"
	"go.mongodb.org/mongo-driver/bson/primitive"

	"instant-messenger-backend/models"
	"instant-messenger-backend/utils"
)

// MessageRaw is a struct that represents the raw message data received from the client
// Attachments field is still multipart.FileHeader type
type MessageRaw struct {
	ID          primitive.ObjectID    `form:"_id"`
	ChatID      string                `form:"chatId"`
	SenderID    string                `form:"senderId"`
	Content     string                `form:"content"`
	SentAt      time.Time             `form:"sentAt"`
	Attachments *multipart.FileHeader `form:"attachments"`
}

func PublishToDatabase(c *gin.Context) {
	// Get the request body and convert it to message
	var messageRaw MessageRaw
	if err := c.ShouldBind(&messageRaw); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	log.Printf("Received message: %+v\n", messageRaw)

	var path string
	var err error
	if messageRaw.Attachments != nil {
		path, err = utils.UploadFile(messageRaw.ChatID, messageRaw.Attachments, c)
		if err != nil {
			fmt.Println("Error uploading file:", err)
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Internal server error"})
			return
		}
		fmt.Println("Attachments: ", path)
	}

	// Transform the raw message to a message
	var message models.Message = models.Message{
		ID:          messageRaw.ID,
		ChatID:      messageRaw.ChatID,
		SenderID:    messageRaw.SenderID,
		Content:     messageRaw.Content,
		SentAt:      messageRaw.SentAt,
		Attachments: path,
	}

	// Convert the message to JSON
	messageJSON, err := json.Marshal(message)
	if err != nil {
		fmt.Printf("Error marshalling message to JSON: %v\n", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Internal server error"})
		return
	}

	// Publish message to the exchange
	err = rabbitMQChannel.Publish(
		"DatabaseExchange", // Exchange name
		"database_key",     // Routing key (matches queue binding)
		true,               // Mandatory (don't fail if no queue bound)
		false,              // Immediate (don't wait for ack)
		amqp.Publishing{
			ContentType: "application/json",
			Body:        messageJSON,
		},
	)
	if err != nil {
		fmt.Printf("Error publishing message to RabbitMQ: %v\n", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Internal server error"})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"error":    false,
		"messages": "Message send successfully, saving to database. Sending Message to Client...",
		"status":   "sending",
	})
}

func PublishToClient(messageJSON []byte) {
	err := rabbitMQChannel.Publish(
		"ClientExchange", // Exchange name
		"client_key",     // Routing key (matches queue binding)
		true,             // Mandatory (don't fail if no queue bound)
		false,            // Immediate (don't wait for ack)
		amqp.Publishing{
			ContentType: "application/json",
			Body:        messageJSON,
		},
	)
	if err != nil {
		fmt.Printf("Error publishing message to RabbitMQ: %v\n", err)
		// c.JSON(http.StatusInternalServerError, gin.H{"error": "Internal server error"})
		return
	}
}
