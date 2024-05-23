package constants

const (
	RequestType = "request.type"

	Discovery = "discovery"

	Collect = "collect"

	Port = "port"

	Ip = "ip"

	Username = "username"

	Password = "password"

	TimeOut = "object.timeout"

	HostIP = "host.ip"

	HostPort = "host.port"

	Status = "status"

	StatusFail = "fail"

	StatusSuccess = "success"

	Error = "error"

	ErrorCode = "error.code"

	ErrorMessage = "error.message"

	Result = "result"

	InvalidCredentialCode = -1

	Credentials = "credentials"

	Credential = "credential"

	CredentialProfileID = "credential.profile.id"

	CredentialId = "credential.id"

	DeviceType = "device.type"

	LinuxDevice = "linux"

	InvalidArgumentCode = 1

	InvalidArgumentMessage = "not valid arguments"

	EncodeError = 2

	EncodeErrorMessage = "not valid encode request"

	UnmarshalError = 3

	UnmarshalMessage = "unable to convert string to json map"

	CommandReadError = 4

	CommandReadErrorMessage = "error in the reading output lines"

	SSHConnection = 5

	SSHConnectionMessage = "Cannot establish connection to host"

	InvalidCommand = 6

	InvalidCommandMessage = "error in the command"

	Agent = "agent"
)
