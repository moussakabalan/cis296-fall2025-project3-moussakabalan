package monopoly;

public class Player {
    public String name;
    public int cash;
    public int position;
    public int consecutiveDoubles;

    public boolean inJail;
    public boolean isBankrupt;

    public Player(String name, int startingCash, int startPosition) {
        this.name = name;
        this.cash = startingCash;
        this.position = startPosition;
        this.consecutiveDoubles = 0;

        this.inJail = false;
        this.isBankrupt = false;
    }

    public void addCash(int amount) {
        this.cash += amount;
    }

    public boolean payCash(int amount) {
        if (this.cash >= amount) {
            this.cash -= amount;
            return true;
        }

        return false;
    }

    public void setPosition(int index) {
        this.position = index;
    }

    public void resetDoubles() {
        this.consecutiveDoubles = 0;
    }
}