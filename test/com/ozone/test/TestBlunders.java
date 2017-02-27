package com.ozone.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Set;

import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.common.Piece;
import com.ozone.engine.Engine;
import com.ozone.engine.EngineMinMaxNoMateDection;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH5;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH7;
import com.ozone.engine.EngineNewFastFast;
import com.ozone.movements.AdvancedMoveUtil;
import com.ozone.movements.BoardUtil;
import com.ozone.movements.MoveUtil;
import com.ozone.movements.MoveUtil.Status;

public class TestBlunders {
	
	EngineNewFastFast ew = new EngineNewFastFast(1);
	EngineNewFastFast eb = new EngineNewFastFast(-1);

	@Test
	public void testMateDetection() {
//		Engine e = new EngineMinMaxNoMateDectionLevel1000MOBILE_REALCOPY(-1);
		Engine e = new EngineMinMaxNoMateDectionMAXDEPTH7(-1);
		Board board = new Board("r4r1k/ppp2p1p/5p2/2q1pN2/2Pn2Q1/8/P1P2PPP/R3R1K1 b - - 0 1");
		Move move = e.findMove(board);
		System.out.println(move);
		System.out.println(AdvancedMoveUtil.isHisNextMovePuttingMeToCheckMate(move.updateBoard(board), -1));
	}
	
	@Test
	public void testCaptureOfFreePawnsLeadsToForkOfQueen() {
		Board board = new Board("rr4k1/1pp2pq1/p1nb3p/3N1p2/2PPpPP1/4P3/2Q1N2P/1R2KR2 b - - 0 1");
		EngineMinMaxNoMateDection e = new EngineMinMaxNoMateDection(-1);
		Move move = e.findMove(board);
		System.out.println(move);
	}
	
	@Test
	public void testPreventFutureFork() {
		Board board = new Board("5rk1/1pp1q2p/1bn1b3/1R3p2/4p1p1/2Q1P3/4BNPP/r1B2R1K b - - 0 1");
		Move moveLeadsToFutureFork = new Move(board, "E7G7");
		EngineMinMaxNoMateDectionMAXDEPTH5 e = new EngineMinMaxNoMateDectionMAXDEPTH5(-1);
		System.out.println("Initial state of board: " + EngineMinMaxNoMateDection.heuristic(board, -1));
		Move idealMove = new Move(board, "E7F6");
		Move move = e.findMove(board);
		System.out.println("Engine did this: " + move);
		assertNotEquals(move, moveLeadsToFutureFork);
		assertEquals(idealMove, move);
	}
	
	@Test
	public void testProtectQueenInsteadOfCapture() {
		Board board = new Board("r1bqk2r/pppp1ppp/2nn4/3Qp1B1/8/2P2N2/PPP2PPP/R4RK1 b kq - 0 1");
		Engine e = new EngineMinMaxNoMateDection(-1);
		Move expected = new Move(board, "f7f6");
		Move actualMove = e.findMove(board);
		System.out.println(actualMove);
		assertEquals(expected, actualMove);
	}
	
	@Test
	public void testKnightBishopForPawnsBlunder(){
		Board board = new Board("r1bq1rk1/1pp2pp1/p1nbp3/3p2Np/3PPPn1/P1N3P1/1PP1B2P/R1BQ1RK1 b KQkq - 0 1");
		Move thisMoveLeadsToFork = new Move(-BoardUtil.KNIGHT, "g4f6");
		Move move = eb.findMove(board);
		assertFalse("This move leads to a fork", thisMoveLeadsToFork.equals(move));	}
	
	@Test
	public void testNotTradingRooksWasABlunderLeadToAMateThreeMovesLaterBlackWasUpSoItShouldHaveTraded(){
		Board board = new Board("rqb3k1/1p4pp/p1p2r2/6Q1/1P6/P1P5/5RP1/R5K1 b KQkq - 0 1");
		Move move = eb.findMove(board);
		Move tradeMove = new Move(-BoardUtil.ROOK, "F6F2");
		assertEquals(tradeMove, move);
	}
	
