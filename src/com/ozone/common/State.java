package com.ozone.common;

import java.util.List;

public class State {
	
	Board board;
	Move move;
	String fen;
	List<Move> moveHistory;
	
	public State(){}
	
	public State(Board board, Move move, String fen, List<Move> moveHistory){
		this.board = board;
		this.move = move;
		this.fen = fen;
		this.moveHistory = moveHistory;
	}
	
	public List<Move> getMoveHistory() {
		return moveHistory;
	}
	public void setMoveHistory(List<Move> moveHistory) {
		this.moveHistory = moveHistory;
	}
	
	public Board getBoard() {
		return board;
	}
	public void setBoard(Board board) {
		this.board = board;
	}
	public Move getMove() {
		return move;
	}
	public void setMove(Move move) {
		this.move = move;
	}
	public String getFen() {
		return fen;
	}
	public void setFes(String fen) {
		this.fen = fen;
	}
}