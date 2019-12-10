package es.codeurjc.webchat.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;

public class ChatManagerNewChatTimeOutTest {

	@Test
	public void test_01_GIVEN_ChatManger_When_newChat_dont_Fit_Then_Timeout() 
			throws InterruptedException, TimeoutException {

		final int maxChats = 3;
		final long timeout = 500;
		ChatManager chatManager = new ChatManager(maxChats);
		
		for(int i=0; i<maxChats; i++) {
			chatManager.newChat("testChat_" + i, timeout, TimeUnit.MILLISECONDS);
		}
				
		Throwable thrown = catchThrowable(
				()->chatManager.newChat(
						"notFittingChat", timeout, TimeUnit.MILLISECONDS));
		
		chatManager.close();
		
		assertThat(thrown)
			.hasMessage("There is no enought capacity to create a new chat")
			.isInstanceOf(TimeoutException.class)
			.hasNoCause();
	}

	@Test
	public void test_02_GIVEN_ChatManger_When_newChat_dont_Fit_Then_Created() 
			throws InterruptedException, TimeoutException, ExecutionException {

		final int maxChats = 3;
		final long timeout = 100;
		ChatManager chatManager = new ChatManager(maxChats);
		TestConcurrencyManager concurrencyMngr = new TestConcurrencyManager(chatManager, 1);
		
		for(int i=0; i<maxChats; i++) {
			chatManager.newChat("testChat_" + i, timeout, TimeUnit.MILLISECONDS);
		}
		
		concurrencyMngr.submitTask(
				()->whenForTest_02_closeOneChatAfterATime(chatManager, timeout));
				
		Chat fittinChat = chatManager.newChat(
				"FittingChat", timeout, TimeUnit.MILLISECONDS);
		
		concurrencyMngr.assertThatAllExecutionsFinishOK(1);
		concurrencyMngr.shutdownAllExecutors(); 
		chatManager.close();
		
		assertThat(chatManager.getChats().size()).isEqualTo(maxChats);
		assertThat(chatManager.getChat("FittingChat")).isEqualTo(fittinChat);
	}
	
	private boolean whenForTest_02_closeOneChatAfterATime(
			ChatManager chatManager, final long timeout) throws InterruptedException {
		
		Thread.sleep(timeout/2);
		chatManager.closeChat(chatManager.getChat("testChat_0"));

		return true;
	}

	
}
