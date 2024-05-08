package requesttype

import (
	"golang.org/x/crypto/ssh"
	"motadata-lite/client"
	"motadata-lite/constants"
)

func Discover(jsonInput map[string]interface{}) {

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

	_, err = client.ExecuteQuery(sshSession, "uptime")

	if err != nil {

		jsonInput[constants.Status] = constants.StatusFail

		jsonInput[constants.Error] = map[string]interface{}{

			constants.ErrorCode: 11,

			constants.ErrorMessage: "error in the command",

			constants.Error: err.Error(),
		}

		return

	}

	jsonInput[constants.Status] = constants.StatusSuccess

	jsonInput[constants.Result] = map[string]interface{}{constants.ObjectIP: jsonInput[constants.ObjectIP].(string)}

	return
}
