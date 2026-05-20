import java.util.ArrayList;

public class Player {
    private String name;
    private int chips;
    private ArrayList<Card> hand = new ArrayList<>();
    private boolean folded;
    private String handType = "Not checked";

    public Player(String name, int chips) {
        this.name = name;
        this.chips = chips;
        this.folded = false;
    }

    public String getName() {
        return name;
    }

    public int getChips() {
        return chips;
    }

    public void addChips(int amount) {
        chips += amount;
    }

    public void removeChips(int amount) {
        chips -= amount;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void clearHand() {
        hand.clear();
    }

    public boolean isFolded() {
        return folded;
    }

    public void setFolded(boolean folded) {
        this.folded = folded;
    }

    public String getHandType() {
        return handType;
    }

    public void setHandType(String handType) {
        this.handType = handType;
    }
}