	@Test
	public void testEarlyQueenMovement(){
		Board board = new Board("r1bqkbnr/pppp2pp/2n5/5p2/3PPp2/2N3P1/PPP4P/R1BQKBNR b KQkq - 0 1");
		Move move = eb.findMove(board, 3, true, true, new ArrayList<Move>()); 
		System.out.println(move);
		assertFalse("Too early to move the queen", -BoardUtil.QUEEN == move.getPieceMoving());
	}
	
	@Test
	public void testPromotePawnWithSurpriseCheckIsANoBrainer(){
		Board board = new Board("2k2r2/8/7P/8/2n2P2/r3p1P1/3qp2K/1R6 b - - 0 1");
		Move move = eb.findMove(board, 55, true, true, new ArrayList<Move>());
		Move expectedMove = new Move(-BoardUtil.PAWN, "E2E1");
		assertEquals("The PAWN MUST PROMOTE!! SURPRISE CHECK!", expectedMove, move);
	}
	
	@Test
	public void testIsSurpriseCheckOfCourseItIs(){
		Board board = new Board("2k2r2/8/7P/8/2n2P2/r3p1P1/3qp2K/1R6 b - - 0 1");
		Move expectedMove = new Move(-BoardUtil.PAWN, "E2E1");
		Board newBoard = expectedMove.updateBoard(board);
		assertTrue(MoveUtil.isSurpriseCheck(newBoard, expectedMove));
	}
	
	@Test
	public void testWhyOnEarthWouldTheKnightCaptureThePawnThatisProtectedByTheBishop(){
		Board board = new Board("r3k2r/1pp2ppn/pbn5/3N2Pp/BP1P4/P3B2P/2P2P2/R3K2R b KQkq - 0 1");
		Move move = eb.findMove(board);
		assertFalse(new Move(-BoardUtil.KNIGHT, "h7g5").equals(move));
		Move castle = new Move(-BoardUtil.KING, "E8C8");
		assertEquals(castle, move);
		Set<Status> statuses = eb.getStatusesForMove(board, castle);
		System.out.println(statuses);
	}
	
	@Test
	public void stopFromPromoting(){
		Board board = new Board("8/2P2bk1/7p/7P/5p2/1r6/2B4R/6K1 b KQkq - 0 1");
		Move stopFromPromotingMove = new Move(-BoardUtil.ROOK, "b3c3");
		Move actualMove = new Move(-BoardUtil.ROOK, "b3b8");
		Set<Status> statuses = eb.getStatusesForMove(board, actualMove);
		System.out.println(statuses);
		Move move = eb.findMove(board);
		assertEquals(stopFromPromotingMove, move);
	}
	
	@Test
	public void testNextMoveLeadsToNoMovesLeftExceptForCapture(){
		Board board = new Board("rn4k1/1p3p1p/2ppr1p1/p7/5BB1/8/PPP2PPP/4RK1R b - - 0 1");
		Move move = eb.findMove(board);
		assertEquals(new Move(-BoardUtil.ROOK, "e6e1"), move);
	}
	
	@Test
	public void givingUpTheQueen(){
		Board board = new Board("r1bqk2r/5p2/2N1p1p1/p2p2n1/P1pP4/B3P1Pp/4QP1P/RN3RK1 b KQkq - 0 1");
		Move move = eb.findMove(board);
		Move expectedMove = new Move(-BoardUtil.QUEEN, "d8c7");
		Set<Status> statuses = eb.getStatusesForMove(board, expectedMove);
		System.out.println(statuses);
		assertTrue(move.toString().matches(".*D8[BCD][67]"));
	}
	
	@Test
	public void testWhyQueenMovedTowardsRook(){
		Board board = new Board("r1q2rk1/npp1np2/p2p2bp/3P2p1/B3P1P1/2P2N1P/P1P1QB2/R3K1R1 b Q - 0 1");
		Move move = eb.findMove(board);
		System.out.println(move);
	}
	
	@Test
	public void isOneOfMyPieceThreatened(){
		Board board = new Board("r1q2rk1/np2np2/p1pp2bp/3P2p1/B3P1P1/2P2N1P/P1P1QB2/R3K1R1 b Q - 0 1");
		for(Piece piece : BoardUtil.getAllTeamPieces(board, -1)){
			assertFalse(MoveUtil.isPieceThreatenedNew(board, piece));	
		}	
	}
	
