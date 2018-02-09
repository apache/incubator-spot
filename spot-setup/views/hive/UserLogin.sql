SET hiveconf:dbname;

CREATE OR REPLACE VIEW ${hiveconf:dbname}.UserLogin_windows AS
SELECT
    event_time              AS TimeOfEvent,
    ""                      AS TimeReceived,
    ""                      AS DeviceReceived,
    ""                      AS TimeLoaded,
    ""                      AS SyslogFacility,
    ""                      AS SyslogPriority,
    "Microsoft"             AS ReportingVendor,
    "Windows"               AS ReportingProduct,
    "2008+"                 AS ReportingVersion,
    event_id                AS EventID,
    event_description       AS EventName,
    "Login"                 AS EventLogType,
    target_logonid          AS SequenceID,
    ""                      AS Severity,
    subject_username        AS SourceUserID,
    workstation_name        AS SourceHost,
    ip_address              AS SourceIP,
    ip_port                 AS SourcePort,
    ""                      AS SourceMac,
    subject_domainname      AS SourceDomain,
    ""                      AS ReportingHost,
    ""                      AS ReportingIP,
    ""                      AS ReportingPort,
    ""                      AS ReportingMac,
    CASE
        WHEN event_id IN (4624) THEN newtarget_username
        ELSE target_username
    END                     AS DestinationUserID,
    ""                      AS DestinationHost,
    ""                      AS DestinationIP,
    ""                      AS DestinationPort,
    ""                      AS DestinationMac,
    target_domainname       AS DestinationDomain,
    year,
    month,
    day
FROM
    ${hiveconf:dbname}.windows_view_partition
WHERE event_id IN (
                     4624,
                     4625,
                     4634,
                     4647,
                     4648);



CREATE OR REPLACE VIEW ${hiveconf:dbname}.UserLogin
(
    TimeOfEvent,
    TimeReceived,
    DeviceReceived,
    TimeLoaded,
    SyslogFacility,
    SyslogPriority,
    ReportingVendor,
    ReportingProduct,
    ReportingVersion,
    EventID,
    EventName,
    EventLogType,
    SequenceID,
    Severity,
    SourceUserID,
    SourceHost,
    SourceIP,
    SourcePort,
    SourceMac,
    SourceDomain,
    ReportingHost,
    ReportingIP,
    ReportingPort,
    ReportingMac,
    DestinationUserID,
    DestinationHost,
    DestinationIP,
    DestinationPort,
    DestinationMac,
    DestinationDomain,
    year,
    month,
    day
) AS
SELECT * from ${hiveconf:dbname}.UserLogin_windows;

