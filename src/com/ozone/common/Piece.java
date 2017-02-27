package com.ozone.common;

import com.ozone.movements.BoardUtil;

public class Piece {
	
	int value;
	int xPosition;
	int yPosition;
	
	public Piece(int value, String square){
		this.value = value;
		int[] yx = Common.convertInputToSquare(square);
		this.yPosition = yx[0];
		this.xPosition = yx[1];
	}
	
	public Piece(int value, int[] yx){
		this.value = value;
		this.yPosition = yx[0];
		this.xPosition = yx[1];
	}
	public Piece(int value, int yPosition, int xPosition){
		this.value = value;
		this.yPosition = yPosition;
		this.xPosition = xPosition;
	}

	public Piece(int[] teamPiece) {
		this.value = teamPiece[0];
		this.yPosition = teamPiece[1];
		this.xPosition = teamPiece[2];
	}

	public Piece(Board board, int y, int x){
		this.yPosition = y;
		this.xPosition = x;
		this.value = board.getPiece(y,x);
	}
	public Piece(Board board, int[] yx){
		this.yPosition = yx[0];
		this.xPosition = yx[1];
		this.value = board.getPiece(yx);
	}
	
	public Piece(Board board, String square){
		int[] yx = Common.convertInputToSquare(square);
		this.yPosition = yx[0];
		this.xPosition = yx[1];
		this.value = board.getPiece(yx);
	}
	
	public int getTeam(){
		return value > 0 ? 1 : -1;
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getxPosition() {
		return xPosition;
	}

	public void setxPosition(int xPosition) {
		this.xPosition = xPosition;
	}

	public int getyPosition() {
		return yPosition;
	}

	public void setyPosition(int yPosition) {
		this.yPosition = yPosition;
	}
	
	public int[] toIntArray(){
		return new int[]{value, yPosition, xPosition};
	}
	
	@Override
	public String toString(){
		return BoardUtil.printPieceWithPosition(new int[]{value, yPosition, xPosition});
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		result = prime * result + xPosition;
		result = prime * result + yPosition;
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
		Piece other = (Piece) obj;
		if (value != other.value)
			return false;
		if (xPosition != other.xPosition)
			return false;
		if (yPosition != other.yPosition)
			return false;
		return true;
	}

}
