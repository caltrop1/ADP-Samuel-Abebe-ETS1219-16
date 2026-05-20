import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class HandEvaluator {
    public static String getHandType(ArrayList<Card> hand) {
        // Check strongest hands first.
        if (isRoyalFlush(hand)) {
            return "Royal Flush";
        }
        if (isStraight(hand) && isFlush(hand)) {
            return "Straight Flush";
        }
        if (hasCount(hand, 4)) {
            return "Four of a Kind";
        }
        if (hasCount(hand, 3) && hasCount(hand, 2)) {
            return "Full House";
        }
        if (isFlush(hand)) {
            return "Flush";
        }
        if (isStraight(hand)) {
            return "Straight";
        }
        if (hasCount(hand, 3)) {
            return "Three of a Kind";
        }
        if (numberOfPairs(hand) == 2) {
            return "Two Pair";
        }
        if (numberOfPairs(hand) == 1) {
            return "One Pair";
        }
        return "High Card";
    }

    public static int getHandScore(ArrayList<Card> hand) {
        String handType = getHandType(hand);

        if (handType.equals("Royal Flush")) {
            return 10;
        }
        if (handType.equals("Straight Flush")) {
            return 9;
        }
        if (handType.equals("Four of a Kind")) {
            return 8;
        }
        if (handType.equals("Full House")) {
            return 7;
        }
        if (handType.equals("Flush")) {
            return 6;
        }
        if (handType.equals("Straight")) {
            return 5;
        }
        if (handType.equals("Three of a Kind")) {
            return 4;
        }
        if (handType.equals("Two Pair")) {
            return 3;
        }
        if (handType.equals("One Pair")) {
            return 2;
        }
        return 1;
    }

    public static int getHandStrength(ArrayList<Card> hand) {
        return getHandScore(hand) * 100 + getBestRankValue(hand);
    }

    private static boolean isRoyalFlush(ArrayList<Card> hand) {
        if (!isFlush(hand)) {
            return false;
        }

        return containsRank(hand, Rank.TEN)
                && containsRank(hand, Rank.JACK)
                && containsRank(hand, Rank.QUEEN)
                && containsRank(hand, Rank.KING)
                && containsRank(hand, Rank.ACE);
    }

    private static boolean isFlush(ArrayList<Card> hand) {
        // Count suits with a HashMap, as a beginner-friendly example.
        HashMap<Suit, Integer> suitCounts = new HashMap<>();

        for (Card card : hand) {
            Suit suit = card.getSuit();
            suitCounts.put(suit, suitCounts.getOrDefault(suit, 0) + 1);
        }

        for (int count : suitCounts.values()) {
            if (count == 5) {
                return true;
            }
        }

        return false;
    }

    private static boolean isStraight(ArrayList<Card> hand) {
        // Sort card values, then check whether each value is one higher.
        ArrayList<Integer> values = new ArrayList<>();

        for (Card card : hand) {
            values.add(card.getRank().getValue());
        }

        Collections.sort(values);

        boolean normalStraight = true;
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) + 1 != values.get(i + 1)) {
                normalStraight = false;
            }
        }

        boolean aceLowStraight = values.get(0) == 2
                && values.get(1) == 3
                && values.get(2) == 4
                && values.get(3) == 5
                && values.get(4) == 14;

        return normalStraight || aceLowStraight;
    }

    private static boolean hasCount(ArrayList<Card> hand, int wantedCount) {
        HashMap<Rank, Integer> rankCounts = getRankCounts(hand);

        for (int count : rankCounts.values()) {
            if (count == wantedCount) {
                return true;
            }
        }

        return false;
    }

    private static int numberOfPairs(ArrayList<Card> hand) {
        HashMap<Rank, Integer> rankCounts = getRankCounts(hand);
        int pairs = 0;

        for (int count : rankCounts.values()) {
            if (count == 2) {
                pairs++;
            }
        }

        return pairs;
    }

    private static HashMap<Rank, Integer> getRankCounts(ArrayList<Card> hand) {
        // Count ranks with a HashMap so pairs and triples are easy to find.
        HashMap<Rank, Integer> rankCounts = new HashMap<>();

        for (Card card : hand) {
            Rank rank = card.getRank();
            rankCounts.put(rank, rankCounts.getOrDefault(rank, 0) + 1);
        }

        return rankCounts;
    }

    private static int getBestRankValue(ArrayList<Card> hand) {
        int bestValue = 0;

        for (Card card : hand) {
            if (card.getRank().getValue() > bestValue) {
                bestValue = card.getRank().getValue();
            }
        }

        return bestValue;
    }

    private static boolean containsRank(ArrayList<Card> hand, Rank rank) {
        for (Card card : hand) {
            if (card.getRank() == rank) {
                return true;
            }
        }

        return false;
    }
}
