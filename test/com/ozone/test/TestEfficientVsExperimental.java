package com.ozone.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.common.Common;
import com.ozone.common.Common.GameStatus;
import com.ozone.common.Move;
import com.ozone.common.State;
import com.ozone.engine.Engine;
import com.ozone.engine.EngineMinMaxNoMateDection;
import com.ozone.engine.EngineMinMaxNoMateDectionLevel1200MOBILE;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH1;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH1ForceMHack;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH1SansPickFromBest;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH3;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH5;
import com.ozone.engine.EngineNewFastFast;
import com.ozone.engine.EnginePlus;
import com.ozone.engine.EngineStrong;
import com.ozone.main.EngineSimulation;
import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;

public class TestEfficientVsExperimental {
	int TOTAL = 20;
	int TOTAL_MATCHES_PER_GAME = 1;
	
	enum MatchItem {
		STATUS,
		ITERATION,
		SCORE,
		TIME
	};
	
	
	public static HashMap<MatchItem, Integer> runMatch(Engine e1, Engine e2){
		HashMap<MatchItem, Integer> match = new HashMap<MatchItem, Integer>();
		for(MatchItem mi : MatchItem.values()){
			match.put(mi, 0);
		}
		
		EngineSimulation es = new EngineSimulation();
		Date tic = new Date();
		GameStatus gs = es.start(e1, e2);
		Date toc = new Date();
		int time = (int)((toc.getTime() - tic.getTime()));
		if(gs.equals(GameStatus.BLACK_IS_CHECK_MATE)){
			match.put(MatchItem.STATUS, 1);
		}else if(gs.equals(GameStatus.WHITE_IS_CHECK_MATE)){
			match.put(MatchItem.STATUS, -1);
		}
		
		match.put(MatchItem.ITERATION, es.getMoves());
		match.put(MatchItem.SCORE, es.getScore());
		match.put(MatchItem.TIME, time);
		return match;
	}
	
