package rabbitmq

import amqp "github.com/rabbitmq/amqp091-go"

var rabbitMQ *amqp.Connection
var rabbitMQChannel *amqp.Channel
