# Java
This is the Java project. The original and best maintained version of the protocol. This uses Netty and can be installed using Jitpack:

## Install (JitPack)
You can simply follow the instructions at https://jitpack.io/#Fernthedev/light-chat to add to your project.

## Install (from source)

To install from source, you can simply follow these steps:

### Linus/macOs
```sh
git clone https://github.com/Fernthedev/light-chat.git
cd light-chat/java
chmod +x ./gradlew
./gradlew clean build publishToMavenLocal
```

### Windows 
```cmd
git clone https://github.com/Fernthedev/light-chat.git
cd light-chat/java
gradlew.bat clean build publishToMavenLocal
```