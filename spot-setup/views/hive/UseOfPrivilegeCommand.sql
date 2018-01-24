SET hiveconf:dbname;

CREATE OR REPLACE VIEW ${hiveconf:dbname}.UseOfPrivilegeCommand_windows AS
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
    "Privilege"         AS EventLogType,
    ""                  AS SequenceID,
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
    privilege_list      AS Command,
    ""                  AS CommandType,
    ""                  AS CommandParameters,
    status              AS CommandResult,
    year,
    month,
    day
FROM
    ${hiveconf:dbname}.windows_view_partition
WHERE
    event_id IN (4672, 4673);



CREATE OR REPLACE VIEW ${hiveconf:dbname}.UseOfPrivilegeCommand
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
  Command,
  CommandType,
  CommandParameters,
  CommandResult,
  year,
  month,
  day
) AS
SELECT * FROM ${hiveconf:dbname}.UseOfPrivilegeCommand_windows;
