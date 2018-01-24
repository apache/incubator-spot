SET hiveconf:dbname;

CREATE OR REPLACE VIEW ${hiveconf:dbname}.ProcessStartupOrShutdown_windows AS
SELECT
    event_time          AS TimeOfEvent,
    ""                  AS TimeReceived,
    ""                  AS DeviceReceived,
    ""                  AS TimeLoaded,
    ""                  AS SyslogFacility,
    ""                  AS SyslogPriority,
    "Microsoft"         AS ReportingVendor,
    "Windows"           AS ReportingProduct,
    "2008+"             AS ReportingVersion,
    event_id            AS EventID,
    event_description   AS EventName,
    "Process"           AS EventLogType,
    subject_logonid     AS SequenceID,
    ""                  AS Severity,
    subject_username    AS SourceUserID,
    ""                  AS SourceHost,
    ip_address          AS SourceIP,
    ip_port             AS SourcePort,
    ""                  AS SourceMac,
    subject_domainname  AS SourceDomain,
    ""                  AS ReportingHost,
    ""                  AS ReportingIP,
    ""                  AS ReportingPort,
    ""                  AS ReportingMac,
    target_username     AS DestinationUserID,
    ""                  AS DestinationHost,
    ""                  AS DestinationIP,
    ""                  AS DestinationPort,
    ""                  AS DestinationMac,
    target_domainname   AS DestinationDomain,
    process_name        AS ProcessName,
    process_id          AS ProcessID,
    CASE
        WHEN event_id IN (4608, 4688, 4709, 4878, 4880, 5024, 5033, 5120, 5478) THEN "Started"
        WHEN event_id IN (4609, 4689, 4710, 4879, 4881, 5025, 5034, 5121, 5479) THEN "Shutdown"
    END                 AS Action,
    year,
    month,
    day
FROM ${hiveconf:dbname}.windows_view_partition
WHERE event_id IN (4608, 4609, 4688, 4689, 4709, 4710, 5024, 5025, 5033, 5034, 5120, 5121, 5778, 5779);


CREATE OR REPLACE VIEW ${hiveconf:dbname}.ProcessStartupOrShutdown
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
    ProcessName,
    ProcessId,
    Action,
    year,
    month,
    day
) AS
SELECT * FROM ${hiveconf:dbname}.ProcessStartupOrShutdown_windows;
