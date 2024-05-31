package availability

import (
	"motadata-lite/constants"
	"os/exec"
	"strings"
)

func CheckStatus(context map[string]interface{}) {

	execute := exec.Command("fping", context[constants.Ip].(string), "-c3", "-q")

	output, _ := execute.CombinedOutput()

	parts := strings.Split(string(output), " : ")

	stats := parts[1]

	segments := strings.Split(stats, ", ")

	transmitReceiveLoss := strings.Split(strings.Split(segments[0], " = ")[1], "/")

	if transmitReceiveLoss[2] == "0%" {
		// 10.20.40.128 : xmt/rcv/%loss = 3/3/0%, min/avg/max = 54.3/74.4/96.5

		// Split the second segment for min/avg/max
		minAvgMax := strings.Split(strings.Split(segments[1], " = ")[1], "/")

		context[constants.Result] = map[string]interface{}{
			"available": true,
			"xmt":       transmitReceiveLoss[0],
			"rcv":       transmitReceiveLoss[1],
			"%loss":     transmitReceiveLoss[2],
			"min":       minAvgMax[0],
			"avg":       minAvgMax[1],
			"max":       minAvgMax[2],
		}

	} else {
		context[constants.Result] = map[string]interface{}{
			"available": false,
			"xmt":       transmitReceiveLoss[0],
			"rcv":       transmitReceiveLoss[1],
			"%loss":     transmitReceiveLoss[2],
		}
	}
}
