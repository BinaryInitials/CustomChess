package com.ozone.common;

import static com.ozone.movements.BoardUtil.SPACE;
import static com.ozone.movements.BoardUtil.printPiece;

import java.util.HashSet;
import java.util.Set;

import com.ozone.movements.MoveUtil.Status;


public class Move {

	private Set<Status> statuses = new HashSet<Status>();
	private int pieceCaptured;
	private int score;
	private int pieceMoving;
	private int y1;
	private int x1;
	private int y2;
	private int x2;
	
	public Move(Board board, String squares){
		String square1 = squares.replaceAll("^(..).*", "$1");
		String square2 = squares.replaceAll(".*(..)$", "$1");
		int[] yx1 = Common.convertInputToSquare(square1);
		int[] yx2 = Common.convertInputToSquare(square2);
		setEverything(board.getPiece(square1), board.getPiece(yx2), yx1, yx2);
	}
	public Move(int pieceMoving, String squares){
		String square1 = squares.replaceAll("^(..).*", "$1");
		String square2 = squares.replaceAll(".*(..)$", "$1");
		setEverything(pieceMoving, SPACE, Common.convertInputToSquare(square1), Common.convertInputToSquare(square2));
	}
	public Move(int pieceMoving, int capturedPiece, String squares){
		String square1 = squares.replaceAll("^(..).*", "$1");
		String square2 = squares.replaceAll(".*(..)$", "$1");
		setEverything(pieceMoving, capturedPiece, Common.convertInputToSquare(square1), Common.convertInputToSquare(square2));
	}
	public Move(int pieceMoving, String square1, String square2){
		setEverything(pieceMoving, SPACE, Common.convertInputToSquare(square1), Common.convertInputToSquare(square2));
	}
	public Move(int pieceMoving, int pieceCaptured, String square1, String square2){
		setEverything(pieceMoving, pieceCaptured, Common.convertInputToSquare(square1), Common.convertInputToSquare(square2));
	}
	public Move(int pieceMoving, Board board, int y1, int x1, int y2, int x2){
		setEverything(pieceMoving, board.getPiece(y2,x2), y1, x1, y2, x2);
	}
	public Move(int pieceMoving, int y1, int x1, int y2, int x2){
		setEverything(pieceMoving, 0, y1, x1, y2, x2);
	}
	public Move(int pieceMoving, int pieceCaptured, int y1, int x1, int y2, int x2){
		setEverything(pieceMoving, pieceCaptured, y1, x1, y2, x2);
	}

	public Move(Piece piece, Piece captured){
		setEverything(piece.getValue(), captured.getValue(), piece.getyPosition(), piece.getxPosition(), captured.getyPosition(), captured.getxPosition());
	}
	
	public Move(int pieceMoving, int pieceCaptured, int y1, int x1, int[] move){
		setEverything(pieceMoving, pieceCaptured, y1, x1, move[0], move[1]);
	}
	
	public Move(int piece, int[] yx1, int[] yx2){
		setEverything(piece, 0, yx1, yx2);
	}
	public Move(int pieceMoving, int pieceCaptured, int[] yx1, int[] yx2){
		setEverything(pieceMoving, pieceCaptured, yx1, yx2);
	}
	
	private void setEverything(int pieceMoving, int pieceCaptured, int[] yx1, int[] yx2) {
		setEverything(pieceMoving, pieceCaptured, yx1[0], yx1[1], yx2[0], yx2[1]);
	}
	private void setEverything(int pieceMoving, int pieceCaptured, int y1, int x1, int y2, int x2) {
		this.pieceMoving = pieceMoving;
		this.pieceCaptured = pieceCaptured;
		this.y1 = y1;
		this.x1 = x1;
		this.y2 = y2;
		this.x2 = x2;
	}
	
	public int getPieceMoving(){
		return pieceMoving;
	}
	public int getPieceCaptured(){
		return pieceCaptured;
	}

	public void setPieceCaptured(int pieceCaptured){
		this.pieceCaptured = pieceCaptured; 
	}

	public Piece getPieceBeforeMove(){
		return new Piece(pieceMoving, y1, x1);
	}
	public Piece getPieceAfterMove(){
		return new Piece(pieceMoving, y2, x2);
	}
	
	public int[] getFromPos(){
		return new int[]{y1, x1};
	}

	public int[] getToPos(){
		return new int[]{y2, x2};
	}
	
	public void setStatuses(Set<Status> statuses){
		this.statuses = statuses;
	}
	
	public Set<Status> getStatuses(){
		return statuses;
	}
	
	public void addAllStatuses(Set<Status> statuses){
		if(statuses == null){
			statuses = new HashSet<Status>();
		}
		this.statuses.addAll(statuses);
	}
	public void addStatus(Status status){
		if(statuses == null){
			statuses = new HashSet<Status>();
		}
		statuses.add(status);
	}
	
	public int getScore(){
		return score;
	}
	public void setScore(int score){
		this.score = score;
	}
	
	public int[] convertToArray(){
		return new int[]{pieceMoving, y1, x1, y2, x2};
	}
	
	public Board updateBoard(Board board) {
		Board finalBoard = new Board(board.getBoard());
		finalBoard.movePiece(pieceMoving, y1, x1, y2, x2);
		return finalBoard;
	}
	
	public String getSquare(){
		return Square.toString(y1,x1) + Square.toString(y2,x2);
	}
	public String getSquareReverse(){
		return Square.toString(y2,x2) + Square.toString(y1,x1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pieceMoving;
		result = prime * result + x1;
		result = prime * result + x2;
		result = prime * result + y1;
		result = prime * result + y2;
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
		Move other = (Move) obj;
		if (pieceMoving != other.pieceMoving)
			return false;
		if (x1 != other.x1)
			return false;
		if (x2 != other.x2)
			return false;
		if (y1 != other.y1)
			return false;
		if (y2 != other.y2)
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		String square1 = Square.toString(y1, x1);
		String square2 = Square.toString(y2, x2);
		return printPiece(pieceMoving) + ": " + square1 + square2;
	}
}