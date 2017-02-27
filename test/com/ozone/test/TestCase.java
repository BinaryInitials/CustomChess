package com.ozone.test;

import java.util.ArrayList;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Move;

public class TestCase {
	
	private Board board;
	private List<Move> testAgainstMoves;
	private boolean isExpected;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((board == null) ? 0 : board.hashCode());
		result = prime * result + (isExpected ? 1231 : 1237);
		result = prime
				* result
				+ ((testAgainstMoves == null) ? 0 : testAgainstMoves.hashCode());
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
		TestCase other = (TestCase) obj;
		if (board == null) {
			if (other.board != null)
				return false;
		} else if (!board.equals(other.board))
			return false;
		if (isExpected != other.isExpected)
			return false;
		if (testAgainstMoves == null) {
			if (other.testAgainstMoves != null)
				return false;
		} else if (!testAgainstMoves.equals(other.testAgainstMoves))
			return false;
		return true;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public List<Move> getTestAgainstMoves() {
		return testAgainstMoves;
	}

	public void setTestAgainstMoves(List<Move> testAgainstMoves) {
		this.testAgainstMoves = testAgainstMoves;
	}

	public boolean isExpected() {
		return isExpected;
	}

	public void setExpected(boolean isExpected) {
		this.isExpected = isExpected;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private String description;
	
	public TestCase(String description, String board, String move) {
		testAgainstMoves = new ArrayList<Move>();
		
		this.board = new Board(board);
		testAgainstMoves.add(new Move(this.board, move)); 
		this.isExpected = true;
		this.description = description;
	}
	
	public TestCase(String description, String board, String move, boolean isExpected) {
		testAgainstMoves = new ArrayList<Move>();
		
		this.board = new Board(board);
		testAgainstMoves.add(new Move(this.board, move)); 
		this.isExpected = isExpected;
		this.description = description;
	}
	
	public TestCase(String description, String board, List<String> moves) {
		testAgainstMoves = new ArrayList<Move>();
		
		this.board = new Board(board);
		for(String move : moves) {
			testAgainstMoves.add(new Move(this.board, move)); 
		}
		this.isExpected = true;
		this.description = description;
	}

	public TestCase(String description, String board, List<String> moves, boolean isExpected) {
		testAgainstMoves = new ArrayList<Move>();
		
		this.board = new Board(board);
		for(String move : moves) {
			testAgainstMoves.add(new Move(this.board, move)); 
		}
		this.isExpected = isExpected;
		this.description = description;
	}

}
