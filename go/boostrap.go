package main

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"motadata-lite/plugins/linux"
	"motadata-lite/utils/constants"
	"motadata-lite/utils/logger"
	"os"
)

func main() {

	// Set the output of the logger to the file

	logger := logger.NewLogger("logs", "main")

	decodedBytes, err := base64.StdEncoding.DecodeString(os.Args[1])

	logger.Info("Boostrap Started")

	if err != nil {

		logger.Fatal(fmt.Sprintf("base64 decoding error: %v", err))

		return

	}

	var jsonInput []map[string]interface{}

	err = json.Unmarshal(decodedBytes, &jsonInput)

	if err != nil {

		logger.Fatal(fmt.Sprintf("unable to convert string to json map: %v", err))

		return

	}

	outputChannel := make(chan bool, len(jsonInput))

	defer close(outputChannel)

	for _, objectIP := range jsonInput {

		userContext := objectIP

		go func() {
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

			outputChannel <- true
		}()
	}

	for i := 0; i < len(jsonInput); i++ {
		select {
		case _ = <-outputChannel:
		}
	}
	logger.Info(fmt.Sprintf("%v", jsonInput))

	jsonOutput, err := json.Marshal(jsonInput)

	if err != nil {

		logger.Fatal(fmt.Sprintf("json marshal error: %v", err))

	}
	encodedString := base64.StdEncoding.EncodeToString(jsonOutput)

	fmt.Println(encodedString)

	logger.Info("Boostrap Ended")

}
