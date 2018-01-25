### Reputation
This section describes the functionality of the current reputation service modules and how you can implement your own. 

 It's possible to add new reputation services by implementing a new sub-module, to do that developers should follow
 these steps:

1. Map the responses of the new reputation service, according to this reputation table.

    | Key | Value |
    |-----|-------|
    |UNVERIFIED|-1|
    |NONE      |0 |
    |LOW       |1 |
    |MEDIUM    |2 |
    |HIGH      |3 |

2. Add a new key for the new reputation service in gti_config.json.

        { 
			"gti" : { …
			},
			"fb" : {…
			},
			"mynewreputationservice":{ "server" : "rep.server.com",
				                        "user" : "user-name"
			}
		}
3. Create file structure for new sub-module.

        [solution-user@edge-server]$ cd ~/spot-oa/components/reputation/
        [solution-user@edge-server]$ mkdir mynewreputationservice
        [solution-user@edge-server]$ cd mynewreputationservice

4. Create an empty _ _init_ _.py file.
5. Add a new file *reputation.py*. Each sub-module should contain a reputation.py file.
6. Write your code in reputation.py. The code should contain the follow structure:

    6.1 Constructor:

    Constructor should receive one *config* parameter. This parameter correspond to the specific configuration of the
    service in gti_config.json. When running, dns_oa.py will iterate through each service in the configuration file
    and create a new instance of each sub-module sending the corresponding configuration for each new instance.

        def __init__(sel,conf):
            #TODO: read configuration.
            # i.e.
            # self._server = configuration['sever']
            # self._user = configuration['user']

    6.2 Implement *check* method:

    Check method should receive a list of urls or IPs to be evaluated and return a dictionary with each element's
    reputation in the following format {"url":"reputation"}.
    *Reputation* should be a string with 3 elements separated by colon **":"** where the first part is the reputation
    service name, second the reputation label and third the reputation value already defined in step 1.

        def check(self,url_list):
            # connect to service
            # call service for each url in list or bulk query
            # translate results to service:label:value format
            # create new entry to result dictionary {"url":"service:label:value"}
            # return a dictionary with each url from url_list and the corresponding reputation

     Results example:

        {
            "dns.somethin.com" : "mynewreputationservice:MEDIUM:2",
			"other.dns.test.org" : "mynewreputationservice:LOW:1",
			"someother.test.com" : "mynewreputationservice:HIGH:3"
		}
