package es.sidelab.webchat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class ChatManagerTest {

	@Test
	public void newChat() throws InterruptedException, TimeoutException {

		ChatManager chatManager = new ChatManager(5);

		User testUser = mock(TestUser.class);
		when(testUser.getName()).thenReturn("testUser");
		
		chatManager.newUser(testUser);
		Chat createdChat = chatManager.newChat("testChat", 5, TimeUnit.SECONDS);

		//Wait for the threads to finish. Needed before checking calls in the test.
		chatManager.close();
		
		verify(testUser).newChat(createdChat);
		assertThat(createdChat.getName()).isEqualTo("testChat");
		
	}

	@Test
	public void newUserInChat() throws InterruptedException, TimeoutException {

		ChatManager chatManager = new ChatManager(5);

		User user1 = mock(TestUser.class);
		User user2 = mock(TestUser.class);
		
		when(user1.getName()).thenReturn("user1");
		when(user2.getName()).thenReturn("user2");
		
		chatManager.newUser(user1);
		chatManager.newUser(user2);

		Chat chat = chatManager.newChat("Chat", 5, TimeUnit.SECONDS);

		chat.addUser(user1);
		chat.addUser(user2);
		
		//Wait for the threads to finish. Needed before checking calls in the test.
		chatManager.close();
		
		verify(user1).newUserInChat(chat, user2);
	}
}
