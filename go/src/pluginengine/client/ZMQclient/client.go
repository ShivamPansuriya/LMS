package ZMQclient

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"github.com/pebbe/zmq4"
	"log"
	"motadata-lite/src/pluginengine/constants"
	"motadata-lite/src/pluginengine/utils"
	"strings"
)

type Connections struct {
	context zmq4.Context

	sender zmq4.Socket

	receiver zmq4.Socket

	channel chan []map[string]interface{}
}

var logger = utils.NewLogger("goEngine/ZMQ client", "ZMQ Client")

func (connection *Connections) Init(topic string) (err error) {

	zmq, _ := zmq4.NewContext()

	connection.context = *zmq

	socket, _ := zmq.NewSocket(zmq4.PUSH)

	if err = socket.Connect("tcp://localhost:9000"); err != nil {

		logger.Fatal("error in connecting socket")

		return err
	}

	connection.sender = *socket

	subscriber, err := zmq.NewSocket(zmq4.SUB)

	if err != nil {
		log.Fatalf("Failed to create ZMQ PULL socket: %v\n", err)

		return err
	}

	address := "tcp://localhost:" + "9001"

	if err = subscriber.Connect(address); err != nil {

		logger.Fatal(fmt.Sprintf("Failed to connect to address %s : %v\n", address, err))

		return err
	}

	if err = subscriber.SetSubscribe(topic); err != nil {

		logger.Fatal(fmt.Sprintf("Failed to set subscribe to topic %s: %v", topic, err))

		return err
	}

	connection.receiver = *subscriber

	connection.channel = make(chan []map[string]interface{}, 10)

	return nil
}

func (connection *Connections) StartReceiver() {

	go func(channel chan []map[string]interface{}) {
		for {
			message, err := connection.receiver.Recv(0)

			if err != nil {
				logger.Error(fmt.Sprintf("Failed to receive message: %v\n", err))
			} else {

				var requestContext []map[string]interface{}

				token := strings.Split(message, " ")

				decodedBytes, err := base64.StdEncoding.DecodeString(token[1])

				if err != nil {

					logger.Fatal(fmt.Sprintf("base64 decoding error: %v", err))

					context := map[string]interface{}{

						constants.Error: map[string]interface{}{

							constants.Error: err.Error(),

							constants.ErrorMessage: constants.EncodeErrorMessage,

							constants.ErrorCode: constants.EncodeError,
						},
					}

					context[constants.Status] = constants.StatusFail

					requestContext = append(requestContext, context)

					err := connection.Send(requestContext)

					if err != nil {
						logger.Fatal(fmt.Sprintf("unable to send message:%v", requestContext))
					}

					continue
				}

				if err = json.Unmarshal(decodedBytes, &requestContext); err != nil {

					logger.Fatal(fmt.Sprintf("unable to convert string to json map: %v", err))

					logger.Info("plugin engine stopped")

					context := map[string]interface{}{

						constants.Error: map[string]interface{}{

							constants.Error: err.Error(),

							constants.ErrorMessage: constants.UnmarshalMessage,

							constants.ErrorCode: constants.UnmarshalError,
						},
					}

					context[constants.Status] = constants.StatusFail

					requestContext = append(requestContext, context)

					err := connection.Send(requestContext)

					if err != nil {
						logger.Fatal(fmt.Sprintf("unable to send message:%v", requestContext))
					}

					continue

				}
				channel <- requestContext
			}
		}
		return
	}(connection.channel)
}

func (connection *Connections) GetChannel() chan []map[string]interface{} {

	return connection.channel
}

func (connection *Connections) Send(result []map[string]interface{}) error {
	responseContext, err := json.Marshal(result)

	if err != nil {

		return err
	}

	encodedString := base64.StdEncoding.EncodeToString(responseContext)

	_, err = connection.sender.Send(encodedString, 0)

	if err != nil {

		return err
	}

	return nil
}

func (connection *Connections) Close() {
	if err := connection.sender.Close(); err != nil {
		logger.Error(fmt.Sprintf("Failed to close ZMQ sender socket: %v", err))
	}

	if err := connection.receiver.Close(); err != nil {
		logger.Error(fmt.Sprintf("Failed to close ZMQ receiver socket: %v", err))
	}

	connection.channel = nil

	if err := connection.context.Term(); err != nil {
		logger.Error(fmt.Sprintf("Failed to close ZMQ context: %v", err))
	}
}
