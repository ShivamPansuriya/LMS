package linux

import (
	"strconv"
	"strings"
)

func ParseMetric(output map[string]interface{}, data []string) {
	output[VENDOR] = data[0]

	osValues := strings.Split(data[1], " ")

	output[SYSTEM_NAME] = osValues[0]

	output[SYSTEM_VERSION] = osValues[1]

	output[START_TIME] = data[2]

	output[START_TIME_SECOND], _ = strconv.ParseFloat(data[3], 64)

	output[SYSTEM_MODEL] = data[4]

	output[SYSTEM_PRODUCT] = data[5]

	output[INTERRUPT_PER_SECONDS], _ = strconv.ParseFloat(data[6], 64)

	output[SYSTEM_CPU_IO_PERCENT], _ = strconv.ParseFloat(data[7], 64)

	output[SYSTEM_RUNNING_PROCESSES], _ = strconv.ParseFloat(data[8], 64)

	output[SYSTEM_NETWORK_UDP_CONNECTIONS], _ = strconv.ParseFloat(data[9], 64)

	output[SYSTEM_NETWORK_TCP_CONNECTIONS], _ = strconv.ParseFloat(data[10], 64)

	output[SYSTEM_NETWORK_TCP_RETRANSMISSIONS], _ = strconv.ParseFloat(data[11], 64)

	output[SYSTEM_NETWORK_ERROR_PACKETS], _ = strconv.ParseFloat(data[12], 64)

	output[SYSTEM_NETWORK_OUT_BYTES_RATE], _ = strconv.ParseFloat(data[13], 64)

	systemMemoryValue := strings.Split(data[14], " ")

	output[SYSTEM_MEMORY_TOTAL_BYTES], _ = strconv.ParseFloat(systemMemoryValue[0], 64)

	output[SYSTEM_MEMORY_AVAILABLE_BYTES], _ = strconv.ParseFloat(systemMemoryValue[1], 64)

	output[SYSTEM_CACHE_MEMORY_BYTES], _ = strconv.ParseFloat(systemMemoryValue[2], 64)

	swapMemoryValue := strings.Split(data[15], " ")

	swapProvidedValue, _ := strconv.ParseFloat(swapMemoryValue[0], 64)

	output[SYSTEM_SWAP_MEMORY_PROVISIONED] = swapProvidedValue

	swapFreeValue, _ := strconv.ParseFloat(swapMemoryValue[1], 64)

	output[SYSTEM_SWAP_MEMORY_FREE_BYTES] = swapFreeValue

	swapUsedValue, _ := strconv.ParseFloat(swapMemoryValue[2], 64)

	output[SYSTEM_SWAP_MEMORY_USED] = swapUsedValue

	output[SYSTEM_SWAP_MEMORY_USED_PERCENT] = (swapUsedValue / swapProvidedValue) * 100

	output[SYSTEM_SWAP_MEMORY_FREE_PERCENT] = (swapFreeValue / swapProvidedValue) * 100

	output[SYSTEM_BUFFER_MEMORY_BYTES], _ = strconv.Atoi(data[16])

	systemMemoryValues := strings.Split(data[17], " ")

	output[SYSTEM_MEMORY_USED_BYTES], _ = strconv.Atoi(systemMemoryValues[0])

	output[SYSTEM_MEMORY_FREE_BYTES], _ = strconv.Atoi(systemMemoryValues[1])

	output[SYSTEM_MEMORY_FREE_PERCENT] = (output[SYSTEM_MEMORY_AVAILABLE_BYTES].(float64) / output[SYSTEM_MEMORY_TOTAL_BYTES].(float64)) * 100

	output[SYSTEM_MEMORY_USED_PERCENT], _ = strconv.ParseFloat(data[18], 64)

	output[SYSTEM_OVERALL_MEMORY_FREE_PERCENT], _ = strconv.ParseFloat(data[19], 64)

	output[SYSTEM_OPENED_FILE_DESCRIPTORS], _ = strconv.ParseFloat(data[20], 64)

	systemDiskValue := strings.Split(data[21], " ")

	output[SYSTEM_DISK_CAPACITY_BYTES], _ = strconv.ParseFloat(systemDiskValue[0], 64)

	output[SYSTEM_DISK_FREE_BYTES], _ = strconv.ParseFloat(systemDiskValue[1], 64)

	output[SYSTEM_DISK_FREE_PERCENT], _ = strconv.ParseFloat(systemDiskValue[2], 64)

	output[SYSTEM_DISK_USED_PERCENT], _ = strconv.ParseFloat(data[22], 64)

	output[SYSTEM_DISK_USED_BYTES], _ = strconv.ParseFloat(data[23], 64)

	output[SYSTEM_DISK_IO_TIME_PERCENT], _ = strconv.ParseFloat(data[24], 64)

	loadAverageValue := strings.Split(data[25], " ")

	output[SYSTEM_LOAD_AVG1_MIN], _ = strconv.ParseFloat(loadAverageValue[0][:len(loadAverageValue[0])-1], 64)

	output[SYSTEM_LOAD_AVG5_MIN], _ = strconv.ParseFloat(loadAverageValue[0][:len(loadAverageValue[1])-1], 64)

	output[SYSTEM_LOAD_AVG15_MIN], _ = strconv.ParseFloat(loadAverageValue[0][:len(loadAverageValue[2])], 64)

	output[INTERRUPT_PER_SECONDS], _ = strconv.ParseFloat(data[26], 64)

	output[SYSTEM_CPU_INTERRUPT_PERCENT] = (output[INTERRUPT_PER_SECONDS].(float64) / 3000000000) * 100

	cpuPercentValue := strings.Split(data[27], " ")

	output[SYSTEM_CPU_USER_PERCENT], _ = strconv.ParseFloat(cpuPercentValue[0], 64)

	output[SYSTEM_CPU_PERCENT], _ = strconv.ParseFloat(cpuPercentValue[1], 64)

	output[SYSTEM_CPU_IO_PERCENT], _ = strconv.ParseFloat(data[28], 64)

	systemCpuValues := strings.Split(data[29], " ")

	output[SYSTEM_CPU_KERNEL_PERCENT], _ = strconv.ParseFloat(systemCpuValues[0], 64)

	output[SYSTEM_CPU_IDLE_PERCENT], _ = strconv.ParseFloat(systemCpuValues[1], 64)

	output[SYSTEM_CPU_TYPE] = data[30]

	output[SYSTEM_CPU_CORE], _ = strconv.ParseFloat(data[31], 64)

	output[SYSTEM_CONTEXT_SWITCHES_PER_SEC], _ = strconv.ParseFloat(data[32], 64)
}
