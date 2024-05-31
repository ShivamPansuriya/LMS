package boostraps

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

func StartAgent(socket *zmq4.Socket) {

	logger := utils.NewLogger("goEngine/main", "agentBoostrap")

	var decodedBytes []byte

	var requestContext []map[string]interface{}

	fileData, err := os.ReadFile("./config.json")

	if err != nil {

		fmt.Println("Error reading file:", err)

		return
	}

	decodedBytes = fileData

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

		return
	}

	connectionStr := "tcp://" + requestContext[0][constants.HostIP].(string) + ":" + fmt.Sprintf("%v", requestContext[0][constants.HostPort].(float64))

	err = socket.Connect(connectionStr)

	if err != nil {

		logger.Error(err.Error())
	}

	wg := sync.WaitGroup{}

	for {
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
		}(&wg, requestContext[0])

		wg.Wait()

		responseContext, _ := json.Marshal(requestContext)

		encodedString := base64.StdEncoding.EncodeToString(responseContext)

		send, err := socket.Send(encodedString, 0)

		if err != nil {

			logger.Error(err.Error())
		}

		logger.Info(fmt.Sprintf("Result sent to socket,data: %v,%v", send, encodedString))

		time.Sleep(time.Duration(requestContext[0][constants.TimeOut].(float64) * float64(time.Second)))
	}
}
