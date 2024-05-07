package main

/*
system vendor = sudo dmidecode -s system-manufacturer
system blocked processes = ps aux | awk {'print $8'} | grep -wc 'X'
system opened file descriptors = lsof | wc -l (output -1)
system.cpu.type = lscpu | head -n 1 | awk {'print $NF'} ?????????????????????????
system.cpu.cores = lscpu | head -n 5 | tail -n 1 | awk {'print $NF'}
system.os.name, system.os.version = uname -sr | awk {'print $1,$2'}
system.disk.capacity.bytes, system.disk.free.bytes = df --total | tail -n 1 | awk {'print $2,$4'}
system.disk.free.percent = system.disk.free.bytes/system.disk.capacity.bytes * 100
system.disk.io.ops.per.sec =
started.time= uptime -s
started.time.seconds = date -d "$(uptime -s)" +%s
system.disk.io.bytes.per.sec =
system.disk.io.queue.length = cat /proc/diskstats | grep 'sda'
system.memory.installed.bytes, system.overall.memory.used.bytes,system.overall.memory.free.bytes = free -b | grep -i 'mem' | awk {'print $2,$3,$NF'}
system.overall.memory.used.percent = system.overall.memory.used.bytes/system.memory.installed.bytes * 100
system.model = cat /sys/class/dmi/id/product_name
system.processor.queue.length =
system.disk.used.percent = df --output=pcent /
system.threads = ps -eLf | wc -l
system.name = hostname
system.disk.used.bytes = df --output=used / | awk 'NR==2 {print $1}'

*/