	@Test
	public void blunderingBugsWithIsBatteryItsNotaBattery(){
		Board board = new Board("r1q2rk1/np2np2/p1pp2bp/3P2p1/B3P1P1/2P2N1P/P1P1QB2/R3K1R1 b Q - 0 1");
		assertFalse(MoveUtil.isBattery(board, new Piece(BoardUtil.BISHOP, 1,5), new Piece(-BoardUtil.KNIGHT, 6, 0)));
	}
	
	@Test
	public void leftYourselfForkableAfterCheck(){
		Board board = new Board("1rb1kq2/7p/pbpP4/5P2/4Qnr1/P1N2N2/5P1P/R1B1K2R b KQ - 0 1");
		Move move = eb.findMove(board);
		Move forkedMove = new Move(-BoardUtil.KING, "e8f7");
		Move goodMove = new Move(-BoardUtil.KING, "e8d8");
		Set<Status> statuses = eb.getStatusesForMove(board, forkedMove);
		System.out.println(statuses);
		assertFalse(forkedMove.equals(move));
		assertEquals(goodMove, move);
	}
	
	@Test
	public void sacrificeKnightToCaptureQueen(){
		Board board =  new Board("1rbk1q2/7p/pbpP4/4NP2/4Qnr1/P1N5/5P1P/R1B1K2R b KQ - 0 1");
		assertFalse(AdvancedMoveUtil.isForcedMate(new Move(board, "f4d3").updateBoard(board), -1, 0, false));
		Move move = eb.findMove(board);
		//Not immediately obvious. Needs depth search to find this one.
		assertEquals(new Move(-BoardUtil.KNIGHT, "f4g2"), move);
	}
	
	@Test
	public void testAddReinforcementInsteadOfCaptureLeadToPromotion(){
		Board board = new Board("r1k1rR2/4P3/bp6/2p2P2/3p4/P7/1P4PP/RB4K1 b KQkq - 0 1");
		BoardUtil.displayBoard(board);
		Move move = eb.findMove(board);
		Move actual = new Move(-BoardUtil.ROOK, "e8f8");
		Move expected = new Move(-BoardUtil.KING, "C8D7");
		Set<Status> statuses = eb.getStatusesForMove(board, actual);
		assertTrue(statuses.contains(Status.PAWN_PROMOTION_OPP));
		Set<Status> statusess = eb.getStatusesForMove(board, expected);
		assertTrue(statusess.contains(Status.ADDING_THREATS));
		assertEquals(expected, move);
	}
	
	@Test
	public void testThreatCounter(){
		Board board = new Board("r1k1rR2/4P3/bp6/2p2P2/3p4/P7/1P4PP/RB4K1 b KQkq - 0 1");
		Move expected = new Move(-BoardUtil.KING, "C8D7");
		assertEquals(1,MoveUtil.findAttackPieces(expected.updateBoard(board), new Piece(-BoardUtil.KING, 6, 3)).size());
	}
	
	@Test
	public void testWrongThreatCounter(){
		Board board = new Board("r1k1rR2/4P3/bp6/2p2P2/3p4/P7/1P4PP/RB4K1 b KQkq - 0 1");
		Move expected = new Move(-BoardUtil.KING, "C8C7");
		assertEquals(0,MoveUtil.findAttackPieces(expected.updateBoard(board), new Piece(-BoardUtil.KING, 6, 2)).size());
	}
	
	@Test
	public void bishopInDangerByPawnSoMoveUnprotectingPawnYeahThatMakesSense(){
		Board board = new Board("rn1qkb1r/pp3ppp/5n2/2pp4/3P2b1/3BPN1P/PP3PP1/RNBQK2R b KQkq - 0 1");
		Move move = eb.findMove(board);
		Move actual = new Move(-BoardUtil.PAWN, "g7g5");
		assertFalse(actual.equals(move));
	}
	
	@Test
	public void thisOneIsABitMoreSubbtleBishopMovesToProtectionButPawnIsAlreadyProtectingKnight(){
		Board board = new Board("r2q1rk1/ppp2pp1/3p2np/8/3NP1b1/2P3Q1/P1P2PPP/R3KB1R b KQkq - 0 1");
		Move move = eb.findMove(board);
		Move actual = new Move(-BoardUtil.BISHOP, "g4e6");
		Move expected = new Move(-BoardUtil.BISHOP, "g4d7");
		Set<Status> statuses = eb.getStatusesForMove(board, actual);
		assertFalse(statuses.contains(Status.PROTECTED));
		assertFalse(actual.equals(move));
		assertEquals(expected, move);
	}
	
