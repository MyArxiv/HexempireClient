package net.donizyo.hexempire;

public class ClientEntry {
	private final Client client;
	static {
		System.load("G:\\Hexempire\\HexempireEngine\\HexempireClientEngine\\Debug\\HexempireClientEngine.dll");
		initEngine();
	}

	public ClientEntry(Client parent) {
		client = parent;
	}

	protected static native void initEngine();

	public native void process(int command);

	public native void mouseEvent(int button, int x, int y);
}
