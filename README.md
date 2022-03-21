# CS4262 Distributed Systems - Group Project - Chat Server App

The goal of `CS4262 Distributed Systems - Group Project` is to create a distributed chat application.
The system consists of two main distributed components: chat servers and chat clients.
This repository contains the source code for chat servers.

For the sourcecode of the chat clients please refer to [
CS4262_ChatClient](https://github.com/GayashanNA/CS4262_ChatClient) by Dr. Gayashan Amarasinghe.

## Functionalities

- Join the chat application with a globally unique identifier
- Exit from the chat application
- Create a chat room with a globally unique identifier
- Delete a chat room
- Join to the already existing rooms of different servers
- Message members of the chatroom
- List all the chatrooms available
- List all the clients in the current chatroom

## Setup

1. Create a configuration `txt` file with `serverid`, `server_address`, `clients_port` and `coordination_port` where,
   - `serverid` is the name of the server,
   - `server_address` is the ip address or hostname of the server,
   - `clients_port` is the port used to communicate with clients,
   - `coordination_port` is the port used by the server to communicate with other servers.

    The content should be tab-delimited without a header as shown below.
    ```text
    s1	localhost	4444	5555
    s2	localhost	4445	5556
    s3	localhost	4446	5557
    ```
2. Generate a jar file using the following instructions.

    // TODO: Jar file creating instructions

3. Run the jar file with following parameters.
   - `serverid` - `-i` - server id specified in the configuration file
   - `servers_conf` - `p` - path to the configuration file.
   
   ```shell
    java -jar chat-server-app.jar -i serverid -p servers_conf
   ```
## Contribution

- [Damika Gamlath](https://github.com/damikag)
- [Ruchin Amaratunga](https://github.com/ruchinamaratunga)
- [Sasindu Alahakoon](https://github.com/SasinduDilshara)
- [Gayal Dassanayake](https://github.com/gayaldassanayake)
