### Server 
   * ServerState  
     * follow
     * leader 
     * candidate

### Thread 
  * main  
    * calls init method in server
      * ServerRequestListener - call the execute method in Server.ServerState
      * HBThread
    * Client Request Handler
      
### Responding to client request

Example - create room

* if candidate -> hold 
* if follower -> request leader for approval 
* if leader -> directly call the createRoom method in the ServerState
