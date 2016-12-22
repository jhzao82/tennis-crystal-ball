package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

import com.google.common.collect.*;

import static java.lang.Math.*;
import static org.strangeforest.tcb.util.DateUtil.*;

public abstract class MatchDataUtil {

	public static double weight(long total) {
		return total > 10 ? 1.0 : total / 10.0;
	}

	public static double weight(long total1, long total2) {
		return sqrt(weight(total1) * weight(total2));
	}

	public static DoubleUnaryOperator probabilityTransformer(boolean forSet, short bestOf) {
		return forSet ? matchProbability(bestOf) : DoubleUnaryOperator.identity();
	}

	public static DoubleUnaryOperator matchProbability(short bestOf) {
		switch (bestOf) {
			case 3: return MatchDataUtil::bestOf3MatchProbability;
			case 5: return MatchDataUtil::bestOf5MatchProbability;
			default: throw new IllegalArgumentException("Invalid bestOf: " + bestOf);
		}
	}

	public static double bestOf3MatchProbability(double setProbability) {
		return setProbability * setProbability * (3 - 2 * setProbability);
	}

	public static double bestOf5MatchProbability(double setProbability) {
		return setProbability * setProbability * setProbability * (10 - 15 * setProbability + 6 * setProbability * setProbability);
	}

	public static final Predicate<MatchData> ALWAYS_TRUE = m -> Boolean.TRUE;

	public static Predicate<MatchData> isRecent(Date date, Period period) {
		return match -> toLocalDate(match.getDate()).compareTo(toLocalDate(date).minus(period)) >= 0;
	}

	public static Predicate<MatchData> isSurface(Surface surface) {
		return match -> nonNullEquals(match.getSurface(), surface != null ? surface.getCode() : null);
	}

	public static Predicate<MatchData> isLevel(TournamentLevel level) {
		return match -> nonNullEquals(match.getLevel(), level.getCode());
	}

	public static Predicate<MatchData> isRound(Round round) {
		return match -> nonNullEquals(match.getRound(), round.getCode());
	}

	public static Predicate<MatchData> isOpponent(int playerId) {
		return match -> match.getOpponentId() == playerId;
	}

	public static Predicate<MatchData> isOpponentRankInRange(Range<Integer> rankRange) {
		return match -> {
			if (rankRange != null) {
				Integer opponentRank = match.getOpponentRank();
				return opponentRank != null && rankRange.contains(opponentRank);
			}
			else
				return false;
		};
	}

	public static Predicate<MatchData> isOpponentHand(String hand) {
		return match -> nonNullEquals(match.getOpponentHand(), hand);
	}

	public static Predicate<MatchData> isOpponentBackhand(String backhand) {
		return match -> nonNullEquals(match.getOpponentBackhand(), backhand);
	}

	private static boolean nonNullEquals(Object o1, Object o2) {
		return o1 != null && o2 != null && o1.equals(o2);
	}
}