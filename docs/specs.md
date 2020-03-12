# Specifications

The networking specifications will be found here. You may also follow the **official** supported languages (as of 3/12/2020) which are the following

- Java (Main)
- Dart/Flutter (Secondary)

## Schema

- The JSON data is expected to be encoded as UTF-8

Packets are sent using JSON and a JSON wrapper.
The packet schema is as follows:

### Unencrypted data

```json5
{
  "ENCRYPT": false, // Used to check if the jsonObject field is encrypted
  "jsonObject": "", // The packet object as a JSON string
  "packetIdentifier": "PACKET_IDENTIFIER_FOR_PARSING", // When parsing the packet, this field is used to identify what class to use when parsing. Should use the same identifier for both server and client
  "packetId": 0 // Packet ID for checking if a newer packet of the same type has been received. Useful for games.
}
```

### Encrypted data

```json5
{
  "ENCRYPT": true, // Used to check if the jsonObject field is encrypted
  "jsonObject": "{
      \"data\":[ENCRYPTED_DATA_AS_BYTE_ARRAY], // The packet object as a JSON string and encrypted as a byte array.
      \"params\":[ENCRYPTION_PARAMETERS], // The AES key parameters used for encryption
      \"paramAlgorithm\":\"AES\" // The key algorithm, in case you are using a different key
      }",
  "packetIdentifier": "PACKET_IDENTIFIER_FOR_PARSING",
  "packetId": 0 // Packet ID for checking if a newer packet of the same type has been received. Useful for games.
}
```

Java code used:

```java

public class PacketWrapper<T> implements Serializable {

    protected static Gson gson = new Gson();

    protected boolean ENCRYPT = false;

    @Getter
    private String jsonObject;

    @Getter
    private transient T jsonObjectInstance;

    @Getter
    private String packetIdentifier;

    /**
     * For packet order
     */
    @Getter
    private int packetId;

    public PacketWrapper(T object, String packetIdentifier, int packetId) {
        this.packetIdentifier = packetIdentifier;
        this.jsonObjectInstance = object;
        this.jsonObject = gson.toJson(object);
        this.packetId = packetId;
    }

    public boolean encrypt() {
        return ENCRYPT;
    }



    protected PacketWrapper() {}
}

/**
 * Wraps a packet not meant to be encrypted
 */
public class UnencryptedPacketWrapper extends PacketWrapper<AcceptablePacketTypes> implements AcceptablePacketTypes {

    protected UnencryptedPacketWrapper() {}

    public UnencryptedPacketWrapper(Packet jsonObject, int packetId) {
        super(jsonObject, jsonObject.getPacketName(), packetId);

        if (PacketRegistry.checkIfRegistered(jsonObject) == PacketRegistry.RegisteredReturnValues.NOT_IN_REGISTRY) {
            throw new IllegalArgumentException("The packet trying to be wrapped is not registered. \"" + jsonObject.getClass() + "\"");
        }

        ENCRYPT = false;
    }


}

@RequiredArgsConstructor
@Getter
public class EncryptedBytes {

    private final byte[] data;
    private final byte[] params;
    private final String paramAlgorithm;

}

public class EncryptedPacketWrapper extends PacketWrapper<EncryptedBytes> {

    public EncryptedPacketWrapper(EncryptedBytes encryptedBytes, Packet packet, int packetId) {
        super(encryptedBytes, packet.getPacketName(), packetId);
        ENCRYPT = true;
    }


}
```

A real-world example of this is as follows

```json5
{
  "ENCRYPT": false,
  "jsonObject": "{\"publicKey\":\"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAty110pSGwvJyzzK6P0mx6Qoc96VkQiXrSmEMfWpXbN+qBu+3W6fTR5N2MrtFlB3LI5xhagHi662k3RHL+rPbQBqcivE6pAa2O3GrDKIUL+3KCVQQ553va7aLgEQy9H4gZnKTk7nyf5y6uKuJQj6lg8PxWd2pCUMqWOa5JGGvX2LYFUUEKq7vR1bZHAoVwC2i15JBJP8VhlsqZ2brFU1kRzHLlEUH+bwIve6Auvyuw947kxQOnA/KGQUlgdTWXKwtPq2dFr3cteLLrBboYH0ODp7gLnhO4Mp5M4u9VAd6zWorbK5dBNgELPDAViznHMpN2eCbzk8+O2q/qACT2z+gm3kj7s1uPhJ7wuUOgAWYUVrXihdqXhfa3x33r78EfZdT4vh/iTdT9wSMlRv1QlsproOwe44+BlD/F8DLzUi2fheK4seqftPvQepd8MlMLPAI7HoT7NmT0Gz6ozXdDZG0WSXQFMaKEyxtxsT2OiSRNBsr09VGH+eVx4UNb5SEUhNHoPn93qs1w8d4aOUqJsqJ5Fxna+RLY9URoD2FfP0RwIC7S1lfSWfErQTMEepxaMIliR1s/7TQn4g9RiUhgb08dTOAtMOUWaLagEiulBrLXH3VNX1bUHYdraYXHpdt2b6enlujw0GjJOxWrRBHZYiFSdLL/4P6gfaai82dkeXW7dECAwEAAQ\\u003d\\u003d\",\"versionData\":{\"version\":\"1.5.3\",\"minVersion\":\"1.5.3\"}}",
  "packetIdentifier": "INITIAL_HANDSHAKE_PACKET",
  "packetId": 0
}
```

