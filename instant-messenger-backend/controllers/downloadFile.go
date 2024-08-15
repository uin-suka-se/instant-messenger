package controllers

import (
  "fmt"
  "io"
  "net/http"
  "os"
  "path/filepath"

  "github.com/gin-gonic/gin"
)

func DownloadFile(c *gin.Context) {
  // Get file location from request parameter (adjust if needed)
  path := c.Query("path")

  // Get base path (consider using a configurable base path)
  basePath, err := os.Getwd()
  if err != nil {
    c.JSON(http.StatusInternalServerError, gin.H{"error": true, "message": "Error getting base path"})
    return
  }

  // Construct full file path
  fileLocation := filepath.Join(basePath, path)

  // Check if file exists
  if _, err := os.Stat(fileLocation); os.IsNotExist(err) {
    c.JSON(http.StatusNotFound, gin.H{"error": true, "message": "File not found"})
    return
  }

  // Open the file for reading
  file, err := os.Open(fileLocation)
  if err != nil {
    c.JSON(http.StatusInternalServerError, gin.H{"error": true, "message": "Error opening file"})
    return
  }
  defer file.Close() // Ensure file is closed even on errors

  // Set content disposition header
  contentDisposition := fmt.Sprintf("attachment; filename=%s", filepath.Base(fileLocation)) // Use base filename
  c.Writer.Header().Set("Content-Disposition", contentDisposition)

  // Stream the file to the client
  _, err = io.Copy(c.Writer, file)
  if err != nil {
    c.JSON(http.StatusInternalServerError, gin.H{"error": true, "message": "Error streaming file"})
    return
  }

  c.JSON(http.StatusOK, gin.H{
    "error":   false,
    "messages": "File downloaded successfully",
  })
}
