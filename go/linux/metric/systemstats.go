package metric

import (
	"fmt"
	"strconv"
	"strings"
)

func SystemQuery() string {
	return "sudo dmidecode -s system-manufacturer; uname -sr | awk {'print $1,$2'};uptime -s; date -d \"$(uptime -s)\" +%s; cat /sys/class/dmi/id/product_name;hostname"
}

func SystemMetrixFormatter(lines []string) map[string]interface{} {

	var output = make(map[string]interface{})

	defer func() {
		if r := recover(); r != nil {
			fmt.Println("error in reading output lines", r)
		}

		output = map[string]interface{}{}

		return
	}()

	output["system.vendor"] = lines[0]

	osValues := strings.Split(lines[1], " ")

	output["system.os.name"] = osValues[0]

	output["system.os.version"] = osValues[1]

	output["started.time"] = lines[2]

	output["started.time.seconds"], _ = strconv.ParseFloat(lines[3], 64)

	output["system.model"] = lines[4]

	output["system.model"] = lines[5]

	return output
}

/*

system vendor = sudo dmidecode -s system-manufacturer
system.os.name, system.os.version = uname -sr | awk {'print $1,$2'}
started.time= uptime -s
started.time.seconds = date -d "$(uptime -s)" +%s
system.model = cat /sys/class/dmi/id/product_name
system.name = hostname
*/
