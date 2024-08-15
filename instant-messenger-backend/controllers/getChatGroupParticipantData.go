package controllers

import (
	"context"
	"instant-messenger-backend/models"
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

func GetChatGroupParticipantData(c *gin.Context) {
	// Get chatId from URL parameter
	chatId := c.Query("chatId")

	var chat models.Chat
	err := chatCollections.FindOne(context.TODO(), bson.M{"chatId": chatId}).Decode(&chat)
	if err != nil {
		// Handle error based on the error type
		if err == mongo.ErrNoDocuments {
			// Chat not found, return error response
			c.JSON(http.StatusNotFound, gin.H{
				"error":   true,
				"message": "Chat not found with chatId: " + chatId,
			})
			return
		} else {
			// Other error, log and return internal server error
			log.Printf("Error finding chat with chatId %s: %v\n", chatId, err)
			c.JSON(http.StatusInternalServerError, gin.H{
				"error":   true,
				"message": "Internal server error",
			})
			return
		}
	}

	// get participantsId from chat
	participantsId := chat.Participants

	if len(participantsId) == 0 {
		c.JSON(http.StatusBadRequest, gin.H{
			"error":   true,
			"message": "Missing required parameter: participantsId",
		})
		return
	}

	log.Printf("Extracted participantsId: %s\n", participantsId)

	type ParticipantData struct {
		NIM  string `json:"userId"`
		Name string `json:"name"`
	}

	var participantsData []ParticipantData
	// Get Participant Data based on participantsId
	for _, participantId := range participantsId {
		// Find chat information based on participantId
		cursorUser, err := userCollections.Find(context.TODO(), bson.M{"nim": participantId})
		if err != nil {
			log.Printf("Error finding user with nim %s: %v\n", participantId, err)
		}

		defer cursorUser.Close(context.TODO())
		for cursorUser.Next(context.TODO()) {
			var user models.User
			err := cursorUser.Decode(&user)
			log.Printf("%s", user)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{
					"error":   true,
					"message": err.Error(),
				})
				return
			}

			var data = ParticipantData{user.NIM, user.Name}

			participantsData = append(participantsData, data)
		}
	}

	c.JSON(http.StatusOK, gin.H{
		"error":    false,
		"participantData": participantsData,
	})
}
