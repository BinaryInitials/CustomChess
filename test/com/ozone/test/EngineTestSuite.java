package com.ozone.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;

import com.ozone.common.Board;
import com.ozone.common.Move;
import com.ozone.engine.EngineMinMaxNoMateDectionMAXDEPTH5;
import com.ozone.movements.AdvancedMoveUtil;
import com.ozone.movements.MoveUtil;

public class EngineTestSuite {
	
	/*
	 * Some of the most basic and interesting fork, pin, and capture gotchas found.
	 * This test suite is meant to determine how good an engine performs before being used against other engines.
	 */

	public static EngineMinMaxNoMateDectionMAXDEPTH5 e = new EngineMinMaxNoMateDectionMAXDEPTH5(1);
//	public static Engine e = new EngineNewFastFast(1);
	
	public static double rate = 0.0;
	public static int numCorrect = 0;
	public static int total = 0;
	public static long time = 0;
	private final int ACC_THRESHOLD = 80;
	private final int TPM_THRESHOLD = 500;

	public static void runUnitTest(TestCase cc){
		runUnitTest(cc.getBoard(), cc.getTestAgainstMoves(), cc.isExpected(), cc.getDescription());
	}
	private static HashMap<String, Integer> result = new HashMap<String, Integer>();
	private static List<Long> timeList = new ArrayList<Long>();
	
	public static void runUnitTest(Board board, List<Move> expected, boolean isExpected, String description){
		System.out.print(description + "\t");
		int team = board.getPiece(expected.get(0).getFromPos()) > 0 ? 1 : -1;
		e.setTeam(team);
		Date tic = new Date();
		Move actual = e.findMove(board, 30, false, true, new ArrayList<Move>());
//		Move actual = e.findMove(board, 30, false, false, new ArrayList<Move>());
		long timeThisTest = (new Date().getTime() - tic.getTime());
		time = time + timeThisTest;
		timeList.add(timeThisTest);
		total++;
		result.put(description, isExpected == expected.contains(actual) ? 1 : 0);
		System.out.println((isExpected == expected.contains(actual)) + "\t" + timeThisTest + " ms\t" + e.getAverageNPS() + " NPS\t");
	}
	
	public static void runUnitTest(Board board, List<String> expectedPositionMoves){
		List<Move> expected = new ArrayList<Move>();
		for(String expectedPositionMove : expectedPositionMoves){
			expected.add(new Move(board, expectedPositionMove));
		}
		int team = board.getPiece(expected.get(0).getFromPos()) > 0 ? 1 : -1;
		e.setTeam(team);
		Date tic = new Date();
		Move actual = e.findMove(board);
		time = time + (new Date().getTime() - tic.getTime());
		total++;
		assertTrue(expected.contains(actual));
		numCorrect++;
	}
	
	public static void runUnitTest(Board board, String expectedPositionMove){
		runUnitTest(board, expectedPositionMove, true);
	}
	
	public static void runUnitTest(Board board, String expectedPositionMove, boolean isExpected){
		Move expected = new Move(board, expectedPositionMove);
		int team = board.getPiece(expected.getFromPos()) > 0 ? 1 : -1;
		e.setTeam(team);
		Date tic = new Date();
		Move actual = e.findMove(board);
		time = time + (new Date().getTime() - tic.getTime());
		total++;
		System.out.println("Actual move: " + actual);
		assertTrue(isExpected == expected.equals(actual));
		numCorrect++;
	}
	
	@AfterClass
	public static void displayResults(){
		List<String> failedTests = new ArrayList<String>();
		for(String description : result.keySet()) {
			if(result.get(description) == 0) {
				failedTests.add(description);
			}
		}
		Collections.sort(failedTests);
		int i=0;
		String engineName = e.getClass().getName().replaceAll(".*\\.", "");
		System.out.println("\n\nFailed Test Cases for " + engineName + ":\n");
		for(String description : failedTests) {
			System.out.println(i++ + "\t" + description);
		}
		double percentage = (100*numCorrect) / ((double) total);
		double tpm = time/((double) total);
		System.out.println("Engine Efficacy:\t" + numCorrect + "\t" + total + "\t" + String.format("%.2f", percentage) + "%");
		System.out.println("Engine Efficiency:\t" + String.format("%.2f", tpm) + "ms");
		if(timeList.size() > 0){
			Collections.sort(timeList);
			System.out.println("MaxTime: " + Collections.max(timeList) + "\tMinTime: " + Collections.min(timeList) + "\tMedian: " + timeList.get(timeList.size()/2));
		}
	}
	
