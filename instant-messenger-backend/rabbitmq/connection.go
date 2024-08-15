package rabbitmq

import (
	"context"
	"fmt"
	"instant-messenger-backend/configs"

	amqp "github.com/rabbitmq/amqp091-go"
)

func ConnectToRabbitMQ() error {
	fmt.Println("Try to connect to RabbitMQ!")
	conn, err := amqp.Dial(configs.EnvRabbitMQURI())
	if err != nil {
		fmt.Println(err)
		return err
	}
	rabbitMQ = conn
	CreateRabbitMQChannel()

	fmt.Println("Successfully connected to RabbitMQ!")

	ctx := context.Background() // Create a context for the connection

	// Database
	// Declare the exchange and queue after successful connection
	err = DeclareExchange("DatabaseExchange", "direct") // Replace with desired exchange name and type
	if err != nil {
		return err
	}
	err = DeclareQueue("DatabaseQueue") // Replace with desired queue name
	if err != nil {
		return err
	}
	// Bind the queue to the exchange
	BindQueueToExchange("DatabaseQueue", "DatabaseExchange", "database_key") // Replace with desired queue name, exchange name, and routing key

	// Start consuming messages after connection and setup
	go ConsumeToDatabase(ctx) // Start consumer in a separate goroutine

	// Client
	// Declare the exchange and queue after successful connection
	err = DeclareExchange("ClientExchange", "direct") // Replace with desired exchange name and type
	if err != nil {
		return err
	}
	err = DeclareQueue("ClientQueue") // Replace with desired queue name
	if err != nil {
		return err
	}
	// Bind the queue to the exchange
	BindQueueToExchange("ClientQueue", "ClientExchange", "client_key") // Replace with desired queue name, exchange name, and routing key

	// Start consuming messages after connection and setup
	go ConsumeToClient(ctx) // Start consumer in a separate goroutine

	return nil
}

func CreateRabbitMQChannel() error {
	ch, err := rabbitMQ.Channel()
	if err != nil {
		fmt.Println(err)
		return err
	}
	rabbitMQChannel = ch
	fmt.Println("Successfully create RabbitMQ Channel!")
	return nil
}
