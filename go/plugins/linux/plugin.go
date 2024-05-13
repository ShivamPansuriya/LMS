package linux

import (
	"fmt"
	"motadata-lite/client/SSHclient"
	"motadata-lite/utils/constants"
	PluginLogger "motadata-lite/utils/logger"
	"strconv"
	"strings"
)

var logger = PluginLogger.NewLogger("plugins/linux", "plugin")

func Discovery(jsonInput map[string]interface{}, errContext *[]map[string]interface{}) {

	defer func() {
		if err := recover(); err != nil {

			logger.Error(fmt.Sprintf("%v", err))

			*errContext = append(*errContext, map[string]interface{}{
				constants.ErrorCode:    21,
				constants.ErrorMessage: "formating problem",
				constants.Error:        err,
			})
		}
	}()

	client := SSHclient.Client{}

	logger.Info(fmt.Sprintf("new client created"))

	for _, credential := range jsonInput["credentials"].([]interface{}) {

		client.SetContext(jsonInput, credential.(map[string]interface{}))

		isValid, _ := client.Init()

		if isValid {
			jsonInput[constants.Result] = map[string]interface{}{constants.ObjectIP: jsonInput[constants.ObjectIP].(string)}

			jsonInput["credential.profile.id"] = credential.(map[string]interface{})["credential.id"].(float64)
			return
		}
	}

	jsonInput["credential.profile.id"] = -1

	logger.Trace("returning to bootstrap")
}

