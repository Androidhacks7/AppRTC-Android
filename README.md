# AppRTC-Android

This Android client is an Enhancement on top of https://github.com/njovy/AppRTCDemo and is pretty much a mirror of what is present in default WebRTC Android

The app is built pointing to https://github.com/Androidhacks7/WebRTC-Signaling-Server instead of apprtc.appspot.com
Also, the concept of users joining a room has been replaced with a call based architecture. This AppRTC android client along with the server mentioned, forms the complete stack to establish a video call from one mobile client to another

The app can be built and deployed as is with just changing the server IP in ServerConfiguration.java
The port numbers can be kept as is

Note: This will work only when the mobile client and server are on the same network