	@Test
	public void testTournament(){
		
		List<Engine> engines = new ArrayList<Engine>();
		
//		engines.add(new EngineMinMaxNoMateDection(1));
//		engines.add(new EngineNoChanges(1));
//		engines.add(new EngineMobileNew(1));
//		engines.add(new EngineBestOneSoFar(1));
//		engines.add(new EngineNewFastFast(1));
//		engines.add(new EngineMobileClassic(1));
//		engines.add(new EngineNewWithFriedLiverAttack(1));
//		engines.add(new EngineNewWithFriedLiverAttackFast(1));

		Collections.shuffle(engines);
		List<Engine> groupA = engines.subList(0, 4);
		List<Engine> groupB = engines.subList(4, 8);
		List<EnginePlus> resultsGroupA = roundRobin(groupA);
		System.out.println("Group A");
		int i=0;
		for(EnginePlus ep : resultsGroupA){
			i++;
			System.out.println(i + ".\t" + ep.getEngine().getClass().toString().replaceAll(".*\\.", "") + "\t" + ep.getWin() + "\t" + ep.getTie() + "\t" + ep.getLoss() + "\t" + ep.getTime()/6);
		}
		List<EnginePlus> resultsGroupB = roundRobin(groupB);
		System.out.println("Group B");
		i=0;
		for(EnginePlus ep : resultsGroupB){
			i++;
			System.out.println(i + ".\t" + ep.getEngine().getClass().toString().replaceAll(".*\\.", "") + "\t" + ep.getWin() + "\t" + ep.getTie() + "\t" + ep.getLoss() + "\t" + ep.getTime()/6);
		}

		System.out.println("Semi Finals: ");
		System.out.print(resultsGroupA.get(0).getEngine().getClass().toString().replaceAll(".*\\.", "") + "\tvs\t" + resultsGroupB.get(1).getEngine().getClass().toString().replaceAll(".*\\.", "") + ":\t");
		HashMap<MatchItem, Integer> semi1a = runMatch(resultsGroupA.get(0).getEngine(), resultsGroupB.get(1).getEngine());
		System.out.print(printOutputOfMatch(semi1a.get(MatchItem.STATUS)));
		System.out.println(semi1a.get(MatchItem.ITERATION) + "\t" + semi1a.get(MatchItem.SCORE));
		System.out.print(resultsGroupB.get(1).getEngine().getClass().toString().replaceAll(".*\\.", "") + "\tvs\t" + resultsGroupA.get(0).getEngine().getClass().toString().replaceAll(".*\\.", "") + ":\t");
		HashMap<MatchItem, Integer> semi1b = runMatch(resultsGroupB.get(1).getEngine(), resultsGroupA.get(0).getEngine());
		System.out.print(printOutputOfMatch(semi1b.get(MatchItem.STATUS)));
		System.out.println(semi1b.get(MatchItem.ITERATION) + "\t" + semi1b.get(MatchItem.SCORE));
		
		int scoreSemi1 = 
				semi1a.get(MatchItem.STATUS) * iter2Score(semi1a.get(MatchItem.ITERATION)) - 
				semi1b.get(MatchItem.STATUS) * iter2Score(semi1b.get(MatchItem.ITERATION));
		System.out.println("Metric for semi final 1: " + scoreSemi1 + "\t" + semi1a.get(MatchItem.STATUS) + "\t" + iter2Score(semi1a.get(MatchItem.ITERATION)));
		System.out.println("Metric for semi final 1: " + scoreSemi1 + "\t" + semi1b.get(MatchItem.STATUS) + "\t" + iter2Score(semi1b.get(MatchItem.ITERATION)));
		
		Engine finalist1 = scoreSemi1 >= 0 ? resultsGroupA.get(0).getEngine() : resultsGroupB.get(1).getEngine();

		System.out.print(resultsGroupB.get(0).getEngine().getClass().toString().replaceAll(".*\\.", "") + "\tvs\t" + resultsGroupA.get(1).getEngine().getClass().toString().replaceAll(".*\\.", "") + ":\t");
		HashMap<MatchItem, Integer> semi2a = runMatch(resultsGroupB.get(0).getEngine(), resultsGroupA.get(1).getEngine());
		System.out.print(printOutputOfMatch(semi2a.get(MatchItem.STATUS)));
		System.out.println(semi2a.get(MatchItem.ITERATION) + "\t" + semi2a.get(MatchItem.SCORE));
		
		System.out.print(resultsGroupA.get(1).getEngine().getClass().toString().replaceAll(".*\\.", "") + "\tvs\t" + resultsGroupB.get(0).getEngine().getClass().toString().replaceAll(".*\\.", "") + ":\t");
		HashMap<MatchItem, Integer> semi2b = runMatch(resultsGroupA.get(1).getEngine(), resultsGroupB.get(0).getEngine());
		System.out.print(printOutputOfMatch(semi2b.get(MatchItem.STATUS)));
		System.out.println(semi2b.get(MatchItem.ITERATION) + "\t" + semi2b.get(MatchItem.SCORE));

		int scoreSemi2 = 
				semi2a.get(MatchItem.STATUS) * iter2Score(semi2a.get(MatchItem.ITERATION)) - 
				semi2b.get(MatchItem.STATUS) * iter2Score(semi2b.get(MatchItem.ITERATION));
		System.out.println("Metric for semi final 2: " + scoreSemi2 + "\t" + semi2a.get(MatchItem.STATUS) + "\t" + iter2Score(semi2a.get(MatchItem.ITERATION)));
		System.out.println("Metric for semi final 2: " + scoreSemi2 + "\t" + semi2b.get(MatchItem.STATUS) + "\t" + iter2Score(semi2b.get(MatchItem.ITERATION)));
		
		
		Engine finalist2 = scoreSemi2 >= 0 ? resultsGroupB.get(0).getEngine() : resultsGroupA.get(1).getEngine();

		System.out.println("FINAL");
		System.out.print(finalist1.getClass().toString().replaceAll(".*\\.", "") + "\tvs\t" + finalist2.getClass().toString().replaceAll(".*\\.", "") + ":\t");
		HashMap<MatchItem, Integer> final1 = runMatch(finalist1, finalist2);
		System.out.print(printOutputOfMatch(final1.get(MatchItem.STATUS)));
		System.out.println(final1.get(MatchItem.ITERATION) + "\t" + final1.get(MatchItem.SCORE));

		System.out.print(finalist2.getClass().toString().replaceAll(".*\\.", "") + "\tvs\t" + finalist1.getClass().toString().replaceAll(".*\\.", "") + ":\t");
		HashMap<MatchItem, Integer> final2 = runMatch(finalist2, finalist1);
		System.out.print(printOutputOfMatch(final2.get(MatchItem.STATUS)));
		System.out.println(final2.get(MatchItem.ITERATION) + "\t" + final2.get(MatchItem.SCORE));
		
		int finalScore = 
				final1.get(MatchItem.STATUS) * iter2Score(final1.get(MatchItem.ITERATION)) - 
				final2.get(MatchItem.STATUS) * iter2Score(final2.get(MatchItem.ITERATION));
		if(finalScore > 0){
			System.out.println(finalist1.getClass().toString().replaceAll(".*\\.", "") + " WINS!!");
		}else{
			System.out.println(finalist2.getClass().toString().replaceAll(".*\\.", "") + " WINS!!");
		}
	}
	
