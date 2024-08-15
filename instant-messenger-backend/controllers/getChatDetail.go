package controllers

import (
	"context"
	"instant-messenger-backend/models"
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/bson"
)

func GetChatDetail(c *gin.Context) {
	// Get chatId from URL parameter
	chatId := c.Query("chatId")
	userId := c.Query("userId")

	if chatId == "" || userId == "" {
		c.JSON(http.StatusBadRequest, gin.H{
			"error":   true,
			"message": "Missing required parameter",
		})
		return
	}

	log.Printf("Extracted chatId: %s\n", chatId)
	log.Printf("Extracted userId: %s\n", userId)

	// Get chat information
	var chat models.Chat
	err := chatCollections.FindOne(context.TODO(), bson.M{"chatId": chatId}).Decode(&chat)

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error":   true,
			"message": err.Error(),
		})
		return
	}

	// Get All message with chatId matches And Sorting Asc Based SentAt field
	var messageList []models.Message
	curMessage, err := messageCollections.Find(context.TODO(), bson.M{"chatId": chatId})

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error":   true,
			"message": err.Error(),
		})
		return
	}

	defer curMessage.Close(context.TODO())

	// Check for empty results
	if !curMessage.Next(context.TODO()) {
		c.JSON(http.StatusNotFound, gin.H{
			"error":   true,
			"message": "No chat found with chatId: " + chatId,
		})
		return
	}

	// Use curMessage.All to decode all documents at once
	if err := curMessage.All(context.TODO(), &messageList); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error":   true,
			"message": err.Error(),
		})
		return
	}

	// Get chat Name if Private Chat
	// Check if chat has a name (because if private chat, chat name is empty)
	if chat.Name == "" {
		// Get Other Chat Participant userId
		var otherParticipantID string
		if len(chat.Participants) == 2 { // Assuming private chat has exactly 2 participants
			// Extract the other participant's userId based on whether it matches the current user's nim
			if chat.Participants[0] == userId {
				otherParticipantID = chat.Participants[1]
			} else {
				otherParticipantID = chat.Participants[0]
			}
		}

		// Fetch user details of the other participant based on their userId/nim
		var otherParticipantUser models.User
		err := userCollections.FindOne(context.TODO(), bson.M{"nim": otherParticipantID}).Decode(&otherParticipantUser)
		if err != nil {
			// Handle error fetching participant details
			log.Printf("Error finding participant for chat %s: %v\n", chat.ChatID, err)
		}

		// Set chat name to other participant's name
		chat.Name = otherParticipantUser.Name
	}

	c.JSON(http.StatusOK, gin.H{
		"error":       false,
		"chatId":      chatId,
		"chatName":    chat.Name,
		"chatType":    chat.ChatType,
		"totalMember": len(chat.Participants),
		"messages":    messageList,
	})
}
