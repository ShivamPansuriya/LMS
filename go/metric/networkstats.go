package metric

import (
	"fmt"
	"strconv"
)

func NetworkQuery() string {
	return "netstat -un | wc -l | awk '{print $1 - 2}';netstat -tn | wc -l | awk '{print $1 - 2}';cat /proc/net/snmp | grep -i 'TCP' | tail -n 1 | awk {'print $13'};ifconfig | awk '/errors/ {sum += $3} END {print sum}';ifconfig | awk '/TX packets/ {sum += $3} END {print sum}'"
}

func NetworkMetrixFormatter(lines []string) map[string]interface{} {

	var output = make(map[string]interface{})

	defer func() {
		if r := recover(); r != nil {
			fmt.Println("error in reading output lines", r)
		}

		output = map[string]interface{}{}

		return
	}()

	output["system.network.udp.connections"], _ = strconv.ParseFloat(lines[0], 64)

	output["system.network.tcp.connections"], _ = strconv.ParseFloat(lines[1], 64)

	output["system.network.tcp.retransmissions"], _ = strconv.ParseFloat(lines[2], 64)

	output["system.network.error.packets"], _ = strconv.ParseFloat(lines[3], 64)

	output["system.network.out.bytes.rate"], _ = strconv.ParseFloat(lines[4], 64)

	return output
}

/*

system.network.udp.connections = netstat -un | wc -l (output -2) 		line[0]
system.network.tcp.connections = netstat -tn | wc -l (output -2)		line[1]
system.network.tcp.retransmissions = cat /proc/net/snmp | grep -i 'TCP' | tail -n 1 | awk {'print $13'}		line[2]
system.network.error.packets = ifconfig | awk '/errors/ {sum += $3} END {print sum}'		line[3]
system.network.out.bytes.rate = ifconfig | awk '/TX packets/ {sum += $3} END {print sum}'		line[4]
*/