	private String printOutputOfMatch(int status) {
		if(status > 0){
			return "\tWhite wins\t";
		}else if(status < 0){
			return "\tBlack wins\t";
		}
		return "\tStalemate\t";
	}

	private Integer iter2Score(int iter) {
		int score = (int)Math.round(50*Math.exp(-(iter/50.0)*(iter/50.0))+50.0);
		return score;
	}

	public static List<EnginePlus> roundRobin(List<Engine> engines){
		List<EnginePlus> result = new ArrayList<EnginePlus>();
		
		for(Engine e : engines)
			result.add(new EnginePlus(e));

		EngineSimulation es = new EngineSimulation();
		for(int a = 0;a < engines.size()-1; a++){
			for(int b = a + 1; b< engines.size(); b++){
				for(int k = 0; k<2; k++){
					int i = (k==0) ? a : b;
					int j = (k==0) ? b : a;
										
					Engine e1 = result.get(i).getEngine();
					Engine e2 = result.get(j).getEngine();
					System.out.print(e1.getClass().toString().replaceAll(".*\\.", "") + "\tvs\t" + e2.getClass().toString().replaceAll(".*\\.", "") + ":\t");
					HashMap<MatchItem, Integer> match = runMatch(e1, e2);
					
					result.get(i).setTime(result.get(i).getTime() + match.get(MatchItem.TIME));
					result.get(j).setTime(result.get(j).getTime() + match.get(MatchItem.TIME));
					result.get(i).setScore(es.getMoves());
					result.get(j).setScore(es.getMoves());
					if(match.get(MatchItem.STATUS) > 0){
						result.get(i).setWin(result.get(i).getWin()+1);
						result.get(j).setLoss(result.get(j).getLoss()+1);
						System.out.print("White wins\t");
					}else if(match.get(MatchItem.STATUS) < 0){
						result.get(j).setWin(result.get(j).getWin()+1);
						result.get(i).setLoss(result.get(i).getLoss()+1);
						System.out.print("Blacks wins\t");
					}else{
						result.get(i).setTie(result.get(i).getTie()+1);
						result.get(j).setTie(result.get(j).getTie()+1);
						System.out.print("Stalemate\t");
					}
					System.out.println(es.getScore() + "\t" + es.getMoves() + "\tTime: " + match.get(MatchItem.TIME) + "ms");
				}
			}
		}
		Collections.sort(result, EnginePlusComparator);
		return result;
	}
	
