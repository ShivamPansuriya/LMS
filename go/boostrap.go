package main

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"log"
	"motadata-lite/plugins/linux"
	"motadata-lite/utils/constants"
	"motadata-lite/utils/logger"
	"os"
	"time"
)

func main() {

	// Set the output of the logger to the file

	looger := logger.NewLogger("boostrap", "main")

	decodedBytes, err := base64.StdEncoding.DecodeString(os.Args[1])

	if err != nil {

		looger.Fatal(fmt.Sprintf("base64 decoding error: %v", err))

		return

	}

	var jsonInput []map[string]interface{}

	err = json.Unmarshal(decodedBytes, &jsonInput)

	if err != nil {

		looger.Fatal(fmt.Sprintf("unable to convert string to json map: %v", err))

		return

	}

	outputChannel := make(chan bool, len(jsonInput))

	defer close(outputChannel)

	//for _, objectIP := range jsonInput {
	//
	//	userContext := objectIP
	//
	//	go func() {
	//		errContexts := make([]map[string]interface{}, 0)
	//
	//		switch userContext[constants.DeviceType].(string) {
	//
	//		case constants.LinuxDevice:
	//
	//			middleware.Linux(userContext, &errContexts)
	//		}
	//
	//		if len(errContexts) > 0 {
	//			userContext[constants.Status] = constants.StatusFail
	//
	//			userContext[constants.Error] = errContexts
	//		} else {
	//			userContext[constants.Status] = constants.StatusSuccess
	//		}
	//
	//		log.Println(time.Now(), userContext)
	//
	//		inputChannel <- userContext
	//	}()
	//}

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

			log.Println(time.Now(), userContext)

			outputChannel <- true
		}()
	}

	for i := 0; i < len(jsonInput); i++ {
		select {
		case _ = <-outputChannel:
		}
	}

	jsonOutput, err := json.Marshal(jsonInput)

	if err != nil {

		looger.Fatal(fmt.Sprintf("json marshal error: \", err", err))

	}

	encodedString := base64.StdEncoding.EncodeToString(jsonOutput)

	fmt.Println(encodedString)
}
