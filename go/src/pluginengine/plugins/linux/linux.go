package linux

import (
	"fmt"
	"motadata-lite/src/pluginengine/client/SSHclient"
	"motadata-lite/src/pluginengine/utils"
	"motadata-lite/src/pluginengine/utils/constants"
	"strconv"
	"strings"
	"sync"
)

const (
	VENDOR = "system.vendor"

	SYSTEM_NAME = "system.os.name"

	SYSTEM_VERSION = "system.os.version"

	START_TIME = "started.time"

	START_TIME_SECOND = "started.time.seconds"

	SYSTEM_MODEL = "system.model"

	SYSTEM_PRODUCT = "system.product"

	INTERRUPT_PER_SECONDS = "system.interrupts.per.sec"

	SYSTEM_CPU_IO_PERCENT = "system.cpu.io.percent"

	SYSTEM_RUNNING_PROCESSES = "system.running.processes"

	SYSTEM_NETWORK_UDP_CONNECTIONS = "system.network.udp.connections"

	SYSTEM_NETWORK_TCP_CONNECTIONS = "system.network.tcp.connections"

	SYSTEM_NETWORK_TCP_RETRANSMISSIONS = "system.network.tcp.retransmissions"

	SYSTEM_NETWORK_ERROR_PACKETS = "system.network.error.packets"

	SYSTEM_NETWORK_OUT_BYTES_RATE = "system.network.out.bytes.rate"

	SYSTEM_MEMORY_TOTAL_BYTES = "system.memory.total.bytes"

	SYSTEM_MEMORY_AVAILABLE_BYTES = "system.memory.available.bytes"

	SYSTEM_CACHE_MEMORY_BYTES = "system.cache.memory.bytes"

	SYSTEM_SWAP_MEMORY_PROVISIONED = "system.swap.memory.provisioned.bytes"

	SYSTEM_SWAP_MEMORY_USED = "system.swap.memory.used.bytes"

	SYSTEM_SWAP_MEMORY_USED_PERCENT = "system.swap.memory.used.percent"

	SYSTEM_SWAP_MEMORY_FREE_PERCENT = "system.swap.memory.free.percent"

	SYSTEM_SWAP_MEMORY_FREE_BYTES = "system.swap.memory.free.bytes"

	SYSTEM_BUFFER_MEMORY_BYTES = "system.buffer.memory.bytes"

	SYSTEM_MEMORY_USED_BYTES = "system.memory.used.bytes"

	SYSTEM_MEMORY_FREE_BYTES = "system.memory.free.bytes"

	SYSTEM_MEMORY_FREE_PERCENT = "system.memory.free.percent"

	SYSTEM_MEMORY_USED_PERCENT = "system.memory.used.percent"

	SYSTEM_OVERALL_MEMORY_FREE_PERCENT = "system.overall.memory.free.percent"

	SYSTEM_OPENED_FILE_DESCRIPTORS = "system.opened.file.descriptors"

	SYSTEM_DISK_CAPACITY_BYTES = "system.disk.capacity.bytes"

	SYSTEM_DISK_FREE_BYTES = "system.disk.free.bytes"

	SYSTEM_DISK_FREE_PERCENT = "system.disk.free.percent"

	SYSTEM_DISK_USED_PERCENT = "system.disk.used.percent"

	SYSTEM_DISK_USED_BYTES = "system.disk.used.bytes"

	SYSTEM_DISK_IO_TIME_PERCENT = "system.disk.io.time.percent"

	SYSTEM_LOAD_AVG1_MIN = "system.load.avg1.min"

	SYSTEM_LOAD_AVG5_MIN = "system.load.avg5.min"

	SYSTEM_LOAD_AVG15_MIN = "system.load.avg15.min"

	SYSTEM_CPU_INTERRUPT_PERCENT = "system.cpu.interrupt.percent"

	SYSTEM_CPU_USER_PERCENT = "system.cpu.user.percent"

	SYSTEM_CPU_PERCENT = "system.cpu.percent"

	SYSTEM_CPU_KERNEL_PERCENT = "system.cpu.kernel.percent"

	SYSTEM_CPU_IDLE_PERCENT = "system.cpu.idle.percent"

	SYSTEM_CPU_TYPE = "system.cpu.type"

	SYSTEM_CPU_CORE = "system.cpu.core"

	SYSTEM_CONTEXT_SWITCHES_PER_SEC = "system.context.switches.per.sec"
)

