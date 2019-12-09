package es.codeurjc.webchat.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
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

public class ChatUserSendMessageTest {

	@Test
	public void test_01_GIVEN_ChatManger_When_sendMessages_Then_are_sent_in_parallel() throws InterruptedException, TimeoutException {

		final int numUsers = 4;
		CountDownLatch latch = new CountDownLatch(numUsers-1);
		DelayedAnswerWithCountDownForMockBuilder countDownLatch = new DelayedAnswerWithCountDownForMockBuilder(latch);
		ChatManager chatManager = new ChatManager(5);
		List<User> users = new ArrayList<>();
		
		Chat chat = chatManager.newChat("testChat", 5, TimeUnit.SECONDS);
		
		for(int i=0; i<numUsers; i++) {
			users.add(mock(TestUser.class));
			when(users.get(i).getName()).thenReturn("user_" + i);
			chatManager.newUser(users.get(i));
			chat.addUser(users.get(i));
		}
		
		for(int i=1; i<numUsers; i++) {
			doAnswer(countDownLatch.delay(1000).build())
				.when(users.get(i))
				.newMessage(any(Chat.class), any(User.class), anyString());
		}
		
		long startTime = System.currentTimeMillis();
		
		chat.sendMessage(users.get(0), "test message");
		latch.await(5000, TimeUnit.MILLISECONDS);
		
		long time = System.currentTimeMillis() - startTime;
		System.out.println("It took" + time + "ms sending the message to all users");
		
		//Wait for the threads to finish. Needed before checking calls in the test.
		chatManager.close();


		assertThat(time)
			.as("Sending the messages took too long. Are the messages sent in parallel?")
			.isLessThan(3000);
		
		for(int i=0; i<numUsers; i++) {
			verify(users.get(i)).newMessage(chat, users.get(0), "test message");
		}
	}

	@Test
	public void test_02_GIVEN_ChatManger_When_sendMessages_Then_are_received_in_order() throws InterruptedException, TimeoutException {
		
		int numMessages = 5;
		long channelDelay = 500;
		CountDownLatch latch = new CountDownLatch(5);
		DelayedAnswerWithCountDownForMockBuilder countDownLatch = new DelayedAnswerWithCountDownForMockBuilder(latch);
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
		
		doAnswer(countDownLatch.delay(channelDelay).build())
			.when(userReceiver1)
			.newMessage(any(Chat.class), any(User.class), anyString());
				
		
		long startTime = System.currentTimeMillis();
		
		for(int i=0; i<numMessages;i++) {
			createdChat.sendMessage(userSender, "test message " + i);
		}
		
		latch.await(channelDelay * numMessages * 2, TimeUnit.MILLISECONDS);
		
		long time = System.currentTimeMillis() - startTime;
		System.out.println("It took" + time + "ms sending the 5 messages a single user.");
		
		//Wait for the threads to finish. Needed before checking calls in the test.
		chatManager.close();


		assertThat(time)
			.as("Sending the messages took too long. Are the messages sent in parallel?")
			.isGreaterThan(channelDelay * numMessages);
		
		InOrder inOrder = inOrder(userReceiver1);
		for(int i=0; i<numMessages;i++) {
			inOrder.verify(userReceiver1).newMessage(createdChat, userSender, "test message " +i);
		}
	}
}
