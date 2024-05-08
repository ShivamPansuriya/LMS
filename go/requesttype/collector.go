package requesttype

import (
	"golang.org/x/crypto/ssh"
	"motadata-lite/client"
	"motadata-lite/constants"
	"motadata-lite/metric"
	"strings"
)

func Collector(jsonInput map[string]interface{}) {
	var err error

	var sshSession *ssh.Session

	var sshClient *ssh.Client

	if jsonInput[constants.ObjectPort] == nil {

		sshSession, err, sshClient = client.Connect(jsonInput[constants.ObjectIP].(string), 22, jsonInput[constants.ObjectHost].(string), jsonInput[constants.ObjectPassword].(string))

	} else {

		sshSession, err, sshClient = client.Connect(jsonInput[constants.ObjectIP].(string), jsonInput[constants.ObjectPort].(float64), jsonInput[constants.ObjectHost].(string), jsonInput[constants.ObjectPassword].(string))

	}
	if err != nil {

		jsonInput[constants.Status] = constants.StatusFail

		jsonInput[constants.Error] = map[string]interface{}{

			constants.ErrorCode: 2,

			constants.ErrorMessage: "unable to establish ssh connection",

			constants.Error: err.Error(),
		}

		return

	}

	defer sshClient.Close()

	defer sshSession.Close()

	var query string

	switch jsonInput[constants.MetricName].(string) {

	case constants.Memory:
		query = metric.MemoryQuery()

	case constants.Disk:
		query = metric.DiskQuery()

	case constants.Network:
		query = metric.NetworkQuery()

	case constants.CPU:
		query = metric.CpuQuery()

	case constants.Process:
		query = metric.ProcessQuery()

	case constants.System:
		query = metric.SystemQuery()
	}

	queryOutput, err := client.ExecuteQuery(sshSession, query)

	if err != nil {

		jsonInput[constants.Status] = constants.StatusFail

		jsonInput[constants.Error] = map[string]interface{}{

			constants.ErrorCode: 11,

			constants.ErrorMessage: "error in the command",

			constants.Error: err.Error(),
		}

		return

	}

	lines := strings.Split(string(queryOutput), "\n")

	jsonInput[constants.Status] = constants.StatusSuccess

	switch jsonInput[constants.MetricName].(string) {

	case constants.Memory:
		jsonInput[constants.Result] = metric.MemoryMetrixFormatter(lines)

	case constants.Disk:
		jsonInput[constants.Result] = metric.DiskMetrixFormatter(lines)

	case constants.Network:
		jsonInput[constants.Result] = metric.NetworkMetrixFormatter(lines)

	case constants.CPU:
		jsonInput[constants.Result] = metric.CpuMetrixFormatter(lines)

	case constants.Process:
		jsonInput[constants.Result] = metric.ProcessMetrixFormatter(lines)

	case constants.System:
		jsonInput[constants.Result] = metric.SystemMetrixFormatter(lines)
	}

	return
}