	@Test
	public void testIsThreatenedWhenPieceIsMovingToAPlaceProtectedByAPieceButThatPieceIsProtectingAPieceThatisThreatened(){
		Board board = new Board("r2q1rk1/ppp2pp1/3p2np/8/3NP1b1/2P3Q1/P1P2PPP/R3KB1R b KQkq - 0 1");
		Move actual = new Move(-BoardUtil.BISHOP, "g4e6");
		int[] piece = new int[]{-BoardUtil.BISHOP, 5,4};

		assertTrue(MoveUtil.isPieceThreatened(actual.updateBoard(board), piece));
	}
	
	@Test
	public void wellThatIcantExplainRookTakesPawnBarelyProtectedWhileQueenCouldHaveBeenTraded(){
		Board board = new Board("r1q4k/ppp1b3/1nn1Q3/3P1r2/8/2N1P1P1/PPP2P1P/R1B2RK1 b - - 0 1");
		Move move = eb.findMove(board);
		Move actual = new Move(-BoardUtil.ROOK, "f5d5");
		assertFalse(actual.equals(move));
		assertEquals(new Move(-BoardUtil.QUEEN, "c8e6"), move);
	}
	
	@Test
	public void testIsThreatenedWhenTwoBattlesAreGoingOn(){
		Board board = new Board("r1q4k/ppp1b3/1nn1Q3/3P1r2/8/2N1P1P1/PPP2P1P/R1B2RK1 b - - 0 1");
		Move actual = new Move(-BoardUtil.ROOK, "f5d5");
		assertTrue(MoveUtil.isPieceThreatened(actual.updateBoard(board), new int[]{-BoardUtil.ROOK, 4,3}));
	}
	
	@Test
	public void testFriendlyThreatToBishopAndHeTakesSacrificingItselfSoStupid(){
		Board board = new Board("r3kb1r/ppp2ppp/2n2q2/3n4/2B1P1b1/5N1P/PPP2PP1/RNBQK2R w KQkq - 0 1");
		Move move = eb.findMove(board);
		Move actual = new Move(-BoardUtil.BISHOP, "g4h3");
		assertFalse(actual.equals(move));
		assertFalse(MoveUtil.isPieceThreatened(move.updateBoard(board), -BoardUtil.BISHOP, move.getToPos()));
	}	
	
	@Test
	public void prettyObviousBlunderThereMoveUpTheUnprotectedPawnDontAttack(){
		Board board = new Board("r2qk1nr/2p1bppp/8/3pP3/3p2Q1/7P/PPPP1PP1/R1B1R1K1 b KQkq - 0 1");
		BoardUtil.displayBoard(board);
		Move move = eb.findMove(board);
		Move actual = new Move(board, "h7h5");
		Move exp = new Move(board, "g7g6");
		assertFalse(actual.equals(move));
		assertEquals(exp, move);
	}
	
	@Test
	public void hadAHugeAdvantageButSomehowEndedUpLosingBecauseOfSurpriseCheckAndPawnPromotionNeededToClearTheWayForTheRookPart2(){
		Board board = new Board("2k3nr/ppp3pp/3rPp2/8/8/2P4B/P1P1K2P/6NR b - - 0 1");
		BoardUtil.displayBoard(board);
		Move move = eb.findMove(board);
		Move actual = new Move(board, "d6a6");
		Move exp = new Move(board, "g8e7");
		Set<Status> statuses = eb.getStatusesForMove(board, actual);
		assertTrue(statuses.contains(Status.SURPRISE_CHECKS_OPP));
		assertEquals(exp, move);
	}
	
	@Test
	public void testAvoidTradingWhenItInvolvedStackedPawns(){
		Board board = new Board("2kr1b1r/pp4pp/1q1p1p2/8/8/3QB2P/PPP2PP1/R3K2R b KQ - 0 1");
		Move move = eb.findMove(board);
		assertFalse(new Move(board, "b6a6").equals(move));
	}
	
