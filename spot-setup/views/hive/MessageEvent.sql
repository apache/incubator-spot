SET hiveconf:dbname;

CREATE OR REPLACE VIEW ${hiveconf:dbname}.MessageEvent_msexchange AS
SELECT
    event_time          AS TimeOfEvent,
    NULL                AS TimeReceived,
    NULL                AS DeviceReceive,
    NULL                AS TimeLoaded,
    NULL                AS SyslogFacility,
    NULL                AS SyslogPriority,
    "Microsoft"         AS ReportingVendor,
    "Exchange"          AS ReportingProduct,
    "*"                 AS ReportingVersion,
    source_ip           AS ClientIPAddress,
    source_host         AS ClientHostname,
    NULL                AS PartnerName,
    destination_host    AS ServerHostname,
    destination_ip      AS ServerIPAddress,
    receiver            AS RecipientEmailAddresses,
    event_name          AS EventID,
    event_name          AS EventName,
    "Message"           AS EventLogType,
    NULL                AS MessageID,
    NULL                AS RecipientReportStatus,
    email_size          AS TotalBytes,
    no_of_receivers     AS NumberOfRecipients,
    NULL                AS OriginationTime,
    NULL                AS Encryption,
    NULL                AS ServiceVersion,
    NULL                AS LinkedMessageID,
    subject             AS MessageSubject,
    sender              AS SenderEmailAddress,
    no_of_attachments   AS NumberOfAttachments,
    year,
    month,
    day
FROM ${hiveconf:dbname}.email_view_partition
WHERE event_name NOT IN ('NOTIFYMAPI');



CREATE OR REPLACE VIEW ${hiveconf:dbname}.MessageEvent
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
    ClientIPAddress,
    ClientHostname,
    PartnerName,
    ServerHostname,
    ServerIPAddress,
    RecipientEmailAddresses,
    EventID,
    EventName,
    EventLogType,
    MessageID,
    RecipientReportStatus,
    TotalBytes,
    NumberOfRecipients,
    OriginationTime,
    Encryption,
    ServiceVersion,
    LinkedMessageID,
    MessageSubject,
    SenderEmailAddress,
    NumberOfAttachments,
    year,
    month,
    day
) AS
SELECT * FROM ${hiveconf:dbname}.MessageEvent_msexchange;
