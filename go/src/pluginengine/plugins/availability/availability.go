package availability

import (
	"motadata-lite/src/pluginengine/constants"
	"os/exec"
	"strings"
	"sync"
)

func CheckStatus(context map[string]interface{}, wg *sync.WaitGroup) {

	defer wg.Done()

	execute := exec.Command("fping", context[constants.Ip].(string), "-c3", "-q")

	output, _ := execute.CombinedOutput()

	parts := strings.Split(string(output), " : ")

	stats := parts[1]

	segments := strings.Split(stats, ", ")

	transmitReceiveLoss := strings.Split(strings.Split(segments[0], " = ")[1], "/")

	context[constants.Result] = map[string]interface{}{
		"available": false,
		"xmt":       transmitReceiveLoss[0],
		"rcv":       transmitReceiveLoss[1],
		"%loss":     transmitReceiveLoss[2],
	}

	if transmitReceiveLoss[2] == "0%" {
		// Split the second segment for min/avg/max
		minAvgMax := strings.Split(strings.Split(segments[1], " = ")[1], "/")

		context[constants.Result] = map[string]interface{}{
			"available": true,
			"min":       minAvgMax[0],
			"avg":       minAvgMax[1],
			"max":       minAvgMax[2],
		}
	}

	return
}
