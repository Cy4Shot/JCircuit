package jcircuit.logic;

import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;

// Use MutablePair<Integer, Integer> since this doesn't represent a position
public record Wiring(MutablePair<Integer, Integer> input, List<Vector2i> turns, MutablePair<Integer, Integer> output) {
	public void updateDelete(Integer deleted) {
		if (input.getLeft() >= deleted) {
			input.setLeft(input.getLeft() - 1);
		}
		if (output.getLeft() >= deleted) {
			output.setLeft(output.getLeft() - 1);
		}
	}
	
	public boolean isAOutput(List<GateInstance> gates) {
		return gates.get(input.getLeft()).gate().isOutput(input.getRight());
	}
	
	public boolean isBOutput(List<GateInstance> gates) {
		return gates.get(output.getLeft()).gate().isOutput(output.getRight());
	}
}