	@Test
	public void evalutateTheEngine() {
		String engineName = e.getClass().getName().replaceAll(".*\\.", "");
		int i=0;
		for(TestCase cc : testCases) {
			System.out.print(i++ + "\t");
			runUnitTest(cc);
		}
		for(String description : result.keySet()) {
			numCorrect = numCorrect + result.get(description);
		}
		double percentage = (100*numCorrect) / ((double) total);
		double tpm = time/((double) total);
		assertTrue("Percentage for " + engineName + " was below 80%", percentage > ACC_THRESHOLD);
		assertTrue(engineName + " took too long to proceed with tests", tpm < TPM_THRESHOLD);
	}
	
	
	private static List<TestCase> testCases = Arrays.asList(
			new TestCase("SacrificeAfterSacrficeToGetToMegaFork","2b1rnk1/2q2rp1/4p2Q/pp1p3N/2p5/6RP/2B2PP1/4R1K1 w - - 0 1","g3g7"),
			new TestCase("NewBattleLossLeadsToFork", "4k3/2r3r1/3p2p1/4p3/8/3N4/1B2P3/3RK3 w - - 0 1", "d3e5"),
			new TestCase("ReinforcementToPreventPromotion","r1k1rR2/4P3/bp6/2p2P2/3p4/P7/1P4PP/RB4K1 b KQkq - 0 1","C8D7"),
			new TestCase("SacrificeKnightForQueen","1rbk1q2/7p/pbpP4/4NP2/4Qnr1/P1N5/5P1P/R1B1K2R b KQ - 0 1","F4G2"),
			new TestCase("DoNotCaptureProtectedPawn","r3k2r/1pp2ppn/pbn5/3N2Pp/BP1P4/P3B2P/2P2P2/R3K2R b KQkq - 0 1","H7G5", false),
			new TestCase("PromoteSurpriseCheck","2k2r2/8/7P/8/2n2P2/r3p1P1/3qp2K/1R6 b - - 0 1","E2E1"),
			new TestCase("KnightBishopForPawnsBlunder","r1bq1rk1/1pp2pp1/p1nbp3/3p2Np/3PPPn1/P1N3P1/1PP1B2P/R1BQ1RK1 b KQkq - 0 1", "g4f6", false),
			new TestCase("NotTradingLeadsToAMatein3", "rqb3k1/1p4pp/p1p2r2/6Q1/1P6/P1P5/5RP1/R5K1 b KQkq - 0 1", "F6F2"),
			new TestCase("ForkLossDetection", "1rb2rk1/p1pq2p1/1np1p2p/3pNp2/2PP4/1P4P1/P2B1PBP/R2QR1K1 w - - 0 1", "D7D6", false),
			new TestCase("NotFork", "r1b1kr2/1ppp1qQp/p1n5/8/4np2/2P4N/PPP3PP/R1B1K2R w KQq - 0 1", "F3G5", false),
			new TestCase("BishopKamikaze", "r1bq1rk1/pp1n1ppp/1bpNp3/3pP3/1P6/P2QP3/1BP1BPPP/R3K2R b KQkq - 0 1","B6E3", false),
			new TestCase("SimpleCapture", "r1Nq3r/pp1nk2p/2p1pp2/2b1P1p1/3p4/4P1Q1/PPP2PPP/R1B1KB1R b KQkq - 0 1",Arrays.asList("A8C8", "D8C8")),
			new TestCase("RookMovingTowardsPinnedMove", "1rb2rk1/2p2pb1/p1pp2qp/4p3/3P1RP1/1PN1P3/P1PN2PP/R2Q2K1 b KQkq - 0 1","F4F3", false),
			new TestCase("ThreatenedEverywhere", "1r3rk1/2p2p2/p1pp3p/4b3/4N3/1PNqP3/P4QPP/R5K1 b KQkq - 0 1", "A1D1"),
			new TestCase("BackAttack","r3kb1r/1pp1nppp/p2p1n2/3Pp1B1/4P2P/2N5/PPP1BPqP/R2QK2R b KQkq - 0 1","e2f3"),
			new TestCase("ProtectedFromDanger","rnbqk2r/ppp1b2p/5pp1/3pN3/3Pn3/5Q2/PPP2PPP/RNB1KB1R b KQkq - 0 1","F1B5", false),
			new TestCase("SickFutureForkDoneRight","r1b1kr2/1ppp1p1Q/p1n2q2/8/2B1np2/2P2N2/PPP3PP/R1B1K2R b KQq - 0 1","E4C3"),
			new TestCase("SurpriseCheckShouldNotHaveBeen","r1b1kr2/1ppp1pQp/p1n2q2/8/2B1np2/2P2N2/PPP3PP/R1B1K2R b KQq - 0 1","G7F7", false),
			new TestCase("CatptureFreeKnightWithQueen","r2qkb1r/ppp1pppp/2n5/3N1b2/3P4/5N2/PPP2PPP/R1BQKB1R b KQkq - 0 1","D8D5"),
			new TestCase("ForkVersusCaptureOfSameValue","r3k3/8/8/3R4/2b5/8/8/3RK3 b - - 0 1","C4B3", false),
			new TestCase("ForkVersusCaptureOfSameValue2","r3k3/8/8/3R4/2b5/8/8/3RK3 b - - 0 1","C4D5"),
			new TestCase("Capture","r4r2/2pqb1k1/p1p2p2/Qp2p1pp/4P1N1/3PP3/PPPN1KPP/R6R b - - 0 1","H5G4"),
			new TestCase("WillNextMoveBeSkewer","r7/2pq1k2/pbp2p2/1p2p1p1/1Q2P1P1/3PPNP1/PPP2K2/7R b - - 0 1","h1h7"),
			new TestCase("MateInTwo","4qrk1/p4p1p/nb2BBp1/8/8/6P1/2Q2PK1/7R w - - 0 1","c2g6"),
			new TestCase("CaptureCount","r1bqk2r/ppp2ppp/2n2n2/2bpp3/2B1P3/2NP1N2/PPP2PPP/R1BQK2R w KQkq - 0 1","e4d5"),
			new TestCase("QueenCaptureFollowedByFork","r1b3k1/1pp2p1p/p4n2/3P3n/2BP3q/2N1Q3/PPP3PN/6K1 b - - 0 1","h4h2"),
			new TestCase("CheckMate","1rk4r/1p6/3p3q/Q1pPbRnP/p1P3B1/P6P/1P4R1/7K w - - 0 1","f5f8"),
			new TestCase("MateInTwoWithKnightInConspicuousPosition","r2qrk2/1bp1b1pp/p1np4/1p1Q1NB1/4n3/2P5/PP3PPP/RN2R1K1 w - - 0 1","f5h6"),
			new TestCase("MateInFour","8/2R5/4R1p1/1p4k1/6P1/1nPn3P/3r1PK1/8 w - - 0 1","g2g3"),
			new TestCase("PromotionAvoidance","8/pR6/5k2/3p3P/5P2/5K2/r7/8 b - - 0 1","a2h2"),
			new TestCase("PreventFork","r1b1k2r/ppp2ppp/2nqpn2/1N1p4/3P4/P4N2/1PP1PPPP/R2QKB1R b KQkq - 0 1","d6f4", false),
			new TestCase("DoNotCaptureWithTwoRooksVersusKingAndRook","2k3r1/ppp2p1p/2p2p2/8/8/P1PP1P1P/RP1R2Pr/6K1 b - - 0 1","H2H3"),
			new TestCase("NotProtected","2r2r2/pb3pk1/3pp2p/3p1p2/2qP4/1PP1PN2/P1QR1PPP/2R4K b KQkq - 0 1","C4B4", false),
			new TestCase("UseOtherPawnToThreatenedAndHelpPromote","4k3/8/8/8/8/1p6/2p5/2R3K1 b - - 0 1","b3b2"),
			new TestCase("CaptureWithWeakestPiece","4k3/3rn3/8/1q1B4/8/8/8/4K3 b - - 0 1","E7D5"),
			new TestCase("ObviousRookCapture","rnbqk2r/ppp2ppp/4p3/3p4/2PP4/1Pb1P3/P3KPPP/R2Q1BNR b KQkq - 0 1","C3A1"),
			new TestCase("FriedLiver1","r2qkb1r/ppp2ppp/4p3/3p1b2/1nPP4/1P2P3/P4PPP/R2QKBNR b KQkq - 0 1","b4c2"),
			new TestCase("FriedLiver2","r2qkb1r/ppp2ppp/4p3/3p1b2/2PP4/1P2P3/P1n1KPPP/R2Q1BNR b kq - 0 1","c2a1"),
			new TestCase("FriedLiver3","r2qkb1r/ppp2ppp/4p3/3p1b2/2PP4/1P2P3/P1n1K1PP/R2Q1B1R b kq - 0 1","f5g4"),
			new TestCase("PawnPromotion","5k1r/pR3p2/2p5/7p/8/2K2p2/q7/8 b - - 0 1","f3f2"),
			new TestCase("AvoidTheStalemate","8/4k3/2p5/1p1p4/4r3/3K4/8/q7 w - - 0 1","A1C1", false),
			new TestCase("TradeFollowedByFork","rnbqk2r/pppp1ppp/4p3/3n4/1b1PP3/2N2N2/PPP2PPP/R1BQKB1R b KQkq - 0 1","d5c3"),
			new TestCase("DetectMateIn2","r1b1r1k1/1pp2p1p/2q1pPp1/8/3P4/1P1B1N2/P2Q1PPP/R4RK1 b KQkq - 0 1","C8D7", false),
			new TestCase("PawnMovementSavesFromMate","r1bq1rk1/ppp2ppp/2n1pP2/3p4/1bP5/1PNQ4/P2B1PPP/RB2K1NR b KQ - 0 1","G7G6"),
			new TestCase("DoNotCaptureKnightBecauseItLeadsToMateInThree","r1br2k1/pppp1p2/2nb2N1/q1p5/3P4/2P1P3/PPQN1PP1/RB2K2R b KQ - 0 1","f7g6", false),
			new TestCase("SurpriseMateKnightMove","r1b1N1k1/pp3pbp/1q4p1/3p4/4PP2/2N5/PP2QnPP/1RB2BK1 b - - 0 1","f2h3"),
			new TestCase("MateInThreeAfterQueenSacrifice","3RQn2/2r1q1k1/4Bppp/3p3P/3p4/4P1P1/5PK1/8 w - - 0 1","E8G6"),
			new TestCase("MateInThreeAfterRookSacrifice","6k1/6p1/3Np2p/2P1P3/1p2Q1Pb/1P5P/1qr5/5RK1 w - - 0 1","F1F8"),
			new TestCase("MateInThreeAfterBishopSacrifice","r5r1/p1q2p1k/1p1R2pB/3pP3/6bQ/2p5/P1P1NPPP/6K1 w - - 0 1","H6F8"),
			new TestCase("RookCaptureSacrifice","8/1p1r2k1/q4npp/3B4/P1PQ4/8/6PP/5R1K b - - 0 1", "D7D5"),
			new TestCase("IgnoreSenselessSacrifice","r2b4/2RRQP2/7k/8/8/8/6PP/7K b - - 0 1","A8A1"),
			new TestCase("BatteryProtection","r2q3r/pppbnk2/4p2p/5p2/3Qp2N/P1P3P1/2P2P1P/3RKB1R b KQkq - 0 1","D7C6"),
			new TestCase("AwesomeCheckLeadsToCaptureRookInsteadOfFreeBishop","r2q3r/ppp2k2/4p2p/3n1N2/b2Qp3/P1P3P1/2P2P1P/3RKB1R w K - 0 1","D4G7"),
			new TestCase("MateUsingRollingRooks","8/8/8/7k/1R5p/R7/8/K7 w - - 0 1","a3a5"),
			new TestCase("MateWithQueenSacrificeBattery","5r2/p1pkb2p/2p1p1p1/8/1PPPp3/P3P3/5qPP/R1BQ3K b KQkq - 0 1","F2F1"),
			new TestCase("KermurSireDeLegalVsSaintBrie1750Part1","rn1qkbnr/ppp2p1p/3p2p1/4p3/2B1P1b1/2N2N2/PPPP1PPP/R1BQK2R w KQkq - 0 1","F3E5"),
			new TestCase("KermurSireDeLegalVsSaintBrie1750Part2","rn1qkbnr/ppp2p1p/3p2p1/4N3/2B1P3/2N5/PPPP1PPP/R1BbK2R w KQkq - 0 1","C4F7"),
			new TestCase("BodensMate","2kr3r/pp1nqppp/1b2pnb1/8/Q2P1B2/2P2N2/PP1NBPPP/R4RK1 w KQkq - 0 1","a4c6"),
			new TestCase("Damiano","r4rk1/pppqn1p1/2n1b1P1/3p1p2/1b1P4/2NBBN2/PPP2P2/2KRQ2R w KQkq - 0 1","h1h8"),
			new TestCase("Grecos","4rr1k/pppbq1pp/1b1p1n2/8/2BP4/4B3/PPPN1PPR/1K1Q3R w KQkq - 0 1","h2h7"),
			new TestCase("EpauletteMate","3rkb1R/pp1n1pp1/1qpp3n/4p3/2BPP3/1QP1BP2/PP3P2/R3K3 w KQkq - 0 1","C4F7"),
			new TestCase("TheImmortalGame1","r1b1k1nr/p2p1ppp/n2B4/1p1NPN1P/6P1/3P1Q2/P1P1K3/q5b1 w KQkq - 0 1","F5G7"),
			new TestCase("TheImmortalGame2","r1bk2nr/p2p1pNp/n2B4/1p1NP2P/6P1/3P1Q2/P1P1K3/q5b1 w - - 0 1","f3f6"),
			new TestCase("PaulMorphyParis1859SmotheredGame","r1k4r/ppp1bq1p/2n1N3/6B1/3p2Q1/8/PPP2PPP/R5K1 w KQkq - 0 1",Arrays.asList("e6c5", "e6f8")),
			new TestCase("RuiLopezWhiteQueenBishopBatteryBlack","r1bqk1nr/ppp2ppp/2p5/2b1N3/4P3/8/PPPP1PPP/RNBQK2R b KQkq - 0 1","D8D4"),
			new TestCase("Pin1","rnbqk1nr/pppp1ppp/4p3/4R3/1b3Q2/4P3/1PPP1PPP/1NB1KBNR b Kk - 0 1","b4d6"),
			new TestCase("Pin2","rnbqkbnr/pppp1ppp/4p3/4R3/5Q2/4P3/1PPP1PPP/1NB1KBNR w Kk - 0 1","f8d6"),
			new TestCase("KnightThreatenedEverywhere","3r4/1p4kp/p2p2p1/3N4/3n4/2P3P1/PP3P1P/R5K1 b KQkq - 0 1","D4C2", false),
			new TestCase("QueenKillingHerself","r1b1k1nr/p1p2ppp/2q1p3/8/1b1P4/2NQ1N2/PPP2PPP/R1B1K2R b KQkq - 0 1","C6C3", false),
			new TestCase("BishopKillingItself","r3k1nr/p1p2ppp/b3p3/8/1b1P4/2P2N2/P1PQ1PPP/R1B1K2R w KQkq - 0 1","B4C3", false),
			new TestCase("TradeOfQueens1","r1b1kb1r/pp1pqQpp/2p5/4N3/1N1Pn3/8/PPP3PP/R1B1KB1R b KQkq - 0 1","E8D8",false),
			new TestCase("TradeOfQueens2","r1b1kb1r/pp1pqQpp/2p5/4N3/1N1Pn3/8/PPP3PP/R1B1KB1R b KQkq - 0 1","E7F7"),
			new TestCase("RetreatPieceThatIsInDanger","r1b2b1r/pp1pk1pp/2p5/4N3/1N1Pn3/3B4/PPP3PP/R1B1K2R b KQkq - 0 1","E7E8", false),
			new TestCase("KnightRecapturesPawnInsteadOfKnight","r1bqk2r/pppp1ppp/2n5/2bNP3/3Pn3/5N2/PPP3PP/R1BQKB1R b KQkq - 0 1","C6E5", false),
			new TestCase("DoNotGiveUpTheQueen1","r1b3k1/2p1n1pp/1p2pr2/1P1q1p2/3B1P2/P1NQP3/6PP/R3K2R b KQ - 0 1","C7C5", false),
			new TestCase("DoNotGiveUpTheQueen2","r1b3k1/2p1n1pp/1p2pr2/1P1q1p2/3B1P2/P1NQP3/6PP/R3K2R b KQ - 0 1","D5G2"),
			new TestCase("CaptureFreeKnightWhyDontYou","r1b2rk1/2p1n1pp/1p2p3/1P1q1p2/N2B1P2/P2QP3/6PP/R3K2R b KQ - 0 1","A8A4"),
			new TestCase("CapturePieceBecauseDefenderIsPinned","2r3k1/pp5p/q2ppr2/n2p1ppQ/3Pn3/1NP1P3/PP3PPP/RN3RK1 b - - 0 1","a5b3"),
			new TestCase("CaptureLeadsToTripleFork","2rqkbnr/p1p3pp/3p1p2/1p1Np3/3nP3/P1Q4P/1PPPBPP1/R1B2RK1 b k - 0 1","d4e2"),
			new TestCase("MateIn2","8/8/8/8/b1QN4/r7/p7/k1K5 w - - 0 1","C4B4"),
			new TestCase("MateIn2SacrificeBishop","8/5Q2/1pkq2n1/p3p3/4P3/1P2K3/2P1B3/8 w - - 0 1","e2b5"),
			new TestCase("ProtectingBishop","r1bqk2r/1pp2ppp/p1n1pn2/1B1p4/1b1P4/2N1PN2/PPP1KPPP/R1BQ3R w KQkq - 0 1","b5c6"),
			new TestCase("StaleMateNotDesired","8/pp5r/2p1K3/4b1k1/8/8/8/8 b - - 0 1","E5C7", false),
			new TestCase("PromotionRidiculousThatItsNotTakingIt1","8/4k2P/8/7Q/P7/8/4K3/8 b KQkq - 0 1","H5C5",false),
			new TestCase("PromotionRidiculousThatItsNotTakingIt2","8/4k2P/8/7Q/P7/8/4K3/8 b KQkq - 0 1","H7H8"),
			new TestCase("PreventKingMovementMateIn3","r6r/3kbQ1p/p2p1p2/4pP2/1P2q1P1/P3B3/3R1K1P/2R5 b - - 0 1","D8D7",false)
			);
	
