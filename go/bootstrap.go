package main

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"github.com/pebbe/zmq4"
	"motadata-lite/constants"
	"motadata-lite/plugins/linux"
	"motadata-lite/utils"
	"os"
	"sync"
	"time"
)

func main() {
	// Set the output of the logger to the file

	logger := utils.NewLogger("goEngine/main", "Boostrap")

	logger.Info("plugin engine started")

	var requestContext []map[string]interface{}

	if len(os.Args) != 2 {

		logger.Fatal(fmt.Sprintf("not valid argument size"))

		logger.Info("plugin engine stopped")

		message := map[string]interface{}{

			constants.Error: map[string]interface{}{

				constants.Error: "os.args error",

				constants.ErrorMessage: constants.InvalidArgumentMessage,

				constants.ErrorCode: constants.InvalidArgumentCode,
			},
		}

		message[constants.Status] = constants.StatusFail

		requestContext = append(requestContext, message)

		send(requestContext)

		return
	}

	var flag bool

	if os.Args[1] == constants.Agent {
		flag = true
	}

	zmq, _ := zmq4.NewContext()

	defer zmq.Term()

	// Create a new PUB socket
	socket, _ := zmq.NewSocket(zmq4.PUSH)

	defer socket.Close()

	var decodedBytes []byte

	var err error

	if flag {
		fileData, err := os.ReadFile("./config.json")

		if err != nil {

			fmt.Println("Error reading file:", err)

			return
		}

		decodedBytes = fileData

	} else {
		decodedBytes, err = base64.StdEncoding.DecodeString(os.Args[1])
	}

	logger.Info("Boostrap Started")

	if err != nil {

		logger.Fatal(fmt.Sprintf("base64 decoding error: %v", err))

		logger.Info("plugin engine stopped")

		message := map[string]interface{}{

			constants.Error: map[string]interface{}{

				constants.Error: err.Error(),

				constants.ErrorMessage: constants.EncodeErrorMessage,

				constants.ErrorCode: constants.EncodeError,
			},
		}

		message[constants.Status] = constants.StatusFail

		requestContext = append(requestContext, message)

		send(requestContext)

		return

	}

	err = json.Unmarshal(decodedBytes, &requestContext)

	if err != nil {

		logger.Fatal(fmt.Sprintf("unable to convert string to json map: %v", err))

		logger.Info("plugin engine stopped")

		message := map[string]interface{}{

			constants.Error: map[string]interface{}{

				constants.Error: err.Error(),

				constants.ErrorMessage: constants.UnmarshalMessage,

				constants.ErrorCode: constants.UnmarshalError,
			},
		}

		message[constants.Status] = constants.StatusFail

		requestContext = append(requestContext, message)

		send(requestContext)

		return

	}

	wg := sync.WaitGroup{}

	for {
		for _, context := range requestContext {

			wg.Add(1)

			go func(wg *sync.WaitGroup, context map[string]interface{}) {

				defer wg.Done()

				errContexts := make([]map[string]interface{}, 0)

				switch context[constants.DeviceType].(string) {

				case constants.LinuxDevice:

					switch context[constants.RequestType].(string) {

					case constants.Collect:

						linux.Collect(context, &errContexts)

					case constants.Discovery:

						linux.Discovery(context, &errContexts)
					}
				}

				if len(errContexts) > 0 {
					context[constants.Status] = constants.StatusFail

					context[constants.Error] = errContexts
				} else {
					context[constants.Status] = constants.StatusSuccess
				}
			}(&wg, context)
		}

		wg.Wait()
		if flag {
			connectionStr := "tcp://" + requestContext[0][constants.HostIP].(string) + ":" + fmt.Sprintf("%v", requestContext[0][constants.HostPort].(float64))

			err := socket.Connect(connectionStr)

			if err != nil {

				logger.Error(err.Error())
			}

			responseContext, _ := json.Marshal(requestContext)

			encodedString := base64.StdEncoding.EncodeToString(responseContext)

			send, err := socket.Send(encodedString, 0)

			if err != nil {

				logger.Error(err.Error())
			}

			logger.Info(fmt.Sprintf("Result sent to socket,data: %v,%v", send, encodedString))

			time.Sleep(time.Duration(requestContext[0][constants.TimeOut].(float64) * float64(time.Second)))

		} else {
			logger.Info(fmt.Sprintf("%v", requestContext))

			send(requestContext)

			logger.Info("plugin engine Ended")

			break
		}
	}
}

func send(result []map[string]interface{}) {

	responseContext, _ := json.Marshal(result)

	encodedString := base64.StdEncoding.EncodeToString(responseContext)

	fmt.Println(encodedString)

}

// WwogIHsKICAgICJkZXZpY2UudHlwZSI6ICJsaW51eCIsCiAgICAicmVxdWVzdC50eXBlIjogImNvbGxlY3QiLAogICAgImlwIjogIjEwLjIwLjQwLjIyNyIsCiAgICAicG9ydCI6IDIyLAogICAgImRpc2NvdmVyeS5pZCI6ICIxIiwKICAgICJvYmplY3QudGltZW91dCI6IDMwLAogICAgImNyZWRlbnRpYWwiOiB7CiAgICAgICJjcmVkZW50aWFsLmlkIjogMCwKICAgICAgIm5hbWUiOiAidGVzdDIiLAogICAgICAicGFzc3dvcmQiOiAiMTAxMCIsCiAgICAgICJ1c2VybmFtZSI6ICJ5YXNoIgogICAgfSwKICAgICJwcm92aXNpb24uaWQiOiAyLAogICAgImlkIjogMgogIH0KXQ==
