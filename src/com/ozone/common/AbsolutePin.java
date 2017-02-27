package com.ozone.common;

import com.ozone.movements.BoardUtil;


public class AbsolutePin {
	int[] pinningPiece;
	int[] pinnedPiece;
	int[] endPiece;
	boolean isDiag;
	
	public AbsolutePin(Piece pinningPiece, Piece pinnedPiece, Piece endPiece, boolean isDiag){
		this.pinningPiece = pinningPiece.toIntArray();
		this.pinnedPiece = pinnedPiece.toIntArray();
		this.endPiece = endPiece.toIntArray();
		this.isDiag = isDiag;
	}
	
	public AbsolutePin(int[] pinningPiece, int[] pinnedPiece, int[] endPiece, boolean isDiag){
		this.pinningPiece = pinningPiece;
		this.pinnedPiece = pinnedPiece;
		this.endPiece = endPiece;
		this.isDiag = isDiag;
	}
	
	public int[] getPinningPiece() {
		return pinningPiece;
	}
	public void setPinningPiece(int[] pinningPiece) {
		this.pinningPiece = pinningPiece;
	}
	public int[] getPinnedPiece() {
		return pinnedPiece;
	}
	public void setPinnedPiece(int[] pinnedPiece) {
		this.pinnedPiece = pinnedPiece;
	}
	public int[] getEndPiece() {
		return endPiece;
	}
	public void setEndPiece(int[] endPiece) {
		this.endPiece = endPiece;
	}
	public boolean isDiag() {
		return isDiag;
	}
	public void setDiag(boolean isDiag) {
		this.isDiag = isDiag;
	}
	
	@Override
	public String toString(){
		return BoardUtil.printPieceWithPosition(pinningPiece) + "\t" + BoardUtil.printPieceWithPosition(pinnedPiece) + "\t" + BoardUtil.printPieceWithPosition(endPiece);
	}
}
