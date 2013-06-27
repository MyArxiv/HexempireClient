package net.donizyo.hexempire.engine;

import net.donizyo.hexempire.Client;
import net.donizyo.hexempire.Communication;

public class ClientEngine {
	public static int length(int par) {
		if (par <= 0xf)
			return 1;
		else if (par <= 0xff)
			return 2;
		else if (par <= 0xfff)
			return 3;
		else if (par <= 0xffff)
			return 4;
		else if (par <= 0xfffff)
			return 5;
		else if (par <= 0xffffff)
			return 6;
		else if (par <= 0xfffffff)
			return 7;
		else
			return 8;
	}
	
	public static int push(int cmd, final int par) {
		cmd <<= length(par) * Communication.HEXLEN;
		cmd |= par;
		return cmd;
	}

	public static void process(Client client, int command) {
		
	}
}
