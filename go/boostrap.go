package main

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"motadata-lite/plugins/linux"
	"motadata-lite/utils"
	"motadata-lite/utils/constants"
	"os"
	"sync"
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
				constants.Error:        "os.args error",
				constants.ErrorMessage: "not valid arguments",
				constants.ErrorCode:    20,
			},
		}

		message[constants.Status] = constants.StatusFail

		requestContext = append(requestContext, message)

		send(requestContext)

		return
	}

	decodedBytes, err := base64.StdEncoding.DecodeString(os.Args[1])

	logger.Info("Boostrap Started")

	if err != nil {

		logger.Fatal(fmt.Sprintf("base64 decoding error: %v", err))

		logger.Info("plugin engine stopped")

		message := map[string]interface{}{
			constants.Error: map[string]interface{}{
				constants.Error:        err.Error(),
				constants.ErrorMessage: "not valid encode request",
				constants.ErrorCode:    21,
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
				constants.Error:        err.Error(),
				constants.ErrorMessage: "unable to convert string to json map",
				constants.ErrorCode:    22,
			},
		}

		message[constants.Status] = constants.StatusFail

		requestContext = append(requestContext, message)

		send(requestContext)

		return

	}

	var wg sync.WaitGroup

	wg.Add(len(requestContext))

	for _, context := range requestContext {

		go func(wg *sync.WaitGroup, context map[string]interface{}) {

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

			wg.Done()
		}(&wg, context)
	}

	wg.Wait()

	logger.Info(fmt.Sprintf("%v", requestContext))

	send(requestContext)

	logger.Info("plugin engine Ended")
}

func send(result []map[string]interface{}) {

	responseContext, _ := json.Marshal(result)

	encodedString := base64.StdEncoding.EncodeToString(responseContext)

	fmt.Println(encodedString)

}
