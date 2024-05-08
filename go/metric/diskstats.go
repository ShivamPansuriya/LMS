package metric

import (
	"fmt"
	"strconv"
	"strings"
)

func DiskQuery() string {
	return "lsof | wc -l | awk '{print $1 - 2}';df --total | tail -n 1 | awk '{print $2,$4, ($4/$2)*100 \"%\"}';df --output=pcent / | tail -n 1; df --output=used / | awk 'NR==2 {print $1}';iostat |head -n 4 | tail -n -1 | awk '{print $4}'"
}

func DiskMetrixFormatter(lines []string) map[string]interface{} {

	var output = make(map[string]interface{})

	defer func() {
		if r := recover(); r != nil {
			fmt.Println("error in reading output lines", r)
		}

		output = map[string]interface{}{}

		return
	}()

	output["system.opened.file.descriptors"], _ = strconv.ParseFloat(lines[0], 64)

	systemDiskValue := strings.Split(lines[1], " ")

	output["system.disk.capacity.bytes"], _ = strconv.ParseFloat(systemDiskValue[0], 64)

	output["system.disk.free.bytes"], _ = strconv.ParseFloat(systemDiskValue[1], 64)

	output["system.disk.free.percent"], _ = strconv.ParseFloat(systemDiskValue[2], 64)

	output["system.disk.used.percent"], _ = strconv.ParseFloat(lines[2], 64)

	output["system.disk.used.bytes"], _ = strconv.ParseFloat(lines[3], 64)

	output["system.disk.io.time.percent"], _ = strconv.ParseFloat(lines[4], 64)

	return output
}

/*
system.opened.file.descriptors = lsof | wc -l (output -1)			line[0]
system.disk.capacity.bytes, system.disk.free.bytes, system.disk.free.percent = df --total | tail -n 1 | awk '{print $2,$4, ($4/$2)*100 "%"}'	line[1]
system.disk.used.percent = df --output=pcent /					line[2]
system.disk.used.bytes = df --output=used / | awk 'NR==2 {print $1}'		line[3]
system.disk.io.time.percent = iostat |head -n 4 | tail -n -1 | awk '{print $4}'     lines[4]


*/
