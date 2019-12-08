package es.codeurjc.webchat.test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

public class TestUser implements User {

	public String name;

	public TestUser(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public String getColor(){
		return "007AFF";
	}

	@Override
	public void newChat(Chat chat) {
		println("New chat " + chat.getName());
	}

	@Override
	public void chatClosed(Chat chat) {
		println("Chat " + chat.getName() + " closed ");
	}

	@Override
	public void newUserInChat(Chat chat, User user) {
		println("New user " + user.getName() + " in chat " + chat.getName());
	}

	@Override
	public void userExitedFromChat(Chat chat, User user) {
		println("User " + user.getName() + " exited from chat " + chat.getName());
	}

	@Override
	public void newMessage(Chat chat, User user, String message) {
		println("New message '" + message + "' from user " + user.getName()
				+ " in chat " + chat.getName());
	}

	@Override
	public String toString() {
		return "User[" + name + "]";
	}
	
	private void println(String message) {
		System.out.println("[" + Thread.currentThread().getName() + "][" + this.name + "] " + message);
	}
}
