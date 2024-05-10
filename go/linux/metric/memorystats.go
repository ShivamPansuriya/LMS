package metric

import (
	"fmt"
	"strconv"
	"strings"
)

//func main() {
//
//	defer func() {
//		if r := recover(); r != nil {
//			fmt.Println("unable to convert string to json map: ", r)
//		}
//	}()
//
//	var jsonInput map[string]interface{}
//
//	err := json.Unmarshal([]byte(os.Args[1]), &jsonInput)
//
//	// Configure the SSH client
//	config := &ssh.ClientConfig{
//		User: jsonInput["user"].(string),
//		Auth: []ssh.AuthMethod{
//			ssh.Password(jsonInput["password"].(string)),
//		},
//		HostKeyCallback: ssh.InsecureIgnoreHostKey(),
//	}
//
//	// Connect to the remote server
//	connectStr := fmt.Sprintf("%s:%d", jsonInput["host"].(string), jsonInput["port"].(int))
//
//	client, err := ssh.Dial("tcp", connectStr, config)
//
//	if err != nil {
//		fmt.Printf("Failed to connect through ssh: %v\n", err)
//
//		return
//	}
//
//	defer client.Close()
//
//	// Create a new session
//	sshSession, err := client.NewSession()
//
//	if err != nil {
//
//		fmt.Printf("Failed to create session: %v\n", err)
//
//		return
//	}
//
//	defer sshSession.Close()
//
//	// running the command on the remote server
//
//	sshResult, err := sshSession.Output(`ipconfig | findstr "IPv4"`)
//
//	if err != nil {
//
//		fmt.Printf("Failed to run command: %v\n", err)
//
//		return
//
//	}
//	lines := strings.Split(string(sshResult), "\n")
//
//	output := metrixFormatter(lines)
//
//	jsonResult, err := json.Marshal(output)
//
//	if err != nil {
//		fmt.Println("Error marshaling output JSON:", err)
//
//		return
//	}
//
//	jsonInput["result"] = jsonResult
//
//	// Convert the output map to JSON
//	jsonOutput, err := json.Marshal(jsonInput)
//
//	if err != nil {
//		fmt.Println("Error marshaling output JSON:", err)
//
//		return
//	}
//
//	fmt.Printf("%s", jsonOutput)
//
//}

func MemoryQuery() string {
	return "top -bn1 | head -n 4 | tail -n 1 | awk {'print $4,$6,$10'};top -bn1 | head -n 5 | tail -n 1 | awk {'print $3,$5,$7'};free | head -n 2 | tail -n 1 | awk {'print $6'};free -b | head -n 2 | tail -n 1 | awk {'print $3,$4'};free | awk '/Mem:/ {printf(\"%.2f\\n\", ($3/$2) * 100)}';free | awk '/Mem:/ {printf(\"%.2f\\n\", ($4/($3+$4)) * 100)}'"
}

func MemoryMetrixFormatter(lines []string) map[string]interface{} {

	var output = make(map[string]interface{})

	defer func() {
		if r := recover(); r != nil {
			fmt.Println("error in reading output lines", r)
		}

		output = map[string]interface{}{}

		return
	}()

	systemMemoryValue := strings.Split(lines[0], " ")

	output["system.memory.total.bytes"], _ = strconv.ParseFloat(systemMemoryValue[0], 64)

	output["system.memory.available.bytes"], _ = strconv.ParseFloat(systemMemoryValue[1], 64)

	output["system.cache.memory.bytes"], _ = strconv.ParseFloat(systemMemoryValue[2], 64)

	swapMemoryValue := strings.Split(lines[1], " ")

	swapProvidedValue, _ := strconv.ParseFloat(swapMemoryValue[0], 64)

	output["system.swap.memory.provisioned.bytes"] = swapProvidedValue

	swapFreeValue, _ := strconv.ParseFloat(swapMemoryValue[1], 64)

	output["system.swap.memory.free.bytes"] = swapFreeValue

	swapUsedValue, _ := strconv.ParseFloat(swapMemoryValue[2], 64)

	output["system.swap.memory.used.bytes"] = swapUsedValue

	output["system.swap.memory.used.percent"] = (swapUsedValue / swapProvidedValue) * 100

	output["system.swap.memory.free.percent"] = (swapFreeValue / swapProvidedValue) * 100

	output["system.buffer.memory.bytes"], _ = strconv.Atoi(lines[2])

	systemMemoryValues := strings.Split(lines[3], " ")

	output["system.memory.used.bytes"], _ = strconv.Atoi(systemMemoryValues[0])

	output["system.memory.free.bytes"], _ = strconv.Atoi(systemMemoryValues[1])

	output["system.memory.free.percent"] = (output["system.memory.available.bytes"].(float64) / output["system.memory.total.bytes"].(float64)) * 100

	output["system.memory.used.percent"], _ = strconv.ParseFloat(lines[4], 64)

	output["system.overall.memory.free.percent"], _ = strconv.ParseFloat(lines[5], 64)

	return output
}

//
/*
system.memory.total.bytes, system.memory.available.bytes , system.cache.memory.bytes = top -bn1 | head -n 4 | tail -n 1 | awk {'print $4,$6,$10'}		line[0]
system.swap.memory.provisioned.bytes, system.swap.memory.free.bytes, system.swap.memory.used.bytes = top -bn1 | head -n 5 | tail -n 1 | awk {'print $3,$5,$7'}		line[1]
system.swap.memory.used.percent = system.swap.memory.used.bytes/system.swap.memory.provisioned.bytes * 100
system.swap.memory.free.percent = system.swap.memory.free.bytes/system.swap.memory.provisioned.bytes * 100
system.buffer.memory.bytes = free | head -n 2 | tail -n 1 | awk {'print $6'}		lines[2]
system.memory.used.bytes,system.memory.free.bytes = free -b | head -n 2 | tail -n 1 | awk {'print $3,$4'}		line[3]
system.memory.free.percent = system.memory.available.bytes/system.memory.total.bytes * 100
system.memory.used.percent = free | awk '/Mem:/ {printf("%.2f\n", ($3/$2) * 100)}'		line[4]
system.overall.memory.free.percent = free | awk '/Mem:/ {printf("%.2f\n", ($4/($3+$4)) * 100)}'  	line[5]
*/
