package net.donizyo.hexempire;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ConnectionHandler implements CompletionHandler<Void, AsynchronousSocketChannel> {
	private final Client client;
	volatile int bufSize = 1024;

	ConnectionHandler(Client client) {
		this.client = client;
	}

	@Override
	public void completed(Void result, AsynchronousSocketChannel channel) {
		ByteBuffer clientBuffer = ByteBuffer.allocate(bufSize);
		channel.read(clientBuffer, clientBuffer, new InputHandler(client, channel));
		while (!client.isGameOver.get()) {
			if (client.taskManager.isEmpty()) {
				try {
					Thread.sleep(Utils.DELAY_LONG);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			channel.write(ByteBuffer.wrap(Integer.toHexString(client.taskManager.pop()).getBytes(Utils.CHARSET)));
			try {
				Thread.sleep(Utils.DELAY_SHORT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
		exc.printStackTrace();
	}
}