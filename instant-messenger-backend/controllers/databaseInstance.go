package controllers

import (
	"instant-messenger-backend/database"

	"go.mongodb.org/mongo-driver/mongo"
)

var messageCollections *mongo.Collection = database.GetCollection(database.DB, "messages")
var userCollections *mongo.Collection = database.GetCollection(database.DB, "users")
var chatCollections *mongo.Collection = database.GetCollection(database.DB, "chats")
