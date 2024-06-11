package main

import (
	"fmt"
	"motadata-lite/src/pluginengine/client/ZMQclient"
	"motadata-lite/src/pluginengine/constants"
	"motadata-lite/src/pluginengine/plugins/availability"
	"motadata-lite/src/pluginengine/plugins/linux"
	"motadata-lite/src/pluginengine/utils"
	"os"
	"sync"
)

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
