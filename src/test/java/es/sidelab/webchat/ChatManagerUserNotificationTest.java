package es.sidelab.webchat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class ChatManagerUserNotificationTest {

	@Test
	public void test_01_GIVEN_ChatManger_When_sendMessages_Then_are_sent_in_parallel() throws InterruptedException, TimeoutException {

		CountDownLatch latch = new CountDownLatch(3);
		
		ChatManager chatManager = new ChatManager(5);
				
		User userSender = mock(TestUser.class);
		User userReceiver1 = mock(TestUser.class);
		User userReceiver2 = mock(TestUser.class);
		User userReceiver3 = mock(TestUser.class);
		
		when(userSender.getName()).thenReturn("userSender");
		when(userReceiver1.getName()).thenReturn("userReceiver1");
		when(userReceiver2.getName()).thenReturn("userReceiver2");
		when(userReceiver3.getName()).thenReturn("userReceiver3");
		
		chatManager.newUser(userSender);
		chatManager.newUser(userReceiver1);
		chatManager.newUser(userReceiver2);
		chatManager.newUser(userReceiver3);
		
		Chat createdChat = chatManager.newChat("testChat", 5, TimeUnit.SECONDS);
		
		createdChat.addUser(userSender);
		createdChat.addUser(userReceiver1);
		createdChat.addUser(userReceiver2);
		createdChat.addUser(userReceiver3);
		
		Answer<Object> delayedAnswer = new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) throws InterruptedException {
				Thread.sleep(1000);
				latch.countDown();
				return null;
			}
		};
				
		doAnswer(delayedAnswer).when(userReceiver1).newMessage(any(Chat.class), any(User.class), anyString());
		doAnswer(delayedAnswer).when(userReceiver2).newMessage(any(Chat.class), any(User.class), anyString());
		doAnswer(delayedAnswer).when(userReceiver3).newMessage(any(Chat.class), any(User.class), anyString());
		
		
		long startTime = System.currentTimeMillis();
		
		createdChat.sendMessage(userSender, "test message");
		latch.await(3000, TimeUnit.MILLISECONDS);
		
		long time = System.currentTimeMillis() - startTime;
		System.out.println("It took" + time + "ms sending the message to all users");
		
		//Wait for the threads to finish. Needed before checking calls in the test.
		chatManager.close();


		assertThat(time)
			.as("Sending the messages took too long. Are the messages sent in parallel?")
			.isLessThan(3000);
		
		verify(userReceiver1).newMessage(createdChat, userSender, "test message");
		verify(userReceiver2).newMessage(createdChat, userSender, "test message");
		verify(userReceiver3).newMessage(createdChat, userSender, "test message");
	}

	@Test
	public void test_02_GIVEN_ChatManger_When_sendMessages_Then_are_received_in_order() throws InterruptedException, TimeoutException {
		
		CountDownLatch latch = new CountDownLatch(5);
		
		ChatManager chatManager = new ChatManager(5);
				
		User userSender = mock(TestUser.class);
		User userReceiver1 = mock(TestUser.class);

		
		when(userSender.getName()).thenReturn("userSender");
		when(userReceiver1.getName()).thenReturn("userReceiver1");
		
		chatManager.newUser(userSender);
		chatManager.newUser(userReceiver1);
		
		Chat createdChat = chatManager.newChat("testChat", 5, TimeUnit.SECONDS);
		
		createdChat.addUser(userSender);
		createdChat.addUser(userReceiver1);
		
		Answer<Object> delayedAnswer = new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) throws InterruptedException {
				Thread.sleep(500);
				latch.countDown();
				return null;
			}
		};
				
		doAnswer(delayedAnswer).when(userReceiver1).newMessage(any(Chat.class), any(User.class), anyString());
				
		long startTime = System.currentTimeMillis();
		
		createdChat.sendMessage(userSender, "test message 1");
		createdChat.sendMessage(userSender, "test message 2");
		createdChat.sendMessage(userSender, "test message 3");
		createdChat.sendMessage(userSender, "test message 4");
		createdChat.sendMessage(userSender, "test message 5");
		latch.await(3000, TimeUnit.MILLISECONDS);
		
		long time = System.currentTimeMillis() - startTime;
		System.out.println("It took" + time + "ms sending the 5 messages a single user.");
		
		//Wait for the threads to finish. Needed before checking calls in the test.
		chatManager.close();


		assertThat(time)
			.as("Sending the messages took too long. Are the messages sent in parallel?")
			.isGreaterThan(2500);
		
		InOrder inOrder = inOrder(userReceiver1);
		inOrder.verify(userReceiver1).newMessage(createdChat, userSender, "test message 1");
		inOrder.verify(userReceiver1).newMessage(createdChat, userSender, "test message 2");
		inOrder.verify(userReceiver1).newMessage(createdChat, userSender, "test message 3");
		inOrder.verify(userReceiver1).newMessage(createdChat, userSender, "test message 4");
		inOrder.verify(userReceiver1).newMessage(createdChat, userSender, "test message 5");
	}
}
