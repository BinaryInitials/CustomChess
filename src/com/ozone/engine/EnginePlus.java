package com.ozone.engine;

public class EnginePlus {
	Engine engine;
	long time;
	int score;
	int win;
	int loss;
	int tie;
	public EnginePlus(Engine engine){
		this.engine = engine;
		time = 0;
		score = 0;
		win = 0;
		loss = 0;
		tie = 0;
	}
	public Engine getEngine() {
		return engine;
	}
	public void setEngine(Engine engine) {
		this.engine = engine;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getWin() {
		return win;
	}
	public void setWin(int win) {
		this.win = win;
	}
	public int getLoss() {
		return loss;
	}
	public void setLoss(int loss) {
		this.loss = loss;
	}
	public int getTie() {
		return tie;
	}
	public void setTie(int tie) {
		this.tie = tie;
	}
}