	@Test
	public void testBest(){
		int whiteWinCounts = 0;
		int blackWinCounts = 0;
		List<Engine> engines = new ArrayList<Engine>();
		HashMap<Engine, Long> timeMap = new HashMap<Engine, Long>();
		HashMap<Engine, Integer> scoreMap = new HashMap<Engine, Integer>();
		HashMap<Engine, Integer> winCounts = new HashMap<Engine, Integer>();
		HashMap<Engine, Integer> lossCounts = new HashMap<Engine, Integer>();
		EngineSimulation es = new EngineSimulation();
		
		engines.add(new EngineNewFastFast(1));
		engines.add(new EngineMinMaxNoMateDectionLevel1200MOBILE(1));
		engines.add(new EngineMinMaxNoMateDection(1));
		engines.add(new EngineMinMaxNoMateDectionMAXDEPTH1(1));
		engines.add(new EngineMinMaxNoMateDectionMAXDEPTH3(1));
		engines.add(new EngineMinMaxNoMateDectionMAXDEPTH1ForceMHack(1));
		engines.add(new EngineMinMaxNoMateDectionMAXDEPTH5(1));

		for(Engine engine : engines){
			timeMap.put(engine, 0L);
			scoreMap.put(engine, 0);
			winCounts.put(engine, 0);
			lossCounts.put(engine, 0);
		}
		
		int games = TOTAL_MATCHES_PER_GAME;
		for(int i=0;i<engines.size()-1; i++){
			for(int j=i+1;j<engines.size();j++){
				for(int k=0;k<games;k++){
					Engine e1 = engines.get(i);
					Engine e2 = engines.get(j);
					e1.setTeam(1);
					e2.setTeam(-1);
					System.out.print(e1.getClass().toString().replaceAll(".*\\.", "") + "\tvs\t" + e2.getClass().toString().replaceAll(".*\\.", "") + ":\t");
					Date tic = new Date();
					GameStatus status1 = es.start(e1, e2);
					if(status1.equals(GameStatus.BLACK_IS_CHECK_MATE)){
						System.out.print("White wins\t");
						whiteWinCounts++;
						scoreMap.put(e1, scoreMap.get(e1) + 3);
						winCounts.put(e1, winCounts.get(e1) + 1);
						lossCounts.put(e2, lossCounts.get(e2) + 1);
					}else if(status1.equals(GameStatus.WHITE_IS_CHECK_MATE)){
						System.out.print("Black wins\t");
						blackWinCounts++;
						scoreMap.put(e2, scoreMap.get(e2) + 4);
						winCounts.put(e2, winCounts.get(e2) + 1);
						lossCounts.put(e1, lossCounts.get(e1) + 1);
					}else{
						System.out.print("Stalemate\t");
						scoreMap.put(e1, scoreMap.get(e1) + 1);
						scoreMap.put(e2, scoreMap.get(e2) + 2);
					}
					Date toc = new Date();
					timeMap.put(e1, timeMap.get(e1) + (toc.getTime() - tic.getTime()));
					timeMap.put(e2, timeMap.get(e2) + (toc.getTime() - tic.getTime()));
					System.out.println(es.getScore() + "\t" + es.getMoves() + "\tTime: " + (toc.getTime() - tic.getTime()) + "ms");
					
					e1.setTeam(-1);
					e2.setTeam(1);
					System.out.print(e2.getClass().toString().replaceAll(".*\\.", "") + "\tvs\t" + e1.getClass().toString().replaceAll(".*\\.", "") + ":\t");
					Date ticc = new Date();
					GameStatus status2 = es.start(e2, e1);
					if(status2.equals(GameStatus.BLACK_IS_CHECK_MATE)){
						System.out.print("White wins\t");
						whiteWinCounts++;
						scoreMap.put(e2, scoreMap.get(e2) + 3);
						winCounts.put(e2, winCounts.get(e2) + 1);
						lossCounts.put(e1, lossCounts.get(e1) + 1);
					}else if(status2.equals(GameStatus.WHITE_IS_CHECK_MATE)){
						System.out.print("Black wins\t");
						blackWinCounts++;
						scoreMap.put(e1, scoreMap.get(e1) + 4);
						winCounts.put(e1, winCounts.get(e1) + 1);
						lossCounts.put(e2, lossCounts.get(e2) + 1);
					}else{
						System.out.print("Stalemate\t");
						scoreMap.put(e2, scoreMap.get(e2) + 1);
						scoreMap.put(e1, scoreMap.get(e1) + 2);
					}
					Date tocc = new Date();
					timeMap.put(e1, timeMap.get(e1) + (tocc.getTime() - ticc.getTime()));
					timeMap.put(e2, timeMap.get(e2) + (tocc.getTime() - ticc.getTime()));
					System.out.println(es.getScore() + "\t" + es.getMoves() + "\tTime: " + (tocc.getTime() - ticc.getTime()) + "ms");
				}
			}
			System.out.println("Ranking for Round " + i);
			rankTeams(scoreMap, engines, winCounts, lossCounts, timeMap);
		}
		System.out.println("Final Ranking");
		rankTeams(scoreMap, engines, winCounts, lossCounts, timeMap);
		System.out.println("White Wins: " + whiteWinCounts);
		System.out.println("Black Wins: " + blackWinCounts);
	}
	
	public final static Comparator<EnginePlus> EnginePlusComparator = new Comparator<EnginePlus>() {
		@Override
		public int compare(EnginePlus left, EnginePlus right) {
			int turnsLeft = left.getScore();
			int turnsRight = right.getScore();
			int winLeft = left.getWin();
			int winRight = right.getWin();
			int tieLeft = left.getTie();
			int tieRight = right.getTie();
			long timeLeft = left.getTime();
			long timeRight = right.getTime();
			
			if(winLeft == winRight){
				if(tieLeft == tieRight){
					if(timeLeft == timeRight){
						return Integer.valueOf(turnsLeft).compareTo(Integer.valueOf(turnsRight));
					}
					return Double.valueOf(timeLeft).compareTo(Double.valueOf(timeRight));	
				}
				return Integer.valueOf(tieRight).compareTo(Integer.valueOf(tieLeft));
			}
			return Integer.valueOf(winRight).compareTo(Integer.valueOf(winLeft));
		}
	};
	
