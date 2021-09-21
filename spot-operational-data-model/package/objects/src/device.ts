export interface Device {
    // dvc_time	long	UTC timestamp from device where event/alert originates or is received	1472653952
    // dvc_ip4/dvc_ip6	long	IP address of device	Integer representation of 10.1.1.1
    // dvc_group	string	Device group label	"somestring"
    // dvc_server	string	Server label	"somestring"
    // dvc_host	string	Hostname of device	Integer representation of 10.1.1.1
    // dvc_domain	string	Domain of dvc	"somestring"
    // dvc_type	string	Device type that generated the log	Unix, Windows, Sonicwall
    // dvc_vendor	string	Vendor	Microsoft, Fireeye
    // dvc_fwd_ip4/fwd_ip6	long	Forwarded from device	Integer representation of 10.1.1.1
    // dvc_version	string	Version	"3.2.2"
}
