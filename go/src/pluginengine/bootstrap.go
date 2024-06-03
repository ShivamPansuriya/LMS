package main

import (
	"fmt"
	"motadata-lite/src/pluginengine/client/ZMQclient"
	"motadata-lite/src/pluginengine/plugins/availability"
	"motadata-lite/src/pluginengine/plugins/linux"
	"motadata-lite/src/pluginengine/utils"
	"motadata-lite/src/pluginengine/utils/constants"
	"os"
	"sync"
)

//
//func main() {
//	logger := utils.NewLogger("goEngine/main", "Boostrap")
//
//	logger.Info("plugin engine started")
//
//	var requestContext []map[string]interface{}
//
//	zmq, _ := zmq4.NewContext()
//
//	defer zmq.Term()
//
//	socket, _ := zmq.NewSocket(zmq4.PUSH)
//
//	defer socket.Close()
//
//	var topic string
//
//	if len(os.Args) != 2 {
//		logger.Fatal("invalid boostrap start arguments")
//
//		return
//	}
//
//	if os.Args[1] == constants.Agent {
//		logger.Info("agent started")
//
//		boostraps.StartAgent(socket)
//
//		return
//
//	} else {
//		topic = os.Args[1]
//	}
//
//	err := socket.Connect("tcp://localhost:9000")
//
//	if err != nil {
//		logger.Fatal("error in connecting socket")
//
//		return
//	}
//
//	subSocket, err := zmq.NewSocket(zmq4.SUB)
//
//	if err != nil {
//		log.Fatalf("Failed to create ZMQ PULL socket: %v\n", err)
//
//		return
//	}
//	defer subSocket.Close()
//
//	address := "tcp://localhost:" + "9001"
//
//	err = subSocket.Connect(address)
//
//	if err != nil {
//		logger.Fatal(fmt.Sprintf("Failed to connect to address %s : %v\n", address, err))
//
//		return
//	}
//
//	err = subSocket.SetSubscribe(topic)
//
//	if err != nil {
//		logger.Fatal(fmt.Sprintf("Failed to set subscribe to topic %s: %v", topic, err))
//
//		return
//	}
//
//	var decodedBytes []byte
//
//	subChannel := make(chan string, 10)
//
//	go func(subChannel chan string) {
//
//		for {
//			message, err := subSocket.Recv(0)
//
//			if err != nil {
//				log.Printf("Failed to receive message: %v\n", err)
//			} else {
//				subChannel <- message
//			}
//		}
//	}(subChannel)
//
//	for {
//		message := <-subChannel
//
//		token := strings.Split(message, " ")
//
//		decodedBytes, err = base64.StdEncoding.DecodeString(token[1])
//
//		if err != nil {
//
//			logger.Fatal(fmt.Sprintf("base64 decoding error: %v", err))
//
//			message := map[string]interface{}{
//
//				constants.Error: map[string]interface{}{
//
//					constants.Error: err.Error(),
//
//					constants.ErrorMessage: constants.EncodeErrorMessage,
//
//					constants.ErrorCode: constants.EncodeError,
//				},
//			}
//
//			message[constants.Status] = constants.StatusFail
//
//			requestContext = append(requestContext, message)
//
//			err := send(requestContext, socket)
//
//			if err != nil {
//				logger.Fatal(fmt.Sprintf("unable to send message:%v", requestContext))
//			}
//
//			continue
//		}
//
//		err = json.Unmarshal(decodedBytes, &requestContext)
//
//		if err != nil {
//
//			logger.Fatal(fmt.Sprintf("unable to convert string to json map: %v", err))
//
//			logger.Info("plugin engine stopped")
//
//			message := map[string]interface{}{
//
//				constants.Error: map[string]interface{}{
//
//					constants.Error: err.Error(),
//
//					constants.ErrorMessage: constants.UnmarshalMessage,
//
//					constants.ErrorCode: constants.UnmarshalError,
//				},
//			}
//
//			message[constants.Status] = constants.StatusFail
//
//			requestContext = append(requestContext, message)
//
//			err := send(requestContext, socket)
//
//			if err != nil {
//				logger.Fatal(fmt.Sprintf("unable to send message:%v", requestContext))
//			}
//
//			continue
//
//		}
//
//		wg := sync.WaitGroup{}
//
//		for _, context := range requestContext {
//
//			wg.Add(1)
//
//			go func(wg *sync.WaitGroup, context map[string]interface{}) {
//
//				defer wg.Done()
//
//				errContexts := make([]map[string]interface{}, 0)
//
//				switch context[constants.DeviceType].(string) {
//
//				case constants.LinuxDevice:
//
//					switch context[constants.RequestType].(string) {
//
//					case constants.Collect:
//
//						linux.Collect(context, &errContexts)
//
//					case constants.Discovery:
//
//						linux.Discovery(context, &errContexts)
//
//					case constants.Availability:
//
//						availability.CheckStatus(context)
//					}
//				}
//
//				if len(errContexts) > 0 {
//					context[constants.Status] = constants.StatusFail
//
//					context[constants.Error] = errContexts
//				} else {
//					context[constants.Status] = constants.StatusSuccess
//				}
//			}(&wg, context)
//		}
//
//		wg.Wait()
//
//		logger.Info(fmt.Sprintf("%v", requestContext))
//
//		err = send(requestContext, socket)
//
//		if err != nil {
//			logger.Fatal(fmt.Sprintf("unable to send message:%v", requestContext))
//		}
//	}
//}
//
//func send(result []map[string]interface{}, socket *zmq4.Socket) error {
//
//	responseContext, _ := json.Marshal(result)
//
//	encodedString := base64.StdEncoding.EncodeToString(responseContext)
//
//	_, err := socket.Send(encodedString, 0)
//
//	if err != nil {
//
//		return err
//	}
//
//	return nil
//}

