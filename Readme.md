This is basically just a fork of my chatroom program that allows turning on and off lights hooked to the raspberry pi. This uses the pi4j lib. It's event system is heavily inspired by bukkit's

| **Software** | **Status** |
|:---:|:---:|
| **Java Server/Client**        | ![](https://github.com/Fernthedev/light-chat/workflows/Java%20CI/badge.svg)|
| **Dart Client CI (Ubuntu 18.04/latest)**        | ![](https://github.com/Fernthedev/light-chat/workflows/Dart%20Client%20CI%20(Ubuntu)/badge.svg)|
<!-- | **Dart Client CI (MacOS Catalina 10.15/latest)**        | ![](https://github.com/Fernthedev/light-chat/workflows/Dart%20Client%20CI%20(MacOS)/badge.svg)| -->
<!-- | **Dart Client CI (Windows Server 2019/latest)**        | ![](https://github.com/Fernthedev/light-chat/workflows/Dart%20Client%20CI%20(Windows)/badge.svg)| -->

# Backstory and Future plans
This project was originally a fork of my java chatroom program. It was actually my first sucessful network application that could handle multiple clients and send messages between each other. Later on I worked on a Raspberry Pi and was trying to make a network application that could control my Arduino Relay Module remotely, so I developed this. I later realized I could make a Java Android App which sucessfully worked however buggy and unreliable. Later on I tried to accomplish it on Flutter though I realized I would need to find a way to communicate in Dart to a Java Netty Server. I first attempted Protobuf though it soon seemed it wouldn't work either. After a few months I later realized I could serialize the data in JSON and encrypt the text. 

I have sucessfully created a Dart client for the Java server and a Flutter app will soon be functioning as well by using the client as the backend. My hope is that I will  be able to create a multi-language/cross-platform communication system for developers as either an API* or as a base for other applications.

# About
This is a network platform in which I hope to soon become a multi-language/cross-platform communication system where developers can use as either an API* or as a base for other applications.

* API is still not implemented nor fully planned yet. It is something I hope to accomplish in the future.

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
