This is basically just a fork of my chatroom program that allows turning on and off lights hooked to the raspberry pi. This uses the pi4j lib. It's event system is heavily inspired by bukkit's

| **Software** | **Status** |
|:---:|:---:|
| **Java Server/Client**        | ![](https://github.com/Fernthedev/light-chat/workflows/Java%20CI/badge.svg)|

Java: ![](https://github.com/Fernthedev/light-chat/workflows/Java%20CI/badge.svg)

# Features:
- Uses JSON for packet wrapping and allows cross-platform usage 
- Encryption using RSA 4096 key exchange (server generates temporary key pair -> server sends public key -> client generates AES 256 key -> encrypts key with server's public key -> server and client use AES key from client for the rest of communications) 
- Uses [LightReader](https://github.com/Fernthedev/LightReader) API
- Support for using RaspberryPi for manipulating pins using a custom file format and a GUI for it.
- ANSI Support using Jansi
- Multicast support for locating other servers in the same network
- Is built on using async code with Netty and the server/client core itself.
- Authentication system for restrictive access.
- Ban System with IPs and names
