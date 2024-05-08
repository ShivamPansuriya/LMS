package main

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"log"
	"motadata-lite/constants"
	"motadata-lite/requesttype"
	"os"
	"time"
)

func main() {

	file, err := os.OpenFile("boostrap.log", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)

	if err != nil {
		log.Fatal(err)
	}

	defer file.Close()

	// Set the output of the logger to the file

	log.SetOutput(file)

	decodedBytes, err := base64.StdEncoding.DecodeString(os.Args[1])

	if err != nil {

		log.Fatal("base64 decoding error: ", err)

		return

	}

	var jsonInput map[string]interface{}

	err = json.Unmarshal(decodedBytes, &jsonInput)

	if err != nil {

		log.Fatal("unable to convert string to json map: ", err)

		return

	}

	if jsonInput[constants.RequestType] != nil && jsonInput[constants.MetricName] != nil && jsonInput[constants.ObjectHost] != nil && jsonInput[constants.ObjectPassword] != nil {
		switch jsonInput[constants.RequestType].(string) {

		case constants.Discovery:

			requesttype.Discover(jsonInput)

		case constants.Collect:

			requesttype.Collector(jsonInput)
		}
	} else {
		jsonInput[constants.Status] = constants.StatusFail
		jsonInput[constants.Error] = map[string]interface{}{
			constants.ErrorCode:    6,
			constants.ErrorMessage: "not valid json object",
			constants.Error:        "nil value in request type/ metric type/ host/ password",
		}
	}

	log.Println(time.Now(), jsonInput)

	jsonOutput, err := json.Marshal(jsonInput)

	if err != nil {

		log.Fatal("json marshal error: ", err)

	}

	encodedString := base64.StdEncoding.EncodeToString(jsonOutput)

	fmt.Println(encodedString)
}