// WwogIHsKICAgICJkZXZpY2UudHlwZSI6ICJsaW51eCIsCiAgICAicmVxdWVzdC50eXBlIjogImNvbGxlY3QiLAogICAgImlwIjogIjEwLjIwLjQwLjIyNyIsCiAgICAicG9ydCI6IDIyLAogICAgImRpc2NvdmVyeS5pZCI6ICIxIiwKICAgICJvYmplY3QudGltZW91dCI6IDMwLAogICAgImNyZWRlbnRpYWwiOiB7CiAgICAgICJjcmVkZW50aWFsLmlkIjogMCwKICAgICAgIm5hbWUiOiAidGVzdDIiLAogICAgICAicGFzc3dvcmQiOiAiMTAxMCIsCiAgICAgICJ1c2VybmFtZSI6ICJ5YXNoIgogICAgfSwKICAgICJwcm92aXNpb24uaWQiOiAyLAogICAgImlkIjogMgogIH0KXQ==

func main() {

	logger := utils.NewLogger("goEngine/main", "Boostrap")

	logger.Info("plugin engine started")

	var topic string

	if len(os.Args) != 2 {
		logger.Fatal("invalid boostrap start arguments")

		return
	} else {
		topic = os.Args[1]
	}

	client := ZMQclient.Connections{}

	err := client.Init(topic)

	defer client.Close()

	if err != nil {
		logger.Fatal(fmt.Sprintf("failed to create client: %v", err))

		return
	}

	client.StartReceiver()

	var channel = client.GetChannel()

	for {
		requestContext := <-channel

		wg := sync.WaitGroup{}

		for _, context := range requestContext {

			wg.Add(1)

			switch context[constants.DeviceType].(string) {

			case constants.LinuxDevice:

				switch context[constants.RequestType].(string) {

				case constants.Collect:

					go linux.Collect(context, &wg)

				case constants.Discovery:

					go linux.Discovery(context, &wg)

				case constants.Availability:

					go availability.CheckStatus(context, &wg)
				}
			}
		}

		wg.Wait()

		logger.Info(fmt.Sprintf("%v", requestContext))

		if err := client.Send(requestContext); err != nil {
			logger.Fatal(fmt.Sprintf("unable to send message:%v", requestContext))
		}
	}
}
