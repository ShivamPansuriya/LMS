package SSHclient

import (
	"fmt"
	"golang.org/x/crypto/ssh"
	. "motadata-lite/src/pluginengine/constants"
	"motadata-lite/src/pluginengine/utils"
	"time"
)

type Client struct {
	client *ssh.Client

	session *ssh.Session

	port float64

	timeout int64

	hostname, password, ip string
}

var logger = utils.NewLogger("goEngine/SSH client", "SSH Client")

func (client *Client) SetContext(context map[string]interface{}, credential map[string]interface{}) {

	if context[Ip] != nil {
		client.ip = context[Ip].(string)
	} else {
		client.ip = "localhost"

		logger.Warn("IP address not found in context. setting to localhost")
	}

	if context[Username] != nil && context[Password] != nil {
		client.hostname = credential[Username].(string)

		client.password = credential[Password].(string)
	}

	if context[Port] != nil {
		client.port = context[Port].(float64)
	} else {
		client.port = 22

		logger.Warn("Port not found in context. setting to 22")
	}

	if context[TimeOut] != nil {
		client.timeout = int64(context[TimeOut].(float64))
	} else {
		client.timeout = 60

		logger.Warn("Timeout setting in context. setting to 60s")
	}
}

func (client *Client) Close() {

	if err := client.session.Close(); err != nil {
		logger.Error(fmt.Sprintf("Can't close ssh session: %v ", err))
	}

	if err := client.client.Close(); err != nil {
		logger.Error(fmt.Sprintf("Can't close ssh connection: %v ", err))

	}
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