	@Test
	public void testWhat(){
		Board board = new Board("r4rk1/1b3p2/p1p1pB1p/qp1p4/3P4/1BP1PQ2/P1PK1Pp1/R5R1 b KQkq - 0 1");
		BoardUtil.displayBoard(board);
		Move move = eb.findMove(board);
		Move actual = new Move(board, "a5a3");
		assertFalse(actual.equals(move));
	}
	
	
	//Not sure how to solve that one without minmax
//	@Test
//	public void whyGiveUpTheKnight(){
//		Board board = new Board("r3k2r/1b3p2/p1p1pn1p/qp1pB3/3P4/1BP1PQ2/P1PK1Pp1/R5R1 b KQkq - 0 1");
//		Move exp = new Move(board, "f6e4");
//		Set<Status> statuses = eb.getStatusesForMove(board, exp);
//
//		Move move = eb.findMove(board);
//		Move actual = new Move(board, "e8g8");
//		assertTrue(statuses.contains(Status.AVOID_THREAT));
//		assertFalse(actual.equals(move));
//	}
	
	@Test
	public void notPreventingPawnPromotion(){
		Board board = new Board("b7/5k2/P6P/5p2/8/6P1/3B3K/8 b KQkq - 0 1");
		Move move = eb.findMove(board);
		Move actual = new Move(board, "f7e6");
		Move exp = new Move(board, "f7g6");
		Set<Status> statuses = eb.getStatusesForMove(board, exp);
		assertFalse(statuses.contains(Status.PAWN_PROMOTION_THREAT));
		assertFalse(actual.equals(move));
	}
	
	@Test
	public void doNotCapturePawnAlreadyIsolated(){
		Board board = new Board("r1bqkb1r/1pp2ppp/p1n1pn2/3pN1B1/3P4/2N5/PPP1PPPP/R2QKB1R b KQkq - 0 1");
		Move actual = new Move(-BoardUtil.KNIGHT, "c6e5");
		Set<Status> statuses = eb.getStatusesForMove(board, actual);
		assertTrue(statuses.contains(Status.PINS_THREAT_IS_REAL));
		Move move = eb.findMove(board);
		Move exp = new Move(board, "h7h6");
		statuses = eb.getStatusesForMove(board, exp);
		assertFalse(statuses.contains(Status.PINS_THREAT_IS_REAL));
		assertFalse(actual.equals(move));
	}
	
	@Test
	public void nextMoveFromOpponentResultsInThreatToPinnedPieceFromLowerPiece(){
		Board board = new Board("r1bqkb1r/1pp2ppp/p3pn2/3pP1B1/8/2N5/PPP1PPPP/R2QKB1R b KQkq - 0 1");
		assertTrue(MoveUtil.isPieceThreatenedNew(board, new Piece(board, "f6")));
		assertTrue(AdvancedMoveUtil.isPiecePinned(board, new Piece(board, "f6")));
	}
	
	@Test
	public void yourKnightWasCapturedWhyNotTakeHisKnightWithYourPawn(){
		Board board = new Board("r1bqkb1r/ppp2ppp/2N2n2/3p4/4P3/5P2/PPP3PP/RNBQKB1R b KQkq - 0 1");
		BoardUtil.displayBoard(board);
		Move move = eb.findMove(board);
		Move exp = new Move(board, "b7c6");
		assertEquals(exp, move);
	}
	
