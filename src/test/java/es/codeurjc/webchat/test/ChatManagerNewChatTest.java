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

public class ChatManagerNewChatTest {

	@Test
	public void test_01_GIVEN_ChatManger_When_newChat_Then_allUserNotified() throws InterruptedException, TimeoutException {

		final int numUsers = 3;
		CountDownLatch latch = new CountDownLatch(numUsers);
		DelayedAnswerWithCountDownForMockBuilder countDownLatch = new DelayedAnswerWithCountDownForMockBuilder(latch);
		ChatManager chatManager = new ChatManager(5);
		List<User> users = new ArrayList<>();
		
		for(int i=0; i<numUsers; i++) {
			users.add(mock(TestUser.class));
			when(users.get(i).getName()).thenReturn("user_" + i);
			chatManager.newUser(users.get(i));
		}
		
		for(User user: users) {
			doAnswer(countDownLatch.build()).when(user).newChat(any(Chat.class));
		}
		
		Chat chat = chatManager.newChat("testChat", 5, TimeUnit.SECONDS);
		
		latch.await(3000, TimeUnit.MILLISECONDS);
		chatManager.close();
		
		for(User user: users) {
			verify(user).newChat(chat);
		}
	}
	
	@Test
	public void test_02_GIVEN_ChatManger_When_newChat_Then_no_chat_duplicated() throws Throwable {

		final int numUsers = 4;
		final int numOfChatsPerUser = 50;
		ChatManager chatManager = new ChatManager(numUsers*numOfChatsPerUser);
		TestConcurrencyManager concurrencyMngr = new TestConcurrencyManager(chatManager, numUsers);
			
		for (int i=0 ; i < numUsers; i++)
		{
			int userNumber = i;
			concurrencyMngr.submitTask(
					() -> this.whenForTest_02_allUsersTryToCreateTheSameChats(
							chatManager, userNumber, numOfChatsPerUser)
			);
		}		
		
		concurrencyMngr.assertThatAllExecutionsFinishOK(numUsers);
		
		concurrencyMngr.shutdownAllExecutors(); 
		
		assertThat(chatManager.getChats().size()).isEqualTo(numOfChatsPerUser);
					
	}
	
	private boolean whenForTest_02_allUsersTryToCreateTheSameChats(
			ChatManager chatManager, 
			final int userNumber, 
			final int numOfChatsPerUser) throws InterruptedException, TimeoutException {
		
		chatManager.newUser(new TestUser("userName_" + userNumber));		
		
		Chat returnedChat;
		for(int i=0; i < numOfChatsPerUser; i++)
		{
			String chatName = "Chat_" + i;
			returnedChat = chatManager.newChat(chatName, 5, TimeUnit.SECONDS);
			if(!returnedChat.getName().equals(chatName)) {
				System.out.println(
						"Race Condition reached! " 
								+ returnedChat.getName() 
								+ " != " + chatName);
				return false;
			}
		}
		return true;
	}
}
