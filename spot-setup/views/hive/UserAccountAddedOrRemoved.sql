SET hiveconf:dbname;

CREATE OR REPLACE VIEW ${hiveconf:dbname}.UserAccountAddedOrRemoved_windows AS
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
    "UserAccount"       AS EventLogType,
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
    CASE
        WHEN event_id IN (4720) THEN "Account Created"
        WHEN event_id IN (4726) THEN "Account Deleted"
        ELSE "Unknown Action"
    END                 AS Action,
    newtarget_username  AS UserName,
    ""                  AS UserID,
    ""                  AS UserType,
    ""                  AS UserCreateTime,
    ""                  AS UserModifiedTime,
    ""                  AS UserDomain,
    ""                  AS UserPermissions,
    ""                  AS UserGroups,
    year,
    month,
    day

FROM
    ${hiveconf:dbname}.windows_view_partition
WHERE
    event_id IN (4720, 4726);

CREATE OR REPLACE VIEW ${hiveconf:dbname}.UserAccountAddedOrRemoved
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
  Action,
  UserName,
  UserID,
  UserType,
  UserCreateTime,
  UserModifiedTime,
  UserDomain,
  UserPermissions,
  UserGroups,
  year,
  month,
  day
) AS
SELECT * FROM ${hiveconf:dbname}.UserAccountAddedOrRemoved_windows;