	public final Comparator<String> EngineComparator = new Comparator<String>() {
		@Override
		public int compare(String left, String right) {
			String[] leftSplit = left.split("@");
			String[] rightSplit = right.split("@");
			int scoreLeft = Integer.valueOf(leftSplit[1]);
			int scoreRight = Integer.valueOf(rightSplit[1]);
			int winLeft = Integer.valueOf(leftSplit[2]);
			int winRight = Integer.valueOf(rightSplit[2]);
			int tieLeft = Integer.valueOf(leftSplit[3]);
			int tieRight = Integer.valueOf(rightSplit[3]);
			Double timeLeft = Double.valueOf(leftSplit[5]);
			Double timeRight = Double.valueOf(rightSplit[5]);
			if(winLeft == winRight){
				if(tieLeft == tieRight){
					if(timeLeft == timeRight){
						return Integer.valueOf(scoreRight).compareTo(Integer.valueOf(scoreLeft));
					}
					return Double.valueOf(timeLeft).compareTo(Double.valueOf(timeRight));	
				}
				return Integer.valueOf(tieRight).compareTo(Integer.valueOf(tieLeft));
			}
			return Integer.valueOf(winRight).compareTo(Integer.valueOf(winLeft));
		}
	};
	
	
	@Test
	public void testFastSimulation(){
		Board initialBoard = null;
		int sum = 0;
		int sumWhite = 0;
		int sumBlack = 0;
		long avgTime = 0;
		double avgTimeWinWhite = 0.0;
		double avgTimeWinBlack = 0.0;
		double avgMovesWinWhite = 0.0;
		double avgMovesWinBlack = 0.0;
		double wMetric = 0.0;
		double bMetric = 0.0;
		for(int i=0;i<TOTAL;i++){
			int black = 0;
			int white = 0;
			int tie = 0;
			
			EngineSimulation es = new EngineSimulation(); 
			Date tic = new Date();
			Engine e1 = new EngineMinMaxNoMateDectionMAXDEPTH1SansPickFromBest(1);
			Engine e2 = new EngineMinMaxNoMateDectionMAXDEPTH1(-1);
			GameStatus status = es.start(e1, e2, false, false, initialBoard);
			long time = new Date().getTime() - tic.getTime();
			if(status == GameStatus.BLACK_IS_CHECK_MATE) {
				white=1;
				sumWhite++;
				avgMovesWinWhite = avgMovesWinWhite + es.getMoves();
				avgTimeWinWhite = avgTimeWinWhite + time;
				wMetric = wMetric + 0.5*Math.exp(-(es.getMoves()/50.0)*(es.getMoves()/50.0)) + 0.5;
			}
			if(status == GameStatus.WHITE_IS_CHECK_MATE) {
				black=1;
				sumBlack++;
				avgMovesWinBlack = avgMovesWinBlack + es.getMoves();
				avgTimeWinBlack = avgTimeWinBlack + time;
				bMetric = bMetric + 0.5*Math.exp(-(es.getMoves()/50.0)*(es.getMoves()/50.0)) + 0.5;
			}
			if(status.toString().startsWith("STALE_MATE")){
				tie=1;
			}
			sum=sum+white-black;
			
			avgTime = avgTime + time;
			System.out.println((i+1) +")\t" + white + "\t" + black + "\t" + tie + "\t" + time + "\t" + es.getMoves() + "\t" + es.getScore());
		}
		avgTime = avgTime/TOTAL;
		System.out.println(sum + "\t" + String.format("%.1f", (100*sum)/(double)TOTAL) + "%\t" + avgTime/1000 + "spm\tTotal Time: " + avgTime*TOTAL +"s");
		System.out.println("W: " + String.format("%.1f", avgTimeWinWhite/(1000.0*sumWhite)) + "\t" + String.format("%.0f", avgMovesWinWhite/((double)sumWhite)) + "\t" + wMetric);
		System.out.println("B: " + String.format("%.1f", avgTimeWinBlack/(1000.0*sumBlack)) + "\t" + String.format("%.0f", avgMovesWinBlack/((double)sumBlack)) + "\t" + bMetric);
	}
	
