export interface Ssh {
    version: String;
    auth: Boolean;
    client: String;
    server: String;
    cipherAlgorithm: String;
    macAlgorithm: String;
    CompressionAlgorithm: String;
    keyExchangeAlgorithm: String;
    hostKeyAlgorithm: String;
}
