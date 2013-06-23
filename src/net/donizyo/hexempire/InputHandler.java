package net.donizyo.hexempire;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class InputHandler implements CompletionHandler<Integer, ByteBuffer>
{
	private final Client client;
	private final AsynchronousSocketChannel channel;
	InputHandler(Client parent, AsynchronousSocketChannel channel)
	{
		client = parent;
		this.channel = channel;
	}

	@Override
	public void completed(Integer i, ByteBuffer buf)
	{
		if (i > 0)
		{
			buf.flip();
			int command = Utils.parseHexInt(Utils.CHARSET.decode(buf).toString());
			if ((command & 0xFF) == Communication.CHAT_NEW_BEGIN) {
				
			} else {
				client.clientEntry.process(command);
			}
			channel.read(buf, buf, this);
		}
		else if (i == -1)
		{
			try
			{
				System.out.println("¶Ô¶Ë¶ÏÏß:"+channel.getRemoteAddress());
				buf = null;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment)
	{
		exc.printStackTrace();
	}
}