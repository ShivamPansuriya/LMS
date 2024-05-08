package client

import (
	"fmt"
	"golang.org/x/crypto/ssh"
	"log"
	"time"
)

func Connect(objectIp string, objectPort float64, objectUser string, objectPassword string) (*ssh.Session, error, *ssh.Client) {

	config := &ssh.ClientConfig{

		User: objectUser,

		Auth: []ssh.AuthMethod{

			ssh.Password(objectPassword),
		},

		HostKeyCallback: ssh.InsecureIgnoreHostKey(),

		Timeout: 10 * time.Second, // Set the timeout to 20 seconds
	}

	// Connect to the remote server
	connectStr := fmt.Sprintf("%s:%v", objectIp, objectPort)

	sshConnection, err := ssh.Dial("tcp", connectStr, config)

	if err != nil {

		log.Println("Failed to establish SSH connection to ", err)

		return nil, err, nil
	}

	sshSession, err := sshConnection.NewSession()

	return sshSession, err, sshConnection

}

func ExecuteQuery(session *ssh.Session, query string) ([]byte, error) {
	return session.Output(query)
}
