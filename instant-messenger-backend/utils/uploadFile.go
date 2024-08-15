package utils

import (
	"log"
	"mime/multipart"
	"os"

	"github.com/gin-gonic/gin"
)

func UploadFile(chatId string, file *multipart.FileHeader, c *gin.Context) (string, error) {
	// Upload file to server
	
    // Create the folder if it doesn't exist
    folder := "assets/" + chatId
    err := os.MkdirAll(folder, 0755)
    if err != nil {
        // Handle error gracefully (e.g., return appropriate HTTP status code)
        log.Println("Error creating directory:", err)
        return "", err
    }

    // Generate file path
    path := folder + "/" + file.Filename

    // Save the uploaded file
    err = c.SaveUploadedFile(file, path)
    if err != nil {
        // Handle error gracefully (e.g., return appropriate HTTP status code)
        log.Println("Error saving file:", err)
        return "", err
    }

    // File uploaded successfully
    log.Println("File uploaded:", file.Filename)

	return path, nil
}
