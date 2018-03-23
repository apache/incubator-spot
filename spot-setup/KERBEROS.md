## Kerberos support installation

run the following in addition to the typical installation instructions

### Spot-Ingest

`pip install -r ./spot-ingest/kerberos-requirements.txt`

### Spot-OA

`pip install -r ./spot-oa/kerberos-requirements.txt`


## spot.conf

KERBEROS       =  set `KERBEROS='true'` in /etc/spot.conf to enable kerberos
KEYTAB         =  should be generated using `ktutil` or another approved method
SASL_MECH      =  should be set to `sasl_plaintext` unless using ssl
KAFKA_SERVICE  =  if not set defaults will be used

SSL            =  enable ssl by setting to true
SSL_VERIFY     =  by setting to `false` disables host checking **important** only recommended in non production environments
CA_LOCATION    =  location of certificate authority file
CERT           =  host certificate
KEY            =  key required for host certificate

sample below:

```
#kerberos config
KERBEROS='true'
KINIT=/usr/bin/kinit
PRINCIPAL='spot'
KEYTAB='/opt/security/spot.keytab'
SASL_MECH='GSSAPI'
SECURITY_PROTO='sasl_plaintext'
KAFKA_SERVICE_NAME=''

#ssl config
SSL='false'
SSL_VERIFY='true'
CA_LOCATION=''
CERT=''
KEY=''

```

Please see [LIBRDKAFKA Configurations](https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md)
for reference to additional settings that can be set by modifying `spot-ingest/common/kafka_client.py`

