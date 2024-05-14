package main

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"motadata-lite/plugins/linux"
	"motadata-lite/utils/constants"
	"motadata-lite/utils/logger"
	"os"
	"sync"
)

func main() {
	// Set the output of the logger to the file

	logger := logger.NewLogger("goEngine/logs", "main")

	logger.Info("plugin engine started")

	var jsonInput []map[string]interface{}

	if len(os.Args) != 2 {

		logger.Fatal(fmt.Sprintf("not valid argument size"))

		logger.Info("plugin engine stopped")

		error := map[string]interface{}{
			constants.Error: map[string]interface{}{
				constants.Error:        "os.args error",
				constants.ErrorMessage: "not valid arguments",
				constants.ErrorCode:    20,
			},
		}
		jsonInput = append(jsonInput, error)

		send(jsonInput)

		return
	}

	decodedBytes, err := base64.StdEncoding.DecodeString(os.Args[1])

	logger.Info("Boostrap Started")

	if err != nil {

		logger.Fatal(fmt.Sprintf("base64 decoding error: %v", err))

		logger.Info("plugin engine stopped")

		error := map[string]interface{}{
			constants.Error: map[string]interface{}{
				constants.Error:        err.Error(),
				constants.ErrorMessage: "not valid encode request",
				constants.ErrorCode:    21,
			},
		}
		jsonInput = append(jsonInput, error)

		send(jsonInput)

		return

	}

	err = json.Unmarshal(decodedBytes, &jsonInput)

	if err != nil {

		logger.Fatal(fmt.Sprintf("unable to convert string to json map: %v", err))

		logger.Info("plugin engine stopped")

		error := map[string]interface{}{
			constants.Error: map[string]interface{}{
				constants.Error:        err.Error(),
				constants.ErrorMessage: "unable to convert string to json map",
				constants.ErrorCode:    22,
			},
		}
		jsonInput = append(jsonInput, error)

		send(jsonInput)

		return

	}

	var wg sync.WaitGroup

	wg.Add(len(jsonInput))

	for _, objectIP := range jsonInput {

		userContext := objectIP

		go func(wg *sync.WaitGroup) {

			errContexts := make([]map[string]interface{}, 0)

			switch userContext[constants.DeviceType].(string) {

			case constants.LinuxDevice:

				switch userContext[constants.RequestType].(string) {

				case constants.Collect:

					linux.Collect(userContext, &errContexts)

				case constants.Discovery:

					linux.Discovery(userContext, &errContexts)
				}
			}

			if len(errContexts) > 0 {
				userContext[constants.Status] = constants.StatusFail

				userContext[constants.Error] = errContexts
			} else {
				userContext[constants.Status] = constants.StatusSuccess
			}

			wg.Done()
		}(&wg)
	}

	wg.Wait()

	logger.Info(fmt.Sprintf("%v", jsonInput))

	send(jsonInput)

	logger.Info("plugin engine Ended")
}

func send(result []map[string]interface{}) {

	jsonOutput, _ := json.Marshal(result)

	encodedString := base64.StdEncoding.EncodeToString(jsonOutput)

	fmt.Println(encodedString)

}
