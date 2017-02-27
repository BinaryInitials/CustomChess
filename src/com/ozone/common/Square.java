package com.ozone.common;


public class Square {
	int x;
	int y;
	
	public Square(String posText){
		int[] pos = (Common.convertInputToSquare(posText));
		this.y = pos[0];
		this.x = pos[1];
	}
	
	public Square(int[] pos){
		this.y = pos[0];
		this.x = pos[1];
	}
	
	public Square(int x, int y){
		this.x = x;
		this.y = y;
	}

	public void setX(int x){
		this.x=x;
	}

	public void setY(int y){
		this.y=y;
	}
	
	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}
	
	public String toString(){
		return "" + (char)(x+65) + (y+1);
	}
	
	public static String toString(int[] p){
		return toString(p[0], p[1]);
	}
	
	public static String toString(int y, int x){
		return "" + (char)(x+65) + (y+1);
	}
}
