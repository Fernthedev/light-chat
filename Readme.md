This is basically just a fork of my chatroom program that allows turning on and off lights hooked to the raspberry pi. This uses the pi4j lib. It's event system is heavily inspired by bukkit's

|                                 **Software**                                 |                                          **Status**                                           |
| :--------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------: |
|                            **Java Server/Client**                            |      ![Java CI](https://github.com/Fernthedev/light-chat/workflows/JavaCI/badge.svg)       |
|             **Dart Client CI (Ubuntu 18.04/MacOS/Windows 1909)**             | ![Dart Client CI](https://github.com/Fernthedev/light-chat/workflows/Dart%20Client/badge.svg) |
| **C# .NET Client CI (Ubuntu 18.04/MacOS/Windows 1909) [ALPHA & INCOMPLETE]** | ![DotNET CSharp CI](https://github.com/Fernthedev/light-chat/workflows/DotNET%20CSharp%20CI/badge.svg)  |

# Backstory and Future plans

This project was originally a fork of my java chatroom program. It was actually my first sucessful network application that could handle multiple clients and send messages between each other. Later on I worked on a Raspberry Pi and was trying to make a network application that could control my Arduino Relay Module remotely, so I developed this. I later realized I could make a Java Android App which sucessfully worked however buggy and unreliable. Later on I tried to accomplish it on Flutter though I realized I would need to find a way to communicate in Dart to a Java Netty Server. I first attempted Protobuf though it soon seemed it wouldn't work either. After a few months I later realized I could serialize the data in JSON and encrypt the text.

I have sucessfully created a Dart client for the Java server and a Flutter app will soon be functioning as well by using the client as the backend. My hope is that I will be able to create a multi-language/cross-platform communication system for developers as either an API\* or as a base for other applications.

This has now become a protocol project itself rather than a project to manage Raspberry Pies. This will continue to be developed as a framework/protocol with certain features rather than a server/client itself. The point of this is to be a protocol with a basic starting point for cross-language communication. 

# About

This is a network platform in which I hope to soon become a multi-language/cross-platform communication system where developers can use as either an API\* or as a base for other applications.

- API is still not implemented nor fully planned yet. It is something I hope to accomplish in the future.

# Specifications:
The specifications can be found [here](docs/specs.md)

# Features:

- Uses JSON for packet wrapping and allows cross-platform usage
- Encryption using RSA 4096 key exchange (server generates temporary key pair -> server sends public key -> client generates AES 256 key -> encrypts key with server's public key -> server and client use AES key from client for the rest of communications)
- ANSI Support using Jansi (Java)
- Multicast support for locating other servers in the same network
- Is built on using async code with Netty and the server/client core itself. (Java)
- Authentication system for restrictive access.
- Ban System with IPs and names

# Compiling and debugging

## Java
Java has six modules, three of which are terminal-based (inside the terminal folder) and three are the main cores of the program. To debug, you would prefer to use the terminal-based implementations as they show logs and you must use the "-debug" flag for the program to not exit immidietely when using any Intellij-based IDEs.

## C# (Dotnet) 
Dotnet follows a similar philosophy Java follows with modules, except the "-debug" flag is not required though it is recommended since it shows extra information.

## Dart
Dart only has the client module for now though most of the code will be moved to different modules later on.