	@Test
	public void testingTime() {
		Board board = new Board("1rb2rk1/p1pq2p1/1np1p2p/3pNp2/2PP4/1P4P1/P2B1PBP/R2QR1K1 w - - 0 1");
		
		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(board, 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(board, -1));
		
		runUnitTest(board, "D7D6", false);
	}
	
	@Test
	public void runIndividualTest(){
		TestCase tc = new TestCase("DoNotCaptureKnightBecauseItLeadsToMateInThree","r1br2k1/pppp1p2/2nb2N1/q1p5/3P4/2P1P3/PPQN1PP1/RB2K2R b KQ - 0 1","f7g6", false);
		System.out.println("Running " + tc.getDescription());
		runUnitTest(tc.getBoard(),"f7g6", false);
	}

	@Test
	public void runConspicuous(){
		/*
		 * The reason this fails is because KeepChecking doesn't work unless perpetual checks are in session. 
		 * This mate is a result of 2 paths: 
		 * One from the knight that is danger combined with the queen or
		 * If the knight is captured immediately, the bishop mates as in Boden's mate.
		 * Because of the 2 paths, the node count required to detect this is high i.e. ~40k.
		 * Also, albeit this being a mate in 2, the new algorithm requires an additional "depth credit" to determine if the opponent has any moves left.
		 * This can be fixed...
		 * The reason that this was done is because isCheckMate is an expensive method. Doing that for every node took forever.    
		 */
		TestCase tc = new TestCase("MateInTwoWithKnightInConspicuousPosition","r2qrk2/1bp1b1pp/p1np4/1p1Q1NB1/4n3/2P5/PP3PPP/RN2R1K1 w - - 0 1","f5h6");
		System.out.println("Running " + tc.getDescription());
		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(tc.getBoard(), 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(tc.getBoard(), -1));
		runUnitTest(tc.getBoard(),tc.getTestAgainstMoves().get(0).getSquare());
	}
	
