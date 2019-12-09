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

public class ChatCloseTest {

	@Test
	public void test_01_GIVEN_ChatManger_When_closeChat_Then_allUserNotified() throws InterruptedException, TimeoutException {

		final int numUsers = 3;
		CountDownLatch latch = new CountDownLatch(numUsers);
		DelayedAnswerWithCountDownForMockBuilder countDownLatch = new DelayedAnswerWithCountDownForMockBuilder(latch);
		ChatManager chatManager = new ChatManager(5);
		List<User> users = new ArrayList<>();
		
		Chat chat = chatManager.newChat("testChat", 5, TimeUnit.SECONDS);
		
		for(int i=0; i<numUsers; i++) {
			users.add(mock(TestUser.class));
			when(users.get(i).getName()).thenReturn("user_" + i);
			chatManager.newUser(users.get(i));
		}
		
		for(User user: users) {
			doAnswer(countDownLatch.build()).when(user).chatClosed(any(Chat.class));
		}
		
		chat.close();
		
		latch.await(3000, TimeUnit.MILLISECONDS);
		chatManager.close();
		
		for(User user: users) {
			verify(user).chatClosed(chat);
		}
	}
	
	@Test
	public void test_02_GIVEN_ChatManger_When_closeChats_Then_chats_are_closed() throws Throwable {
		final int numUsers = 4;
		final int numOfChatsPerUser = 50;
		ChatManager	chatManager = new ChatManager(numUsers*numOfChatsPerUser);
		TestConcurrencyManager concurrencyMngr = new TestConcurrencyManager(chatManager, numUsers*2);
				
		for (int i=0 ; i < numUsers*2; i++)
		{
			int userNumber = i;
			
			if (userNumber % 2 == 0) {
				concurrencyMngr.submitTask(
						() -> this.whenForTest_02_usersCreatingChats(chatManager, userNumber, numOfChatsPerUser));
			} else {
				concurrencyMngr.submitTask(
						() -> this.whenForTest_02_closeAllChats(chatManager));
			}
		}
		
		concurrencyMngr.assertThatAllExecutionsFinishOK(numUsers*2);
		
		this.whenForTest_02_closeAllChats(chatManager);
		concurrencyMngr.shutdownAllExecutors(); 
		
		assertThat(chatManager.getChats().size()).isEqualTo(0);
	}
	
	private boolean whenForTest_02_usersCreatingChats(ChatManager chatManager, final int userNumber, final int numOfChatsPerUser) throws InterruptedException, TimeoutException {
		
		chatManager.newUser(new TestUser("userName_" + userNumber));		

		for(int i=0; i < numOfChatsPerUser; i++)
		{
			String chatName = "Chat_" + i;
			chatManager.newChat(chatName, 5, TimeUnit.SECONDS);
		}
		return true;
	}
	
	private boolean whenForTest_02_closeAllChats(ChatManager chatManager) throws InterruptedException, TimeoutException {
		
	for(Chat chat: chatManager.getChats())
	{
		try {
			chat.close();
		} catch(IllegalArgumentException exc) {}
	}
	return true;
}

}
