package main

import (
	"fmt"
	"golang.org/x/crypto/ssh"
	"log"
)

func main() {
	// Configure the SSH client
	config := &ssh.ClientConfig{
		User: "pansu",
		Auth: []ssh.AuthMethod{
			ssh.Password("Shivam@098"),
		},
		HostKeyCallback: ssh.InsecureIgnoreHostKey(),
	}

	// Connect to the remote server
	client, err := ssh.Dial("tcp", "192.168.152.206:22", config)
	if err != nil {
		log.Fatalf("Failed to connect: %v", err)
	}
	defer client.Close()

	// Create a new session
	session, err := client.NewSession()
	if err != nil {
		log.Fatalf("Failed to create session: %v", err)
	}
	defer session.Close()

	// Run the mpstat command on the remote server
	output, err := session.Output(`ifconfig enp0s31f6 | grep "RX packets" | awk '{print $5}';`)
	if err != nil {
		log.Fatalf("Failed to run mpstat: %v", err)
	}

	// Print the output to the local terminal
	fmt.Printf("%s", output)
}

/*

system.network.udp.connections = netstat -un | wc -l (output -2)
system.network.tcp.connections = netstat -tn | wc -l (output -2)
system.network.tcp.retransmissions = cat /proc/net/snmp | grep -i 'TCP' | tail -n 1 | awk {'print $13'}
system.network.error.packets = ifconfig | awk '/errors/ {sum += $3} END {print sum}'
system.network.out.bytes.rate = ifconfig | awk '/TX packets/ {sum += $3} END {print sum}'
*/
