package com.ozone.common;

public class StateMinMax {
	
	Board board;
	int depth;
	
	public StateMinMax(Board board, int depth){
		this.board = board;
		this.depth = depth;
	}
	
	public Board getBoard() {
		return board;
	}
	public void setBoard(Board board) {
		this.board = board;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	@Override
	public String toString() {
		return "StateMinMax [board=" + board + ", depth="
				+ depth + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((board == null) ? 0 : board.hashCode());
		result = prime * result + depth;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateMinMax other = (StateMinMax) obj;
		if (board == null) {
			if (other.board != null)
				return false;
		} else if (!board.equals(other.board))
			return false;
		if (depth != other.depth)
			return false;
		return true;
	}
}
