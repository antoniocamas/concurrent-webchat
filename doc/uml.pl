@startuml
interface TextWebSocketHandler {}
interface User
{
- String name
- String color
}

class WebSocketUser {}

User <|-- WebSocketUser
WebSocketUser "1" *--> WebSocketSession

class WebSocketSession << one per user>>
{
- WebSocket socket
+ Map<K,V> getAttributes()
+ void send(String)
}

WebSocketSession "1" o--> Chat
WebSocketSession "1" o--> User

class ChatHandler <<singleton>>
{
- ChatManager chatmanager(10)
+ void newMessage(WebSocketSession)
}

class ChatManager << singleton >>
{
- int maxChats
- Map<String, Chat> chats
- Map<String, User> users
+ void newUser(User)
+ Chat newChat(String)
+ void closeChat(Chat)
}

class Chat << Global scope >>{
- String name
- Map<String,User> users
+ void addUser(User)
+ void removeUser(User)
+ void sendMessage(User, String)
+ void close()
}

ChatHandler -up-|> TextWebSocketHandler
ChatHandler --> WebSocketSession
ChatHandler "1" *--> ChatManager

Chat "1" *--> ChatManager
ChatManager "maxChats" *--> Chat
ChatManager "n" *--> User

Chat "n" *--> User
@enduml
