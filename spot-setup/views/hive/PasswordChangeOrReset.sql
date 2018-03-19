SET hiveconf:dbname;

CREATE OR REPLACE VIEW ${hiveconf:dbname}.PasswordChangeOrReset_windows AS
SELECT
    event_time               AS TimeOfEvent,
    "Microsoft"              AS ReportingVendor,
    "Windows"                AS ReportingProduct,
    "2008+"                  AS ReportingVersion,
    ""                       AS ReportingHost,
    event_id                 AS EventID,
    event_description        AS EventName,
    "PasswordChangeOrReset"  AS EventLogType,
    subject_username         AS SourceUserID,
    subject_domainname       AS SourceDomain,
    target_username          AS DestinationUserID,
    target_domainname        AS DestinationDomain,
    year,
    month,
    day
FROM
    ${hiveconf:dbname}.windows_view_partition
WHERE
    event_id IN (4723, 4724, 4794);


CREATE OR REPLACE VIEW ${hiveconf:dbname}.PasswordChangeOrReset
(
    TimeOfEvent,
    ReportingVendor,
    ReportingProduct,
    ReportingVersion,
    ReportingHost,
    EventID,
    EventName,
    EventLogType,
    SourceUserID,
    SourceDomain,
    DestinationUserID,
    DestinationDomain,
    year,
    month,
    day
) AS
SELECT * FROM ${hiveconf:dbname}.PasswordChangeOrReset_windows;

