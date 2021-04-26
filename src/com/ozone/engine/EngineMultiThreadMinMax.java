package com.ozone.engine;

import static com.ozone.movements.MoveUtil.getAllMoveForTeamMinMaxSpecialSorting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.movements.BoardUtil;


public class EngineMultiThreadMinMax implements Runnable{
	public Thread t;
	private String threadName;
	private Board board;
	private EngineSingleThreadMinMax e;
	private HashMap<Move, Integer> output = new HashMap<Move, Integer>();
	
	
	public EngineMultiThreadMinMax(String threadName) {
		this.threadName = threadName;
	}

	@Override
	public void run() { 
		output = e.findMoves(board);
	}
	
	public HashMap<Move, Integer> getOutput(){
		return output;
	}
	
	public void start(Move move, Board board, int team){
		if(t==null){
			System.out.println(threadName + "\t" + move);
			this.board = board;
			e = new EngineSingleThreadMinMax(team, move);
			t = new Thread(this, threadName);
			t.start();
		}
 	}

	public static void main(String[] args){
		Board board = new Board("r1bqk2r/pppp1ppp/2n5/4p3/2B1n3/P1P2N2/1PP2PPP/R1BQK2R w KQkq - 0 1");
		int team = BoardUtil.WHITE;
		{
			System.out.println("Single Threaded Way: ");
			Date tic = new Date();
			Engine e = new EngineMinMaxNoMateDectionMAXDEPTH5(team);
			Move move = e.findMove(board);
			System.out.println(move);
			Date toc = new Date();
			long time = toc.getTime() - tic.getTime();
			System.out.println("Elapsed time: " + time + "ms");
			
		}
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("Multi Threaded Way: ");
		Date tic = new Date();
		System.out.println("Started at " + tic);
		List<Thread> threadList = new ArrayList<Thread>();
		List<EngineMultiThreadMinMax> emtmms = new ArrayList<EngineMultiThreadMinMax>();
		int threadCount = 20;

		List<Move> moves = getAllMoveForTeamMinMaxSpecialSorting(board, team);
		int i=0;
		for(Move move : moves){
			i++;
			EngineMultiThreadMinMax emtmm = new EngineMultiThreadMinMax("Thread-" + i);
			emtmm.start(move, board, team);
			emtmms.add(emtmm);
			threadList.add(emtmm.t);
			if(i==threadCount){
				break;
			}
		}
		
		for(Thread t : threadList){
			try{
				t.join();
			}catch(InterruptedException e){
			}
		}
		HashMap<Move, Integer> output = new HashMap<Move, Integer>();
		for(EngineMultiThreadMinMax emtmm : emtmms){
			HashMap<Move, Integer> moveMap = emtmm.getOutput();
			for(Move move : moveMap.keySet()){
				output.put(move, moveMap.get(move));
				System.out.println(move + "\t" + moveMap.get(move));
			}
		}
		
		Date toc = new Date();
		long time = toc.getTime() - tic.getTime();
		System.out.println("Elapsed Time: " + time + "ms");
	}
}