## Initializing the connection

Make sure that the [important notice has been achknowledged](#important-notice)

When the connection has been established, the server will generate a 4096 RSA key pair, preferrably a unique one for each client. Then, the server will send an [INITIAL_HANDSHAKE_PACKET](#initial-handshake-packet)

Once the client receives this data,
the client should check if the version is the same as the client or if the minimum version is supported by the client. If not, the usual procedure is to warn the user of the version mismatch and to expect issues. If the versions do match, the client should generate an AES 256-bit key and then send a [KEY_RESPONSE_PACKET](#key-response-packet) with the key encrypted by the public RSA key in a byte array form.

Once the server receives the AES 256-bit key encrypted by the RSA public key, the server should decrypt the key and store it on the client's data class. Now the RSA key pairs are no longer needed and can be discarded from memory. The server will then send an empty packet named "REQUEST_CONNECT_INFO_PACKET" to request the client's data. The client will send a [CONNECTED_PACKET](#connected-packet)

Once the server receives the packet, the server can ask for password (use the [SELF_MESSAGE_PACKET](#self-message-packet) with **FILL_PASSWORD**) or kick for an abusive name etc. This is the last stage of the handshake process

## Special primitive packets

### IMPORTANT NOTICE

- When describing packet data, it is expected to be wrapped around the [packet wrapper schema as shown here](#schema)

These packets are _special_ or _primitive_ packets used for specific needs.
They can be sent by the networking API or the application using it.

### Ping packet (3 packets)

The ping packets [[PING_PACKET, SERVER], [PONG_PACKET, CLIENT], [PING_RECEIVE, SERVER]] (in their respectful order) are empty packets that are used to call events to measure ping. The server may send a PING_PACKET or the client may send a PONG_PACKET to start the ping-pong handshake.

All that is required is to use the names provided above and the correct order.

Order:

1. Server sends PING_PACKET
2. Client responds with PONG_PACKET
3. Server responds to PING_RECEIVE

### Initial Handshake Packet

This packet is used by the server to initiate the encryption handshake.

JSON:

```json5
{
  "publicKey": "RSA_PUBLIC_KEY_HERE",
  "versionData": {
    "version": "protocol_version", // The version used by the sender (SEMANTIC VERSIONING)
    "minVersion": "minimum_supported_protocol_version" // The minimum version supported by the sender (SEMANTIC VERSIONING)
  }
}
```

### Key Response Packet

This packet is used to send the AES 256 bit key encrypted by the server's RSA 4096 public key

JSON:

```json5
{
  "secretKeyEncrypted": ["BYTES_HERE", 21, 21, 32]
}
```

### Connected Packet

This packet is used by the client to send the required information to the server.

JSON:

```json5
{
  "name": "Client-Name", // The client's name
  "os": "Windows/Linux etc.", // The sender's OS,
  "versionData": {
    "version": "protocol_version", // The version used by the sender (SEMANTIC VERSIONING)
    "minVersion": "minimum_supported_protocol_version" // The minimum version supported by the sender (SEMANTIC VERSIONING)
  }
}
```

### Self Message Packet

The self message packet is a packet used to call events. The events as of the date of writing are as follows:

- FILL_PASSWORD // Request the client to send a hashed password using HASHED_PASSWORD_PACKET
- INCORRECT_PASSWORD_ATTEMPT // The password attempted is wrong
- INCORRECT_PASSWORD_FAILURE // The passwords attempted were wrong, so cancelling authentication
- LOST_SERVER_CONNECTION
- REGISTER_PACKET //Called when the client has sucessfully been registered on the server
- TIMED_OUT_REGISTRATION

JSON:

```json5
{
  "type": "REGISTER_PACKET"
}
```

### Hashed Password Packet

Used for authentication by the server
The password is expected to be hashed and the packet wrapper is highly recommended to be encrypted

JSON:

```json5
{
  "hashedPassword": {
    "password": "SHA-256-hashedPassword"
  }
}
```
