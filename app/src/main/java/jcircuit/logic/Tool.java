package jcircuit.logic;

public enum Tool {
	EMPTY(false),
	SELECT(false),
	WIRE(false),
	AND(true),
	NAND(true),
	OR(true),
	NOR(true),
	XOR(true),
	XNOR(true),
	BUFFER(true),
	NOT(true),
	TEXT(false),
	DELETE(false);

	public boolean isGate;

	Tool(boolean gate) {
		this.isGate = gate;
	}

	public boolean isEmpty() {
		return equals(EMPTY);
	}

	public Gate getGate() {
		if (!isGate)
			return null;

		for (Gate g : Gate.values()) {
			if (g.name().equals(name()))
				return g;
		}

		return null;
	}
}