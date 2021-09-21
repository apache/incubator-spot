export interface Http {
    // http_request_method	string	HTTP method	GET, CONNECT, POST
    // http_request_uri	string	Requested URI	/wcm/assets/images/imagefileicon.gif
    // http_request_body_len	int	Length of request body	98
    // http_request_user_name	string	username from event	jsmith
    // http_request_password	string	Password from event	abc123
    // http_request_proxied	string	Proxy request label	"somestring"
    // http_request_headers	MAP	HTTP request headers	request_headers['HOST'] request_headers['USER-AGENT'] request_headers['ACCEPT']
    // http_response_status_code	int	HTTP response status code	404
    // http_response_status_msg	string	HTTP response status message	"Not found"
    // http_response_body_len	int	Length of response body	98
    // http_response_info_code	int	HTTP response info code	100
    // http_response_info_msg	string	HTTP response info message	"somestring"
    // http_response_resp_fuids	string	Response FUIDS	"somestring"
    // http_response_mime_types	string	Mime types	"cgi,bat,exe"
    // http_response_headers	MAP	Response headers	response_headers['SERVER'] response_headers['SET-COOKIE'] response_headers['DATE']
}
