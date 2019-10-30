This is basically just a fork of my chatroom program that allows turning on and off lights hooked to the raspberry pi. This uses the pi4j lib. It's event system is heavily inspired by bukkit's

# Features:
- Object Encryption (Not SSL)
- Support for using RaspberryPi for manipulating pins using a custom file format and a GUI for it.
- ANSI Support using Jansi
- Multicast support for locating other servers in the same network
- Is built on using async code with Netty and the server/client core itself.
- Authentication system for restrictive access.
- Ban System with IPs and names
