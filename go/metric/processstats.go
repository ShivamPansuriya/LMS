package metric

import (
	"fmt"
	"strconv"
)

func ProcessQuery() string {
	return "ps aux | awk {'print $8'} | grep -wc 'X';ps -eLf | wc -l;ps -e --no-headers | wc -l "
}

func ProcessMetrixFormatter(lines []string) map[string]interface{} {

	var output = make(map[string]interface{})

	defer func() {
		if r := recover(); r != nil {
			fmt.Println("error in reading output lines", r)
		}

		output = map[string]interface{}{}

		return
	}()

	output["system.interrupts.per.sec"], _ = strconv.ParseFloat(lines[0], 64)

	output["system.cpu.io.percent"], _ = strconv.ParseFloat(lines[1], 64)

	output["system.running.processes"], _ = strconv.ParseFloat(lines[2], 64)

	return output
}

/*
system blocked processes = ps aux | awk {'print $8'} | grep -wc 'X'		line[0]
system.threads = ps -eLf | wc -l		line[1]
system.running.processes = ps -e --no-headers | wc -l 		line[2]
*/