	@Test
	public void mateInTwo() {
		Board board = new Board("8/8/8/8/b1QN4/r7/p7/k1K5 w - - 0 1");
		
		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(board, 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(board, -1));
		
		assertTrue(AdvancedMoveUtil.isMyNextMovePuttingHimToCheckMate(new Move(board, "C4B4").updateBoard(board), 1));
		
		runUnitTest(board, "C4B4");
	}
	
	@Test
	public void simpleCapture() {
		Board board = new Board("r1Nq3r/pp1nk2p/2p1pp2/2b1P1p1/3p4/4P1Q1/PPP2PPP/R1B1KB1R b KQkq - 0 1");
		runUnitTest(board, "D8C8");
	}
	
	@Test
	public void smotheredGame() {
		Board board = new Board("r1k4r/ppp1bq1p/2n1N3/6B1/3p2Q1/8/PPP2PPP/R5K1 w KQkq - 0 1");
		
		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(board, 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(board, -1));
		
		runUnitTest(board, Arrays.asList("e6c5", "e6f8"));
	}
	
	@Test
	public void mateInFour() {
		Board board = new Board("8/2R5/4R1p1/1p4k1/6P1/1nPn3P/3r1PK1/8 w - - 0 1");
		
		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(board, 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(board, -1));
		
		runUnitTest(board, "g2g3");
	}
	
