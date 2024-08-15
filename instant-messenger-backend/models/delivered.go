package models

type Delivered struct {
	ChatID    string `json:"chatId"`
	Delivered bool   `json:"delivered"`
}
