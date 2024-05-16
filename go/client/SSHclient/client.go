package SSHclient

import (
	"fmt"
	"golang.org/x/crypto/ssh"
	"motadata-lite/utils"
	"motadata-lite/utils/constants"
	"time"
)

type Client struct {
	ip string

	hostname string

	port float64

	timeout float64

	password string

	client *ssh.Client

	session *ssh.Session

	logger utils.Logger
}

func (client *Client) SetContext(context map[string]interface{}, credential map[string]interface{}) {
	client.logger = utils.NewLogger("goEngine/SSH client", "SSH Client")

	client.ip = context[constants.ObjectIP].(string)

	client.hostname = credential[constants.ObjectHost].(string)

	client.port = context[constants.ObjectPort].(float64)

	client.timeout = context[constants.TimeOut].(float64)

	client.password = credential[constants.ObjectPassword].(string)

}

func (client *Client) Close() error {

	err := client.session.Close()

	if err != nil {
		client.logger.Error(fmt.Sprintf("Can't close ssh session: %v ", err))
		return err
	}

	err = client.client.Close()

	if err != nil {
		client.logger.Error(fmt.Sprintf("Can't close ssh connection: %v ", err))
		return err
	}
	return err
}

func (client *Client) Init() (bool, error) {

	config := &ssh.ClientConfig{

		User: client.hostname,

		Auth: []ssh.AuthMethod{

			ssh.Password(client.password),
		},

		HostKeyCallback: ssh.InsecureIgnoreHostKey(),

		Timeout: time.Duration(client.timeout * float64(time.Second)), // Set the timeout
	}

	// Connect to the remote server
	sshConnection, err := ssh.Dial("tcp", fmt.Sprintf("%s:%v", client.ip, client.port), config)

	if err != nil {

		client.logger.Info(fmt.Sprintf("failed to connet reason: %s", err.Error()))

		return false, err
	}

	client.client = sshConnection

	sshSession, err := sshConnection.NewSession()

	if err != nil {
		client.logger.Info(fmt.Sprintf("failed to create session reason: %s", err.Error()))

		return false, err

	}
	client.session = sshSession

	return true, err

}

func (client *Client) ExecuteCommand(query string) ([]byte, error) {
	return client.session.Output(query)
}
