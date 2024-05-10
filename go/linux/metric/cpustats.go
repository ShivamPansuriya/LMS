package metric

import (
	"fmt"
	"strconv"
	"strings"
)

func CpuQuery() string {
	return "top -bn1 | head -n 1 | awk {'print $10,$11,$NF'};mpstat -I ALL | head -n 4 | tail -n 1 | awk {'print $NF'};top -bn1 | head -n 3 | tail -n 1 | awk {'print $2,$4'};mpstat | tail -n -1 | awk {'print $8'};mpstat |tail -n 1 |  awk {'print $7,$NF'};lscpu | head -n 1 | awk {'print $NF'};lscpu | head -n 5 | tail -n 1 | awk {'print $NF'};vmstat | tail -n 1 | awk {'print $12'}"
}

func CpuMetrixFormatter(lines []string) map[string]interface{} {

	var output = make(map[string]interface{})

	defer func() {
		if r := recover(); r != nil {
			fmt.Println("error in reading output lines", r)
		}

		output = map[string]interface{}{}

		return
	}()

	loadAverageValue := strings.Split(lines[0], " ")

	output["system.load.avg1.min"], _ = strconv.ParseFloat(loadAverageValue[0][:len(loadAverageValue[0])-1], 64)

	output["system.load.avg5.min"], _ = strconv.ParseFloat(loadAverageValue[0][:len(loadAverageValue[1])-1], 64)

	output["system.load.avg15.min"], _ = strconv.ParseFloat(loadAverageValue[0][:len(loadAverageValue[2])-1], 64)

	output["system.interrupts.per.sec"], _ = strconv.ParseFloat(lines[1], 64)

	output["system.cpu.interrupt.percent"] = float64((output["system.interrupts.per.sec"].(float64) / 3000000000) * 100)

	cpuPercentValue := strings.Split(lines[2], " ")

	output["system.cpu.user.percent"], _ = strconv.ParseFloat(cpuPercentValue[0], 64)

	output["system.cpu.percent"], _ = strconv.ParseFloat(cpuPercentValue[1], 64)

	output["system.cpu.io.percent"], _ = strconv.ParseFloat(lines[3], 64)

	systemCpuValues := strings.Split(lines[4], " ")

	output["system.cpu.kernel.percent"], _ = strconv.ParseFloat(systemCpuValues[0], 64)

	output["system.cpu.idle.percent"], _ = strconv.ParseFloat(systemCpuValues[1], 64)

	output["system.cpu.type"] = lines[5]

	output["system.cpu.core"], _ = strconv.ParseFloat(lines[6], 64)

	output["system.context.switches.per.sec"], _ = strconv.ParseFloat(lines[7], 64)

	return output
}

//system load avg1 min, system load avg5 min, system.load.avg15.min = top -bn1 | head -n 1 | awk {'print $10,$11,$NF'} (there will be extra comma)
//system.interrupts.per.sec = mpstat -I ALL | head -n 4 | tail -n 1 | awk {'print $NF'}	lines[1]
//system.cpu.interrupt.percent = [(system.interrupts.per.sec / 3000000000) * 100]
//system.cpu.user.percent,system.cpu.percent = top -bn1 | head -n 3 | tail -n 1 | awk {'print $2,$4'}		line[2]
//system.cpu.io.percent = mpstat | tail -n -1 | awk {'print $8'}  	line[3]
//system.cpu.kernel.percent, system.cpu.idle.percent = mpstat |tail -n 1 |  awk {'print $7,$NF'}		line[4]
//system.cpu.type = lscpu | head -n 1 | awk {'print $NF'} ?????????????????????????			line[5]
//system.cpu.cores = lscpu | head -n 5 | tail -n 1 | awk {'print $NF'}				line[6]
//system.context.switches.per.sec = vmstat | tail -n 1 | awk {'print $12'}			lines[7]
