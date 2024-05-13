package SSHclient

import (
	"fmt"
	"golang.org/x/crypto/ssh"
	"motadata-lite/utils/constants"
	"motadata-lite/utils/logger"
	"time"
)

type Client struct {
	ip string

	hostname string

	port float64

	password string

	client *ssh.Client

	session *ssh.Session

	logger logger.Logger
}

func (client *Client) SetContext(context map[string]interface{}, credential map[string]interface{}) {
	client.logger = logger.NewLogger("client", "SSH Client")

	client.ip = context[constants.ObjectIP].(string)

	client.hostname = credential[constants.ObjectHost].(string)

	client.port = context[constants.ObjectPort].(float64)

	client.password = credential[constants.ObjectPassword].(string)

}

func (client *Client) Close() error {

	err := client.client.Close()
	if err != nil {
		client.logger.Error(fmt.Sprintf("Can't close ssh connection: %v ", err))
		return err
	}

	err = client.session.Close()
	if err != nil {
		client.logger.Error(fmt.Sprintf("Can't close ssh session: %v ", err))
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

		Timeout: 10 * time.Second, // Set the timeout to 20 seconds
	}

	// Connect to the remote server
	connectStr := fmt.Sprintf("%s:%v", client.ip, client.port)

	sshConnection, err := ssh.Dial("tcp", connectStr, config)

	client.client = sshConnection

	client.logger.Info("hello")

	if err != nil {

		client.logger.Info(fmt.Sprintf("failed to connet raason: %s", err.Error()))

		return false, err
	}

	sshSession, err := sshConnection.NewSession()

	client.session = sshSession

	return true, err

}

func (client *Client) ExecuteCommand(query string) ([]byte, error) {
	return client.session.Output(query)
}
