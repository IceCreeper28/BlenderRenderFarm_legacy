# BlenderRenderFarm

> This program is currently in a very early development step. Currently this repository exists only for me to not forget this project idea.
>
> Development will not be constant and regular, because I develop this in my free time.



## What is it?

BlenderRenderFarm is a project, that simplifies the Blender NetworkRender process. This means, that you can run a rendering network which renders blender animations on multiple devices thus increasing the rendering speed.



## How does it work?

The program consists of a server and several clients.

1. The server gets a blender file and an output directory and distributes the blender file among all the connected clients.
2. The server then sends out the individual frames to be rendered, and the clients work on their assigned frames. 
3. When a client finishes rendering its frame, it sends it back to the server and requests the next available frame. If there are no available frames the client shuts down.



## How to use?

### Examples

#### Server

``-p 1337 -b "D:/BlenderRenderFarm/coolblenderproject.blend" -w "D:/BlenderRenderFarm/workingDirectoryServer/"``

-p is the server port

-b is the blender file to be rendered

-w is the workingDirectory of the server



#### Client

``-ip ::1 -p 1337 -b "C:/Program Files/Blender Foundation/Blender/blender.exe" -w "D:/BlenderRenderFarm/wokringDirectoryClient/"``

-ip is the server ip

-p is the server port

-b is the blender.exe

-w is the workingDirectory of the client