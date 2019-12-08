package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Chat {

	private String name;
	private Map<String, User> users = new ConcurrentHashMap<>();
		
	private ChatManager chatManager;

	public Chat(ChatManager chatManager, String name) {
		this.chatManager = chatManager;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addUser(User user) {
		users.putIfAbsent(user.getName(), user);
		for(User u : users.values()){
			if (u != user) {
				chatManager.launchCommandInUser(u.getName(), ()->u.newUserInChat(this, user));
				//executors.get(u.getName()).execute(()->u.newUserInChat(this, user));
				
			}
		}
	}

	public void removeUser(User user) {
		users.remove(user.getName());
		for(User u : users.values()){
			chatManager.launchCommandInUser(u.getName(), ()->u.userExitedFromChat(this, user));
			//executors.get(u.getName()).execute(()->u.userExitedFromChat(this, user));
		}
	}

	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	public User getUser(String name) {
		return users.get(name);
	}

	public void sendMessage(User user, String message) {
		for(User u : users.values()){
			chatManager.launchCommandInUser(u.getName(), ()->u.newMessage(this, user, message));
			//executors.get(u.getName()).execute(()->u.newMessage(this, user, message));
		}
	}

	public void close() {
		this.chatManager.closeChat(this);
	}
}
