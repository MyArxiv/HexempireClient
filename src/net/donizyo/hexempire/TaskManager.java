package net.donizyo.hexempire;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

class ClientTaskManager implements Runnable {
	private final Client client;
	final Deque<Integer> commands;
	final Deque<Integer> queries;//queries

	public ClientTaskManager(Client parent) {
		client = parent;
		commands = new ArrayDeque<>(16);
		queries = new ArrayDeque<>(16);
	}

	public void push(int command) {
		synchronized (commands) {
			commands.addLast(command);
		}
	}

	public int pop() {
		synchronized (commands) {
			return commands.removeFirst();
		}
	}

	public boolean isEmpty() {
		synchronized (commands) {
			return commands.isEmpty();
		}
	}

	@Override
	public void run() {
		if (isEmpty()) {
			try {
				Thread.sleep(Utils.DELAY_LONG);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		client.channel.write(ByteBuffer.wrap(Integer.toHexString(pop()).getBytes(Utils.CHARSET)));
	}
}
