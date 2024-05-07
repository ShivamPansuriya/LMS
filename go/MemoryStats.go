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
	output, err := session.Output(`ipconfig | findstr "IPv4"`)
	if err != nil {
		log.Fatalf("Failed to run mpstat: %v", err)
	}

	// Print the output to the local terminal
	fmt.Printf("%s", output)
}

/*
system.memory.total.bytes, system.memory.available.bytes , system.cache.memory.bytes = top -bn1 | head -n 4 | tail -n 1 | awk {'print $4,$6,$10'} [output * 1024]
system load avg1 min, system load avg5 min, system.load.avg15.min = top -bn1 | head -n 1 | awk {'print $10,$11,$NF'} (there will be extra comma)
system.swap.memory.provisioned.bytes, system.swap.memory.free.bytes, system.swap.memory.used.bytes = top -bn1 | head -n 5 | tail -n 1 | awk {'print $3,$5,$7'}
system.swap.memory.used.percent = system.swap.memory.used.bytes/system.swap.memory.provisioned.bytes * 100
system.swap.memory.free.percent = system.swap.memory.free.bytes/system.swap.memory.provisioned.bytes * 100
system.disk.io.time.percent = iostat |head -n 4 | tail -n -1 | awk '{print $4}'
system.context.switches.per.sec = vmstat | tail -n 1 | awk {'print $12'}
system.buffer.memory.bytes = free | head -n 2 | tail -n 1 | awk {'print $6'}
system.interrupts.per.sec = mpstat -I ALL | head -n 4 | tail -n 1 | awk {'print $NF'}
system.cpu.interrupt.percent = [(system.interrupts.per.sec / 3000000000) * 100]
system.cpu.user.percent,system.cpu.percent = top -bn1 | head -n 3 | tail -n 1 | awk {'print $2,$4'}
system.memory.used.bytes,system.memory.free.bytes = free -b | head -n 2 | tail -n 1 | awk {'print $3,$4'}
system.running.processes = ps -e --no-headers | wc -l
system.memory.free.percent = system.memory.available.bytes/system.memory.total.bytes * 100
system.cpu.io.percent = mpstat | tail -n -1 | awk {'print $8'}
system.memory.used.percent = free | awk '/Mem:/ {printf("%.2f\n", ($3/$2) * 100)}'
system.overall.memory.free.percent = free | awk '/Mem:/ {printf("%.2f\n", ($4/($3+$4)) * 100)}'
system.cpu.kernel.percent, system.cpu.idle.percent = mpstat |tail -n 1 |  awk {'print $7,$NF'}

*/
