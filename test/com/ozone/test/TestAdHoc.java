package com.ozone.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH5;
import com.ozone.movements.MoveUtil;
import com.ozone.utility.Converters;

public class TestAdHoc {
	
	@Test
	public void testProbabilityOfMate(){
		Board board = new Board("rnbqkb1r/1p2pppp/p4n2/2p1N3/2B5/2N5/PPPP1PPP/R1BQK2R w KQkq - 0 1");
		int mateCount = 0;
		int totalCount = 0;
		for(Move blackMove : MoveUtil.getAllMoveForTeam(board, -1)){
			totalCount++;
			Board newBoard = blackMove.updateBoard(board);
			for(Move whiteMove : MoveUtil.getAllMoveForTeam(board, 1)){
				if(MoveUtil.isCheckMate(whiteMove.updateBoard(newBoard), -1)){
					mateCount++;
					break;
				}
			}
		}
		System.out.println("Total Mates #:\t" + mateCount);
		System.out.println("Total Moves #:\t" + totalCount);
		double ratio = mateCount/(0.0+totalCount);
		System.out.println("Percentage:\t" + String.format("%.3f", ratio));
	}
	
	public HashMap<Move, List<Integer>> findOptimalMateProbability(Board board0, int team){
		HashMap<Move, List<Integer>> overallMoveMap = new HashMap<Move, List<Integer>>();
		Set<List<Move>> moves = new HashSet<List<Move>>();
		for(Move ownMove : MoveUtil.getAllMoveForTeam(board0, team))
//		Move ownMove = new Move(board0, "H3F1");
		{
			Board board1 = ownMove.updateBoard(board0);
			List<Move> freeCaptures = MoveUtil.getFreeCaptures(board1, -team);
			for(Move oppMove : freeCaptures.size() > 0 ? freeCaptures : MoveUtil.getAllGoodMovesForTeam(board1, -team))
				moves.add(Arrays.asList(ownMove, oppMove));
		}
		
		System.out.println("Analyzing " + moves.size() + " board permutations for " + Converters.boardToFen(board0, team));
		
		for(List<Move> initialMoves: moves){
			Board initialBoard = initialMoves.get(1).updateBoard(initialMoves.get(0).updateBoard(board0));
			if(overallMoveMap.get(initialMoves.get(0)) == null){
				overallMoveMap.put(initialMoves.get(0), new ArrayList<Integer>());
				overallMoveMap.get(initialMoves.get(0)).add(0);
				overallMoveMap.get(initialMoves.get(0)).add(0);
			}
			for(Move firstOwnMove : MoveUtil.getAllMoveForTeam(initialBoard, team)){
				Board board = firstOwnMove.updateBoard(initialBoard);
				int mateCount = 0;
				int totalCount = 0;
				List<Move> freeCaptures = MoveUtil.getFreeCaptures(board, -team);
				List<Move> movesToUse = freeCaptures.size() > 0 ? freeCaptures : MoveUtil.getAllGoodMovesForTeam(board, -team); 
				for(Move oppMove : movesToUse){
					totalCount++;
					Board newBoard = oppMove.updateBoard(board);
					for(Move finalOwnMove : MoveUtil.getAllMoveForTeam(newBoard, team)){
						//TODO: Change isCheckMate to isForcedMate
						if(MoveUtil.isCheckMate(finalOwnMove.updateBoard(newBoard), -team)){
							mateCount++;
//							System.out.println(initialMoves.get(0) + "\t" + initialMoves.get(1) + "\t" + firstOwnMove + "\t" + oppMove + "\t" + finalOwnMove);
							break;
						}
					}
				}
				overallMoveMap.get(initialMoves.get(0)).set(0, overallMoveMap.get(initialMoves.get(0)).get(0) + mateCount);
				overallMoveMap.get(initialMoves.get(0)).set(1, overallMoveMap.get(initialMoves.get(0)).get(1) + totalCount);
			}
		}
		return overallMoveMap;
	}
	
	@Test
	public void findMoveThatLeadsToHighestProbabilityOfMate(){
		Date tic = new Date();
//		HashMap<Move, List<Integer>> overallMoveMap = findOptimalMateProbability(new Board("r1bqkb1r/ppp1pppp/2n2n2/3p4/3P4/2N2N2/PPP1PPPP/R1BQKB1R w KQkq - 0 1"), 1);
		//St Brie vs Legal
		HashMap<Move, List<Integer>> overallMoveMap = findOptimalMateProbability(new Board("r2qkbnr/ppp2ppp/2np4/4p3/2B1P1b1/2N2N2/PPPP1PPP/R1BQK2R w KQkq - 0 1"), 1);
		Date toc = new Date();
		for(Move move : overallMoveMap.keySet()){
			double mateRate = 100.0*overallMoveMap.get(move).get(0)/(0.0+overallMoveMap.get(move).get(1));
			System.out.println(move + "\t" + overallMoveMap.get(move).get(0) + "\t" + overallMoveMap.get(move).get(1) +"\t" + String.format("%.2f", mateRate) + "%");
		}
		long time = toc.getTime()-tic.getTime();
		System.out.println("Ellapsed time: " + time/1000 +"s");
	}

	@Test
	public void isFreeCapture(){
		Board board1 = new Board("rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 1");
		Assert.assertFalse(MoveUtil.isFreeCapture(board1, new Move(board1, "F6E4")));
		Board board2 = new Board("r1bqkb1r/pppp1ppp/2n2n2/4N3/4P3/2N5/PPPP1PPP/R1BQKB1R w KQkq - 0 1");
		Assert.assertTrue(MoveUtil.isFreeCapture(board2, new Move(board2, "C6E5")));
		List<Move> freeCaptures = MoveUtil.getFreeCaptures(new Board("r1bqkb1r/pppp1ppp/2n2n2/4N3/4P3/2N5/PPPP1PPP/R1BQKB1R w KQkq - 0 1"), -1);
		System.out.println(freeCaptures);
		Assert.assertTrue(freeCaptures.size() > 0);
		
	}
	
	@Test
	public void testIsForceMate(){
		Board board = new Board("r1b1qrk1/ppp2p1p/3p1Bp1/2bBp3/3nP3/3P1N2/PPPQ1PPP/R3K2R w KQ - 0 1");
		Move move = new Move(board, "D2H6");
		EngineMinMaxNoMateDectionMAXDEPTH5 e = new EngineMinMaxNoMateDectionMAXDEPTH5(1);
		Move actual= e.findMove(board);
		System.out.println("Expecting: " + move);
		System.out.println("But was: " + actual);
		
//		Assert.assertTrue(AdvancedMoveUtil.isForcedMate(move.updateBoard(board), 1, false));
	}
	
	Comparator<HashMap<Move, List<Integer>>> MoveMateComparator = new Comparator<HashMap<Move, List<Integer>>>(){
		@Override
		public int compare(HashMap<Move, List<Integer>> left, HashMap<Move, List<Integer>> right){
			Move leftKey = MoveUtil.STALE_MATE;
			Move rightKey = MoveUtil.STALE_MATE;
			
			for(Move key : left.keySet())
				leftKey = key;
			for(Move key : right.keySet())
				rightKey = key;
			return Double.compare(
					right.get(rightKey).get(0)/(0.0+right.get(rightKey).get(1)), 
					left.get(leftKey).get(0)/(0.0+left.get(leftKey).get(1))
					);
		}
	};
}