var logger = utils.NewLogger("goEngine/plugin", "plugin")

func Discovery(context map[string]interface{}, wg *sync.WaitGroup) {

	defer wg.Done()

	errContexts := make([]map[string]interface{}, 0)

	context[constants.Status] = constants.StatusSuccess

	if len(errContexts) > 0 {
		context[constants.Status] = constants.StatusFail

		context[constants.Error] = errContexts
	}

	defer func() {
		if err := recover(); err != nil {

			logger.Error(fmt.Sprintf("%v", err))

			errContexts = append(errContexts, map[string]interface{}{

				constants.ErrorCode: constants.CommandReadError,

				constants.ErrorMessage: constants.CommandReadErrorMessage,

				constants.Error: err,
			})
		}
	}()

	client := SSHclient.Client{}

	for _, credential := range context[constants.Credentials].([]interface{}) {

		client.SetContext(context, credential.(map[string]interface{}))

		if err := client.Init(); err != nil {
			continue
		} else {
			context[constants.Result] = map[string]interface{}{constants.Ip: context[constants.Ip].(string)}

			context[constants.CredentialProfileID] = credential.(map[string]interface{})[constants.CredentialId].(float64)

			return
		}
	}

	context[constants.CredentialProfileID] = constants.InvalidCredentialCode

	return
}

func Collect(context map[string]interface{}, wg *sync.WaitGroup) {

	defer wg.Done()

	var err error

	client := SSHclient.Client{}

	errContexts := make([]map[string]interface{}, 0)

	context[constants.Status] = constants.StatusSuccess

	if len(errContexts) > 0 {
		context[constants.Status] = constants.StatusFail

		context[constants.Error] = errContexts
	}

	client.SetContext(context, context[constants.Credential].(map[string]interface{}))

	err = client.Init()

	if err != nil {

		errContexts = append(errContexts, map[string]interface{}{

			constants.ErrorCode: constants.SSHConnection,

			constants.ErrorMessage: constants.SSHConnectionMessage,

			constants.Error: err,
		})

		return
	}

	var command = "cat /sys/class/dmi/id/sys_vendor; uname -sr | awk {'print $1,$2'};uptime -s; date -d \"$(uptime -s)\" +%s; cat /sys/class/dmi/id/product_name;hostname;ps aux | awk {'print $8'} | grep -wc 'X';ps -eLf | wc -l;ps -e --no-headers | wc -l ;netstat -un | wc -l | awk '{print $1 - 2}';netstat -tn | wc -l | awk '{print $1 - 2}';cat /proc/net/snmp | grep -i 'TCP' | tail -n 1 | awk {'print $13'};ifconfig | awk '/errors/ {sum += $3} END {print sum}';ifconfig | awk '/TX packets/ {sum += $3} END {print sum}';top -bn1 | head -n 4 | tail -n 1 | awk {'print $4,$6,$10'};top -bn1 | head -n 5 | tail -n 1 | awk {'print $3,$5,$7'};free | head -n 2 | tail -n 1 | awk {'print $6'};free -b | head -n 2 | tail -n 1 | awk {'print $3,$4'};free | awk '/Mem:/ {printf(\"%.2f\\n\", ($3/$2) * 100)}';free | awk '/Mem:/ {printf(\"%.2f\\n\", ($4/($3+$4)) * 100)}';lsof | wc -l | awk '{print $1 - 2}';df --total | tail -n 1 | awk '{print $2,$4, ($4/$2)*100 \"%\"}';df --output=pcent | head -3 | tail -1 | awk {'print $NF'}; df --output=used / | awk 'NR==2 {print $1}';iostat |head -n 4 | tail -n -1 | awk '{print $4}';cat /proc/loadavg | awk {'print $1,$2,$3'};mpstat -I ALL | head -n 4 | tail -n 1 | awk {'print $NF'};top -bn1 | head -n 3 | tail -n 1 | awk {'print $2,$4'};mpstat | tail -n -1 | awk {'print $8'};mpstat |tail -n 1 |  awk {'print $7,$NF'};lscpu | head -n 1 | awk {'print $NF'};lscpu | head -n 5 | tail -n 1 | awk {'print $NF'};vmstat | tail -n 1 | awk {'print $12'}"

	queryOutput, err := client.ExecuteCommand(command)

	if err != nil {

		errContexts = append(errContexts, map[string]interface{}{

			constants.ErrorCode: constants.InvalidCommand,

			constants.ErrorMessage: constants.InvalidCommandMessage,

			constants.Error: err.Error(),
		})

		logger.Error(fmt.Sprintf("error in the command: %s", err.Error()))

		return

	}

	defer func(client *SSHclient.Client) {

		err := client.Close()

		if err != nil {
			logger.Error(fmt.Sprintf("error in closing ssh connection: %s", err.Error()))
		}
	}(&client)

	defer func() {
		if r := recover(); r != nil {
			errContexts = append(errContexts, map[string]interface{}{

				constants.ErrorCode: constants.CommandReadError,

				constants.ErrorMessage: constants.CommandReadErrorMessage,

				constants.Error: r,
			})

			logger.Error(fmt.Sprintf("error in the reading output lines: %s", r))
		}
		return
	}()

	var output = make(map[string]interface{})

	lines := strings.Split(string(queryOutput), "\n")

	ParseMetric(output, lines)

	context[constants.Result] = output

	return

}