	@Test
	public void legalBrie1() {
		Board board = new Board("rn1qkbnr/ppp2p1p/3p2p1/4p3/2B1P1b1/2N2N2/PPPP1PPP/R1BQK2R w KQkq - 0 1");
		
		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(board, 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(board, -1));
		
		runUnitTest(board, "F3E5");
	}
	
	@Test
	public void testMovementLedToMateIn3() {
		Board board = new Board("r6r/3kbQ1p/p2p1p2/4pP2/1P2q1P1/P3B3/3R1K1P/2R5 b - - 0 1");
		
		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(board, 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(board, -1));
		
		runUnitTest(board, "D7D8", false);
	}

	@Test
	public void testQLfollowedByF() {
		Board board = new Board("r1b3k1/1pp2p1p/p4n2/3P3n/2BP3q/2N1Q3/PPP3PN/6K1 b - - 0 1");
		
		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(board, 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(board, -1));
		
		runUnitTest(board, "h4h2");
	}
	
	@Test
	public void testAwesome() {
		Board board = new Board("r2q3r/ppp2k2/4p2p/3n1N2/b2Qp3/P1P3P1/2P2P1P/3RKB1R w K - 0 1");
		
		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(board, 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(board, -1));

		runUnitTest(board, "D4G7");
	}
	
	@Test
	public void testSpecificTest() {
		TestCase tc = getTestCase("SacrificeAfterSacrficeToGetToMegaFork");
		Board board = tc.getBoard();
		String square = tc.getTestAgainstMoves().get(0).getSquare();
		
		System.out.println("Test case " + (!tc.isExpected() ? "not " :" ") + "expecting this move: " + square);
		
		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(board, 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(board, -1));
		
		runUnitTest(board, square, tc.isExpected());
	}
	
	public TestCase getTestCase(String description) {
		for(TestCase tc : testCases) {
			if(tc.getDescription().equals(description)) {
				return tc;
			}
		}
		return null;
	}
	
	@Test
	public void testRollingRook() {
		Board board = new Board("8/8/8/7k/1R5p/R7/8/K7 w - - 0 1");

		System.out.println("WC# " + MoveUtil.getMoveCountForTeam(board, 1));
		System.out.println("BC# " + MoveUtil.getMoveCountForTeam(board, -1));
		
		runUnitTest(board,"a3a5");
	}
}