func Collect(jsonInput map[string]interface{}, errContext *[]map[string]interface{}) {

	defer func() {
		if err := recover(); err != nil {

			logger.Error(fmt.Sprintf("%v", err))

			*errContext = append(*errContext, map[string]interface{}{
				constants.ErrorCode:    21,
				constants.ErrorMessage: "formating problem",
				constants.Error:        err,
			})
		}
	}()

	var err error

	client := SSHclient.Client{}

	var credential = jsonInput["credentials"].([]interface{})

	client.SetContext(jsonInput, credential[0].(map[string]interface{}))

	isValid, err := client.Init()

	if !isValid {
		*errContext = append(*errContext, map[string]interface{}{
			constants.ErrorCode:    12,
			constants.ErrorMessage: "Cannot establish connection to host",
			constants.Error:        err.Error(),
		})
		return
	}

	var command = "cat /sys/class/dmi/id/sys_vendor; uname -sr | awk {'print $1,$2'};uptime -s; date -d \"$(uptime -s)\" +%s; cat /sys/class/dmi/id/product_name;hostname;ps aux | awk {'print $8'} | grep -wc 'X';ps -eLf | wc -l;ps -e --no-headers | wc -l ;netstat -un | wc -l | awk '{print $1 - 2}';netstat -tn | wc -l | awk '{print $1 - 2}';cat /proc/net/snmp | grep -i 'TCP' | tail -n 1 | awk {'print $13'};ifconfig | awk '/errors/ {sum += $3} END {print sum}';ifconfig | awk '/TX packets/ {sum += $3} END {print sum}';top -bn1 | head -n 4 | tail -n 1 | awk {'print $4,$6,$10'};top -bn1 | head -n 5 | tail -n 1 | awk {'print $3,$5,$7'};free | head -n 2 | tail -n 1 | awk {'print $6'};free -b | head -n 2 | tail -n 1 | awk {'print $3,$4'};free | awk '/Mem:/ {printf(\"%.2f\\n\", ($3/$2) * 100)}';free | awk '/Mem:/ {printf(\"%.2f\\n\", ($4/($3+$4)) * 100)}';lsof | wc -l | awk '{print $1 - 2}';df --total | tail -n 1 | awk '{print $2,$4, ($4/$2)*100 \"%\"}';df --output=pcent | head -3 | tail -1 | awk {'print $NF'}\n; df --output=used / | awk 'NR==2 {print $1}';iostat |head -n 4 | tail -n -1 | awk '{print $4}';top -bn1 | head -n 1 | awk {'print $10,$11,$NF'};mpstat -I ALL | head -n 4 | tail -n 1 | awk {'print $NF'};top -bn1 | head -n 3 | tail -n 1 | awk {'print $2,$4'};mpstat | tail -n -1 | awk {'print $8'};mpstat |tail -n 1 |  awk {'print $7,$NF'};lscpu | head -n 1 | awk {'print $NF'};lscpu | head -n 5 | tail -n 1 | awk {'print $NF'};vmstat | tail -n 1 | awk {'print $12'}"

	queryOutput, err := client.ExecuteCommand(command)

	if err != nil {

		*errContext = append(*errContext, map[string]interface{}{

			constants.ErrorCode: 11,

			constants.ErrorMessage: "error in the command",

			constants.Error: err.Error(),
		})

		logger.Error(fmt.Sprintf("error in the command: %s", err.Error()))

		return

	}
	err = client.Close()

	if err != nil {
		logger.Error(fmt.Sprintf("error in closing ssh connection: %s", err.Error()))
	}

	lines := strings.Split(string(queryOutput), "\n")

	var output = make(map[string]interface{})

	defer func() {
		if r := recover(); r != nil {
			//fmt.Println("error in reading output lines", r)

			*errContext = append(*errContext, map[string]interface{}{
				constants.ErrorCode:    16,
				constants.ErrorMessage: "error in the reading output lines",
				constants.Error:        "out of index",
			})

			logger.Error(fmt.Sprintf("error in the reading output lines: %s", err.Error()))
		}
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

	output["system.interrupts.per.sec"], _ = strconv.ParseFloat(lines[6], 64)

	output["system.cpu.io.percent"], _ = strconv.ParseFloat(lines[7], 64)

	output["system.running.processes"], _ = strconv.ParseFloat(lines[8], 64)

	output["system.network.udp.connections"], _ = strconv.ParseFloat(lines[9], 64)

	output["system.network.tcp.connections"], _ = strconv.ParseFloat(lines[10], 64)

	output["system.network.tcp.retransmissions"], _ = strconv.ParseFloat(lines[11], 64)

	output["system.network.error.packets"], _ = strconv.ParseFloat(lines[12], 64)

	output["system.network.out.bytes.rate"], _ = strconv.ParseFloat(lines[13], 64)

	systemMemoryValue := strings.Split(lines[14], " ")

	output["system.memory.total.bytes"], _ = strconv.ParseFloat(systemMemoryValue[0], 64)

	output["system.memory.available.bytes"], _ = strconv.ParseFloat(systemMemoryValue[1], 64)

	output["system.cache.memory.bytes"], _ = strconv.ParseFloat(systemMemoryValue[2], 64)

	swapMemoryValue := strings.Split(lines[15], " ")

	swapProvidedValue, _ := strconv.ParseFloat(swapMemoryValue[0], 64)

	output["system.swap.memory.provisioned.bytes"] = swapProvidedValue

	swapFreeValue, _ := strconv.ParseFloat(swapMemoryValue[1], 64)

	output["system.swap.memory.free.bytes"] = swapFreeValue

	swapUsedValue, _ := strconv.ParseFloat(swapMemoryValue[2], 64)

	output["system.swap.memory.used.bytes"] = swapUsedValue

	output["system.swap.memory.used.percent"] = (swapUsedValue / swapProvidedValue) * 100

	output["system.swap.memory.free.percent"] = (swapFreeValue / swapProvidedValue) * 100

	output["system.buffer.memory.bytes"], _ = strconv.Atoi(lines[16])

	systemMemoryValues := strings.Split(lines[17], " ")

	output["system.memory.used.bytes"], _ = strconv.Atoi(systemMemoryValues[0])

	output["system.memory.free.bytes"], _ = strconv.Atoi(systemMemoryValues[1])

	output["system.memory.free.percent"] = (output["system.memory.available.bytes"].(float64) / output["system.memory.total.bytes"].(float64)) * 100

	output["system.memory.used.percent"], _ = strconv.ParseFloat(lines[18], 64)

	output["system.overall.memory.free.percent"], _ = strconv.ParseFloat(lines[19], 64)

	output["system.opened.file.descriptors"], _ = strconv.ParseFloat(lines[20], 64)

	systemDiskValue := strings.Split(lines[21], " ")

	output["system.disk.capacity.bytes"], _ = strconv.ParseFloat(systemDiskValue[0], 64)

	output["system.disk.free.bytes"], _ = strconv.ParseFloat(systemDiskValue[1], 64)

	output["system.disk.free.percent"], _ = strconv.ParseFloat(systemDiskValue[2], 64)

	output["system.disk.used.percent"], _ = strconv.ParseFloat(lines[22], 64)

	output["system.disk.used.bytes"], _ = strconv.ParseFloat(lines[23], 64)

	output["system.disk.io.time.percent"], _ = strconv.ParseFloat(lines[24], 64)

	loadAverageValue := strings.Split(lines[25], " ")

	output["system.load.avg1.min"], _ = strconv.ParseFloat(loadAverageValue[0][:len(loadAverageValue[0])-1], 64)

	output["system.load.avg5.min"], _ = strconv.ParseFloat(loadAverageValue[0][:len(loadAverageValue[1])-1], 64)

	output["system.load.avg15.min"], _ = strconv.ParseFloat(loadAverageValue[0][:len(loadAverageValue[2])], 64)

	output["system.interrupts.per.sec"], _ = strconv.ParseFloat(lines[26], 64)

	output["system.cpu.interrupt.percent"] = float64((output["system.interrupts.per.sec"].(float64) / 3000000000) * 100)

	cpuPercentValue := strings.Split(lines[27], " ")

	output["system.cpu.user.percent"], _ = strconv.ParseFloat(cpuPercentValue[0], 64)

	output["system.cpu.percent"], _ = strconv.ParseFloat(cpuPercentValue[1], 64)

	output["system.cpu.io.percent"], _ = strconv.ParseFloat(lines[28], 64)

	systemCpuValues := strings.Split(lines[29], " ")

	output["system.cpu.kernel.percent"], _ = strconv.ParseFloat(systemCpuValues[0], 64)

	output["system.cpu.idle.percent"], _ = strconv.ParseFloat(systemCpuValues[1], 64)

	output["system.cpu.type"] = lines[30]

	output["system.cpu.core"], _ = strconv.ParseFloat(lines[31], 64)

	output["system.context.switches.per.sec"], _ = strconv.ParseFloat(lines[32], 64)

	jsonInput[constants.Result] = output

}