func ParseMetric(output map[string]interface{}, data []string) {

	output[VENDOR] = data[0]

	osValues := strings.Split(data[1], " ")

	output[SYSTEM_NAME] = osValues[0]

	output[SYSTEM_VERSION] = osValues[1]

	output[START_TIME] = data[2]

	if startTimeSeconds, err := strconv.ParseFloat(data[3], 64); err != nil {
		output[START_TIME_SECOND] = startTimeSeconds
	}

	output[SYSTEM_MODEL] = data[4]

	output[SYSTEM_PRODUCT] = data[5]

	if interruptPerSeconds, err := strconv.ParseFloat(data[6], 64); err != nil {
		output[INTERRUPT_PER_SECONDS] = interruptPerSeconds
	}

	if systemCpuIoPercent, err := strconv.ParseFloat(data[7], 64); err == nil {
		output[SYSTEM_CPU_IO_PERCENT] = systemCpuIoPercent
	}

	if systemRunningProcesses, err := strconv.ParseFloat(data[8], 64); err == nil {
		output[SYSTEM_RUNNING_PROCESSES] = systemRunningProcesses
	}

	if systemNetworkUdpConnections, err := strconv.ParseFloat(data[9], 64); err == nil {
		output[SYSTEM_NETWORK_UDP_CONNECTIONS] = systemNetworkUdpConnections
	}

	if systemNetworkTcpConnections, err := strconv.ParseFloat(data[10], 64); err == nil {
		output[SYSTEM_NETWORK_TCP_CONNECTIONS] = systemNetworkTcpConnections
	}

	if systemNetworkTcpRetransmissions, err := strconv.ParseFloat(data[11], 64); err == nil {
		output[SYSTEM_NETWORK_TCP_RETRANSMISSIONS] = systemNetworkTcpRetransmissions
	}

	if systemNetworkErrorPackets, err := strconv.ParseFloat(data[12], 64); err == nil {
		output[SYSTEM_NETWORK_ERROR_PACKETS] = systemNetworkErrorPackets
	}

	if systemNetworkOutBytesRate, err := strconv.ParseFloat(data[13], 64); err == nil {
		output[SYSTEM_NETWORK_OUT_BYTES_RATE] = systemNetworkOutBytesRate
	}

	systemMemoryValue := strings.Split(data[14], " ")
	if systemMemoryTotalBytes, err := strconv.ParseFloat(systemMemoryValue[0], 64); err == nil {
		output[SYSTEM_MEMORY_TOTAL_BYTES] = systemMemoryTotalBytes
	}

	if systemMemoryAvailableBytes, err := strconv.ParseFloat(systemMemoryValue[1], 64); err == nil {
		output[SYSTEM_MEMORY_AVAILABLE_BYTES] = systemMemoryAvailableBytes
	}

	if systemCacheMemoryBytes, err := strconv.ParseFloat(systemMemoryValue[2], 64); err == nil {
		output[SYSTEM_CACHE_MEMORY_BYTES] = systemCacheMemoryBytes
	}

	swapMemoryValue := strings.Split(data[15], " ")
	if swapProvidedValue, err := strconv.ParseFloat(swapMemoryValue[0], 64); err == nil {
		output[SYSTEM_SWAP_MEMORY_PROVISIONED] = swapProvidedValue
	}

	if swapFreeValue, err := strconv.ParseFloat(swapMemoryValue[1], 64); err == nil {
		output[SYSTEM_SWAP_MEMORY_FREE_BYTES] = swapFreeValue
	}

	if swapUsedValue, err := strconv.ParseFloat(swapMemoryValue[2], 64); err == nil {
		output[SYSTEM_SWAP_MEMORY_USED] = swapUsedValue
	}

	output[SYSTEM_SWAP_MEMORY_USED_PERCENT] = (output[SYSTEM_SWAP_MEMORY_USED].(float64) / output[SYSTEM_SWAP_MEMORY_PROVISIONED].(float64)) * 100

	output[SYSTEM_SWAP_MEMORY_FREE_PERCENT] = (output[SYSTEM_SWAP_MEMORY_FREE_BYTES].(float64) / output[SYSTEM_SWAP_MEMORY_PROVISIONED].(float64)) * 100

	if systemBufferMemoryBytes, err := strconv.Atoi(data[16]); err == nil {
		output[SYSTEM_BUFFER_MEMORY_BYTES] = systemBufferMemoryBytes
	}

	systemMemoryValues := strings.Split(data[17], " ")
	if systemMemoryUsedBytes, err := strconv.Atoi(systemMemoryValues[0]); err == nil {
		output[SYSTEM_MEMORY_USED_BYTES] = systemMemoryUsedBytes
	}

	if systemMemoryFreeBytes, err := strconv.Atoi(systemMemoryValues[1]); err == nil {
		output[SYSTEM_MEMORY_FREE_BYTES] = systemMemoryFreeBytes
	}

	if output[SYSTEM_MEMORY_TOTAL_BYTES] != 0 {
		output[SYSTEM_MEMORY_FREE_PERCENT] = (output[SYSTEM_MEMORY_AVAILABLE_BYTES].(float64) / output[SYSTEM_MEMORY_TOTAL_BYTES].(float64)) * 100
	}

	if systemMemoryUsedPercent, err := strconv.ParseFloat(data[18], 64); err == nil {
		output[SYSTEM_MEMORY_USED_PERCENT] = systemMemoryUsedPercent
	}

	if systemOverallMemoryFreePercent, err := strconv.ParseFloat(data[19], 64); err == nil {
		output[SYSTEM_OVERALL_MEMORY_FREE_PERCENT] = systemOverallMemoryFreePercent
	}

	if systemOpenedFileDescriptors, err := strconv.ParseFloat(data[20], 64); err == nil {
		output[SYSTEM_OPENED_FILE_DESCRIPTORS] = systemOpenedFileDescriptors
	}

	systemDiskValue := strings.Split(data[21], " ")
	if systemDiskCapacityBytes, err := strconv.ParseFloat(systemDiskValue[0], 64); err == nil {
		output[SYSTEM_DISK_CAPACITY_BYTES] = systemDiskCapacityBytes
	}

	if systemDiskFreeBytes, err := strconv.ParseFloat(systemDiskValue[1], 64); err == nil {
		output[SYSTEM_DISK_FREE_BYTES] = systemDiskFreeBytes
	}

	if systemDiskFreePercent, err := strconv.ParseFloat(systemDiskValue[2], 64); err == nil {
		output[SYSTEM_DISK_FREE_PERCENT] = systemDiskFreePercent
	}

	if systemDiskUsedPercent, err := strconv.ParseFloat(data[22], 64); err == nil {
		output[SYSTEM_DISK_USED_PERCENT] = systemDiskUsedPercent
	}

	if systemDiskUsedBytes, err := strconv.ParseFloat(data[23], 64); err == nil {
		output[SYSTEM_DISK_USED_BYTES] = systemDiskUsedBytes
	}

	if systemDiskIoTimePercent, err := strconv.ParseFloat(data[24], 64); err == nil {
		output[SYSTEM_DISK_IO_TIME_PERCENT] = systemDiskIoTimePercent
	}

	loadAverageValue := strings.Split(data[25], " ")
	if systemLoadAvg1Min, err := strconv.ParseFloat(loadAverageValue[0][:len(loadAverageValue[0])-1], 64); err == nil {
		output[SYSTEM_LOAD_AVG1_MIN] = systemLoadAvg1Min
	}

	if systemLoadAvg5Min, err := strconv.ParseFloat(loadAverageValue[1][:len(loadAverageValue[1])-1], 64); err == nil {
		output[SYSTEM_LOAD_AVG5_MIN] = systemLoadAvg5Min
	}

	if systemLoadAvg15Min, err := strconv.ParseFloat(loadAverageValue[2], 64); err == nil {
		output[SYSTEM_LOAD_AVG15_MIN] = systemLoadAvg15Min
	}

	if interruptPerSeconds, err := strconv.ParseFloat(data[26], 64); err == nil {
		output[INTERRUPT_PER_SECONDS] = interruptPerSeconds
	}

	if output[INTERRUPT_PER_SECONDS] != 0 {
		output[SYSTEM_CPU_INTERRUPT_PERCENT] = (output[INTERRUPT_PER_SECONDS].(float64) / 3000000000) * 100
	}

	cpuPercentValue := strings.Split(data[27], " ")
	if systemCpuUserPercent, err := strconv.ParseFloat(cpuPercentValue[0], 64); err == nil {
		output[SYSTEM_CPU_USER_PERCENT] = systemCpuUserPercent
	}

	if systemCpuPercent, err := strconv.ParseFloat(cpuPercentValue[1], 64); err == nil {
		output[SYSTEM_CPU_PERCENT] = systemCpuPercent
	}

	if systemCpuIoPercent, err := strconv.ParseFloat(data[28], 64); err == nil {
		output[SYSTEM_CPU_IO_PERCENT] = systemCpuIoPercent
	}

	systemCpuValues := strings.Split(data[29], " ")
	if systemCpuKernelPercent, err := strconv.ParseFloat(systemCpuValues[0], 64); err == nil {
		output[SYSTEM_CPU_KERNEL_PERCENT] = systemCpuKernelPercent
	}

	if systemCpuIdlePercent, err := strconv.ParseFloat(systemCpuValues[1], 64); err == nil {
		output[SYSTEM_CPU_IDLE_PERCENT] = systemCpuIdlePercent
	}

	output[SYSTEM_CPU_TYPE] = data[30]

	if systemCpuCore, err := strconv.ParseFloat(data[31], 64); err == nil {
		output[SYSTEM_CPU_CORE] = systemCpuCore
	}

	if systemContextSwitchesPerSec, err := strconv.ParseFloat(data[32], 64); err == nil {
		output[SYSTEM_CONTEXT_SWITCHES_PER_SEC] = systemContextSwitchesPerSec
	}
}
