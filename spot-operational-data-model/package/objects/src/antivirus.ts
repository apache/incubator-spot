import {Application} from "./application";
import {Signature} from "./signature";

export interface Antivirus {
    riskName: String;
    actualAction: String;
    requestedAction: String;
    secondaryAction: String;
    downloadSite: String;
    downloadedBy: String;
    trackingStatus: String;
    firstSeen: BigInteger;
    application: Application
    categorySet: String;
    categoryType: String;
    threatCount: Number;
    infectedCount: Number;
    omittedCount: Number;
    scanId: Number;
    startMessage: String;
    stopMessage: String;
    totalFiles: Number;
    signature: Signature;
    intrusionUrl: String;
    intrusionPayloadUrl: String;
    objectName: String;
}