	@Test
	public void testAvoidPinningPosition(){
		/*
		 * Avoid positioning yourself in a way that puts your self in a pinning position. Rook Rook diagonal attacked by bishop
		 */
		Board board = new Board("r5k1/pp2r1pp/8/2P1p3/8/2P5/P2BnPKP/R4R2 b KQkq - 0 1");
		Move actual = new Move(board, "a8d8");
		Move pin = new Move(BoardUtil.BISHOP, "d2g5");
		Board newBoard = actual.updateBoard(board);
		BoardUtil.displayBoard(pin.updateBoard(newBoard));
		assertTrue(AdvancedMoveUtil.isPiecePinned(pin.updateBoard(newBoard), new Piece(pin.updateBoard(newBoard), "e7")));
		Set<Status> statuses = eb.getStatusesForMove(board, actual);
		assertTrue(statuses.contains(Status.PINS_THREAT_IS_REAL));
		Move move = eb.findMove(board);
		assertFalse(actual.equals(move));
		Move move2 = ew.findMove(newBoard);
		assertTrue(pin.equals(move2));
	}
	
	
	@Test
	public  void testNotSeeingOpportunitiesToFork(){
		/*
		 * In this situation if forked, white could potentially lose the queen
		 * Also, make sure that black sees this i.e. knight checks, white moves queen, black takes queen.
		 * You could use a pseudo minimax removing any moves that doesn't lead to a capture, checks, threats, or threat removal.
		 */
		Board doNotForkBoard = new Board("r1b1kbn1/pppppppp/2q3r1/8/1n6/3N4/PPPPPPPP/R1BQKBNR w KQq - 0 1");
		Move forkButShouldnt = new Move(doNotForkBoard, "d3e5");
		assertFalse(forkButShouldnt.equals(ew.findMove(doNotForkBoard)));
		Board forkBoard = new Board("rn2kbn1/pppppppp/2q3r1/8/1b6/2P2N2/PP1PPPPP/RNBQKB1R w KQq - 0 1");
		assertTrue(new Move(forkBoard, "f3e5").equals(ew.findMove(forkBoard)));
	}
	
	@Test
	public void testNotProtectingBishop(){
		Board board = new Board("2kr3r/ppp1bppp/5n2/3P4/8/2N5/PPPP1PPP/R1B1R1K1 b KQkq - 0 1");
		BoardUtil.displayBoard(board);
		Move move = eb.findMove(board);
		Move badMove = new Move(board, "f6d5");
		Move exp = new Move(board, "h8e8");
		assertFalse(badMove.equals(move));
		assertEquals(exp, move);
	}
	
	@Test
	public void testNMovingIntoHarmsWayWhileQIsThreatened(){
		Board board = new Board("2b2rk1/r1p2ppp/p1nqp3/3p4/3P4/BNP1PPP1/2P1Q2P/R4RK1 b KQkq - 0 1");
		BoardUtil.displayBoard(board);
		assertTrue(AdvancedMoveUtil.findAllPinnedPiecesFromAbsolutePin(board, 1).size()>0);
		
		Move move = eb.findMove(board);
		
		Move actual = new Move(board, "c6b4");
		assertTrue(MoveUtil.isPieceThreatenedNew(actual.updateBoard(board), new Piece(actual.updateBoard(board), "B4")));
		assertFalse(actual.equals(move));
		assertTrue(move.toString().matches("kQ: D6D."));
	}
	
	@Test
	public void testNowhereToGo(){
		Board board = new Board("8/3n1p1r/1pk3p1/2p4p/2P5/NR3PP1/Pn3KP1/4B3 b KQkq - 0 1");
		Move bestMove = new Move(board, "D7E5");
		assertEquals(new Move(BoardUtil.QUEEN, "D3H3"), ew.findMove(new Board("7k/1r6/4r3/8/8/P2Q4/1P6/K7 w - - 0 1")));
		Set<Status> statuses = eb.getStatusesForMove(board, bestMove);
		assertFalse(statuses.contains(Status.CURRENT_FORK_LOSS));
		Move move = eb.findMove(board);
		assertEquals(bestMove, move);
	}
	
	@Test
	public void testNotCheckMate(){
		System.out.println(MoveUtil.isCheckMate(new Board("4k3/8/8/8/8/q7/3PPP2/3qK3 w - - 0 1"), 1));
		System.out.println(MoveUtil.isCheckMate(new Board("4k3/8/8/8/7q/5P2/3PP3/3rK3 w - - 0 1"), 1));
		System.out.println(MoveUtil.isCheckMate(new Board("4k3/8/8/8/7q/5P2/2bPP3/3rK3 w - - 0 1"), 1));
		System.out.println(MoveUtil.isCheckMate(new Board("4k3/8/8/8/b6q/5P2/2BPP3/3rK3 w - - 0 1"), 1));
	}
	
	@Test
	public void testWhyDidTheKnightCaptureTheProtectedPawn(){
		Board board = new Board("2b2rk1/r1pnqppp/p1p1p3/3pP3/N4P2/PP2P3/2P3PP/R1BQ1RK1 b - - 0 1");
		Move move = eb.findMove(board);
		Move doNotMoveThat = new Move(-BoardUtil.KNIGHT, "D7E5");
		assertNotEquals(doNotMoveThat, move);
	}
}