	@Test
	public void findBlunder(){
		//Standard engine, TODO: create a standard engine
		Engine e1 = new EngineStrong(1);
		//Engine to test blunder against
		Engine e2 = new EngineMinMaxNoMateDectionMAXDEPTH5(-1);
		
		EngineSimulation es = new EngineSimulation();
		State state = null;
		while(state == null)
			state = es.findBlunder(e1, e2);

		System.out.println(state.getMoveHistory());
		System.out.println(state.getFen());
	}
	
	@Test
	public void testEngineSimulation(){
		Engine e1 = new EngineNewFastFast(1);
		Engine e2 = new EngineMinMaxNoMateDectionMAXDEPTH5(-1);
		EngineSimulation es = new EngineSimulation();
		GameStatus gs = es.start(e1, e2, true, true, null);
		assertNotNull(gs);
	}
	
	@Test
	public void testSimulation(){
		Date tic = new Date();
		Engine e1 = new EngineMinMaxNoMateDection(1);
		Engine e2 = new EngineNewFastFast(-1);
		
		Board board = new Board();
		List<Move> moveHistory = new ArrayList<Move>();
		GameStatus status = GameStatus.ON_GOING;
		board.reset();
		int i=0;
		int turn = 1;
		int isNoConsecutiveCaptureOrPawnMovement = 0;
		while(status != GameStatus.WHITE_IS_CHECK_MATE && !status.toString().startsWith("STALE_MATE")){
			Move whiteMove = e1.findMove(board, i, false, true, moveHistory);
			System.out.println("White move:\t" + whiteMove);
			moveHistory.add(whiteMove);
			
			if(!MoveUtil.isCaptureOrPawnMovement(board, whiteMove)){
				isNoConsecutiveCaptureOrPawnMovement++;
			}else{
				isNoConsecutiveCaptureOrPawnMovement = 0;
			}
			board.movePiece(whiteMove);
			BoardUtil.displayBoard(board);
			System.out.println("T: " + i + "\t" + whiteMove);
			turn = -1*turn;
			status = Common.getGameStatus(board, moveHistory, turn, isNoConsecutiveCaptureOrPawnMovement);
			
			if(status != GameStatus.ON_GOING){
				break;
			}
			
			Move blackMove = e2.findMove(board, i, false, true, moveHistory);
			System.out.println("Black move:\t" + blackMove);
			moveHistory.add(blackMove);
			
			if(!MoveUtil.isCaptureOrPawnMovement(board, blackMove)){
				isNoConsecutiveCaptureOrPawnMovement++;
			}else{
				isNoConsecutiveCaptureOrPawnMovement = 0;
			}
			board.movePiece(blackMove);
			BoardUtil.displayBoard(board);
			System.out.println("T: " + i + "\t" + blackMove);
			turn = -1*turn;
			status = Common.getGameStatus(board, moveHistory, turn, isNoConsecutiveCaptureOrPawnMovement);
			i++;
		}
		if(isNoConsecutiveCaptureOrPawnMovement >= 100){
			System.out.println("Stalemate by no pawn movement or capture");
		}else{
			System.out.println(status);
		}
		System.out.println("Time: " + ((new Date().getTime())-tic.getTime()) + "ms");
		assertEquals(GameStatus.BLACK_IS_CHECK_MATE, status);
	}
	
	public void rankTeams(HashMap<Engine, Integer> scoreMap, List<Engine> engines, HashMap<Engine, Integer> winCounts, HashMap<Engine, Integer> lossCounts, HashMap<Engine, Long> timeMap){

		List<String> enginesWithScore = new ArrayList<String>();
		for(Engine engine : scoreMap.keySet()){
			int ties = 2*TOTAL_MATCHES_PER_GAME*(engines.size()-1) - winCounts.get(engine) - lossCounts.get(engine);
			enginesWithScore.add(engine.getClass().toString().replaceAll(".*\\.", "") + "@" + scoreMap.get(engine) +"@" + winCounts.get(engine) + "@" + ties + "@" + lossCounts.get(engine) + "@" + String.format("%.3f",timeMap.get(engine)/(2000.0*TOTAL_MATCHES_PER_GAME*engines.size()-2000.0*TOTAL_MATCHES_PER_GAME)));
		}
		
		System.out.println("-------------------------------------");
		Collections.sort(enginesWithScore, EngineComparator);
		int rank = 0;
		for(String engineWithScore : enginesWithScore){
			rank++;
			
			System.out.println(rank + ")\t" + engineWithScore.replaceAll("@", "\t"));
			if(rank == 3){
				System.out.println("-------------------------------------");
			}
		}
	}
}