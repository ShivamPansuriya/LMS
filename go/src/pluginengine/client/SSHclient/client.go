package SSHclient

import (
	"fmt"
	"golang.org/x/crypto/ssh"
	"motadata-lite/src/pluginengine/utils"
	. "motadata-lite/src/pluginengine/utils/constants"
	"time"
)

type Client struct {
	hostname, password, ip string

	port float64

	timeout int64

	client *ssh.Client

	session *ssh.Session
}

var logger = utils.NewLogger("goEngine/SSH client", "SSH Client")

func (client *Client) SetContext(context map[string]interface{}, credential map[string]interface{}) {

	client.ip = context[Ip].(string)

	client.hostname = credential[Username].(string)

	client.port = context[Port].(float64)

	client.timeout = int64(context[TimeOut].(float64))

	client.password = credential[Password].(string)

}

func (client *Client) Close() (err error) {

	if err = client.session.Close(); err != nil {
		logger.Error(fmt.Sprintf("Can't close ssh session: %v ", err))
	}

	if err = client.client.Close(); err != nil {
		logger.Error(fmt.Sprintf("Can't close ssh connection: %v ", err))

	}

	return err
}

func (client *Client) Init() (err error) {

	// Connect to the remote server
	if client.client, err = ssh.Dial("tcp", fmt.Sprintf("%s:%v", client.ip, client.port), &ssh.ClientConfig{

		User: client.hostname,

		Auth: []ssh.AuthMethod{

			ssh.Password(client.password),
		},

		HostKeyCallback: ssh.InsecureIgnoreHostKey(),

		Timeout: time.Duration(client.timeout * int64(time.Second)), // Set the timeout
	}); err != nil {

		logger.Error(fmt.Sprintf("failed to connect reason: %s", err.Error()))

		return
	}

	if client.session, err = client.client.NewSession(); err != nil {

		logger.Error(fmt.Sprintf("failed to create session reason: %s", err.Error()))

		return
	}

	return err
}

func (client *Client) ExecuteCommand(command string) ([]byte, error) {
	return client.session.Output(command)
}
