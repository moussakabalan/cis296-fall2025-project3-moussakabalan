package monopoly;

import java.util.*;

public class GameEngine {
    private Rules rules;
    private Board board;
    private final List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private final Random rng = new Random();
    
    private int lastDice1 = 0;
    private int lastDice2 = 0;
    private String lastMessage = "";
    private Property pendingProperty = null;
    private boolean waitingForPropertyDecision = false;
    private boolean turnInProgress = false;
    
    public GameEngine() {}
    
    public void newGame(List<String> playerNames) {
        rules = new Rules();
        board = new Board(rules);
        players.clear();
        
        for (String name : playerNames) {
            if (name == null || name.isEmpty()) name = "Player" + (players.size() + 1);
            players.add(new Player(name, rules.startCash, rules.goIndex));
        }
        
        currentPlayerIndex = 0;
    }
    
    public boolean loadGame(String fileName) {
        Rules[] rulesOut = new Rules[1];
        Board[] boardOut = new Board[1];
        int[] indexOut = new int[1];
        
        boolean success = SaveLoad.Load(fileName, rulesOut, boardOut, players, indexOut);
        
        if (success) {
            this.rules = rulesOut[0];
            this.board = boardOut[0];
            this.currentPlayerIndex = indexOut[0];
        }
        
        return success;
    }
    
    public boolean saveGame(String fileName) {
        return SaveLoad.Save(fileName, board, players, currentPlayerIndex);
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    public Board getBoard() {
        return board;
    }
    
    public Rules getRules() {
        return rules;
    }
    
    public int[] getLastDiceRoll() {
        return new int[]{lastDice1, lastDice2};
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public boolean isWaitingForPropertyDecision() {
        return waitingForPropertyDecision;
    }
    
    public Property getPendingProperty() {
        return pendingProperty;
    }
    
    public void rollDice() {
        if (turnInProgress) return;
        
        Player player = getCurrentPlayer();
        lastMessage = "";
        
        if (player.inJail) {
            if (!player.payCash(rules.jailFee)) {
                lastMessage = player.name + " cannot afford jail fee and goes bankrupt!";
                bankruptPlayer(player);
                advanceTurn();
                return;
            }
            player.inJail = false;
            lastMessage = player.name + " paid $" + rules.jailFee + " to get out of jail.\n";
        }
        
        lastDice1 = 1 + rng.nextInt(6);
        lastDice2 = 1 + rng.nextInt(6);
        int diceSum = lastDice1 + lastDice2;
        boolean isDoubles = (lastDice1 == lastDice2);
        
        lastMessage += "Rolled: " + lastDice1 + " + " + lastDice2 + " = " + diceSum;
        if (isDoubles) lastMessage += " (DOUBLES!)";
        
        if (isDoubles) {
            player.consecutiveDoubles++;
            if (player.consecutiveDoubles >= rules.maxConsecutiveDoubles) {
                lastMessage += "\nThree consecutive doubles! Go to Jail!";
                jailPlayer(player);
                advanceTurn();
                return;
            }
        }
        
        int size = board.getSize();
        int raw = player.position + diceSum;
        int newPos = raw % size;
        boolean hasPassedGo = raw >= size;
        
        player.setPosition(newPos);
        
        if (hasPassedGo) {
            player.addCash(rules.goReward);
            lastMessage += "\n" + player.name + " passed GO and collects $" + rules.goReward + ".";
        }
        
        onLanding(player, newPos);
        
        if (!isDoubles && !waitingForPropertyDecision) {
            player.resetDoubles();
            advanceTurn();
        }
    }
    
    private void onLanding(Player player, int index) {
        String tileName = board.getSpaceName(index);
        lastMessage += "\n" + player.name + " lands on " + tileName + ".";
        
        Rules.SpaceType type = rules.spaces.get(index).type;
        
        if (type == Rules.SpaceType.PROP) {
            handlePropertyLanding(player, index);
        } else if (type == Rules.SpaceType.CHANCE) {
            handleChance(player);
        } else if (type == Rules.SpaceType.COMMUNITY_CHEST) {
            handleCommunityChest(player);
        }
    }
    
    private void handlePropertyLanding(Player player, int index) {
        Property property = board.getProperty(index);
        
        if (!property.isOwned()) {
            if (player.cash >= property.price) {
                pendingProperty = property;
                waitingForPropertyDecision = true;
            } else {
                lastMessage += "\n" + player.name + " cannot afford this property.";
            }
        } else if (property.owner != player) {
            int rent = property.getRent();
            lastMessage += "\nOwned by " + property.owner.name + ". Rent = $" + rent + ".";
            if (player.payCash(rent)) {
                property.owner.addCash(rent);
                lastMessage += "\n" + player.name + " paid $" + rent + " to " + property.owner.name + ".";
            } else {
                lastMessage += "\n" + player.name + " cannot afford rent and goes bankrupt!";
                bankruptPlayer(player);
            }
        } else {
            lastMessage += "\nYou own this property.";
        }
    }
    
    public void buyProperty() {
        if (!waitingForPropertyDecision || pendingProperty == null) return;
        
        Player player = getCurrentPlayer();
        player.payCash(pendingProperty.price);
        pendingProperty.setOwner(player);
        lastMessage += "\n" + player.name + " bought " + pendingProperty.name + ".";
        
        waitingForPropertyDecision = false;
        pendingProperty = null;
        
        if (lastDice1 != lastDice2) {
            player.resetDoubles();
            advanceTurn();
        }
    }
    
    public void declineProperty() {
        if (!waitingForPropertyDecision) return;
        
        lastMessage += "\n" + getCurrentPlayer().name + " declined to buy.";
        waitingForPropertyDecision = false;
        pendingProperty = null;
        
        if (lastDice1 != lastDice2) {
            getCurrentPlayer().resetDoubles();
            advanceTurn();
        }
    }
    
    private void handleChance(Player player) {
        String[] chanceCards = {
            "Advance to GO! Collect $200",
            "Bank pays you dividend of $50",
            "Go to Jail!",
            "Pay poor tax of $15",
            "Get out of Jail Free card",
            "Advance to nearest property"
        };
        
        String card = chanceCards[rng.nextInt(chanceCards.length)];
        lastMessage += "\nChance: " + card;
        
        if (card.contains("Advance to GO")) {
            player.setPosition(rules.goIndex);
            player.addCash(rules.goReward);
        } else if (card.contains("Bank pays")) {
            player.addCash(50);
        } else if (card.contains("Go to Jail")) {
            jailPlayer(player);
        } else if (card.contains("Pay poor tax")) {
            if (!player.payCash(15)) {
                bankruptPlayer(player);
            }
        }
    }
    
    private void handleCommunityChest(Player player) {
        String[] communityChestCards = {
            "Advance to GO! Collect $200",
            "Bank error in your favor. Collect $200",
            "Doctor's fee. Pay $50",
            "From sale of stock you get $50",
            "Get Out of Jail Free",
            "Holiday fund matures. Receive $100"
        };
        
        String card = communityChestCards[rng.nextInt(communityChestCards.length)];
        lastMessage += "\nCommunity Chest: " + card;
        
        if (card.contains("Advance to GO")) {
            player.setPosition(rules.goIndex);
            player.addCash(rules.goReward);
        } else if (card.contains("Bank error") || card.contains("sale of stock")) {
            player.addCash(card.contains("Bank error") ? 200 : 50);
        } else if (card.contains("Holiday fund")) {
            player.addCash(100);
        } else if (card.contains("Doctor's fee")) {
            if (!player.payCash(50)) {
                bankruptPlayer(player);
            }
        }
    }
    
    public boolean canBuildHouse(Property property) {
        Player player = property.owner;
        if (player == null) return false;
        
        List<Property> colorSet = getPropertiesInColorSet(property.colorSet);
        for (Property p : colorSet) {
            if (p.owner != player) return false;
        }
        
        return property.canBuildHouse() && player.cash >= property.housePrice;
    }
    
    public boolean canBuildHotel(Property property) {
        Player player = property.owner;
        if (player == null) return false;
        
        List<Property> colorSet = getPropertiesInColorSet(property.colorSet);
        for (Property p : colorSet) {
            if (p.owner != player || p.houses != 4) return false;
        }
        
        return property.canBuildHotel() && player.cash >= property.housePrice;
    }
    
    public void buildHouse(Property property) {
        if (!canBuildHouse(property)) return;
        
        property.owner.payCash(property.housePrice);
        property.buildHouse();
    }
    
    public void buildHotel(Property property) {
        if (!canBuildHotel(property)) return;
        
        property.owner.payCash(property.housePrice);
        property.buildHotel();
    }
    
    private List<Property> getPropertiesInColorSet(String colorSet) {
        List<Property> result = new ArrayList<>();
        for (Property p : board.properties.values()) {
            if (p.colorSet != null && p.colorSet.equals(colorSet)) {
                result.add(p);
            }
        }
        return result;
    }
    
    private void advanceTurn() {
        if (players.isEmpty()) return;
        
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        
        int safetySteps = 0;
        while (players.get(currentPlayerIndex).isBankrupt && safetySteps < players.size()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            safetySteps++;
        }
    }
    
    public boolean isGameOver() {
        return getAlivePlayers().size() <= 1;
    }
    
    public Player getWinner() {
        List<Player> alive = getAlivePlayers();
        return alive.size() == 1 ? alive.get(0) : null;
    }
    
    private List<Player> getAlivePlayers() {
        List<Player> alivePlayers = new ArrayList<>();
        for (Player player : players) {
            if (!player.isBankrupt) {
                alivePlayers.add(player);
            }
        }
        return alivePlayers;
    }
    
    private void jailPlayer(Player player) {
        player.setPosition(board.getJailIndex());
        player.inJail = true;
        player.resetDoubles();
    }
    
    private void bankruptPlayer(Player player) {
        player.isBankrupt = true;
        player.cash = 0;
        
        for (Property property : board.properties.values()) {
            if (property.owner == player) {
                property.releaseOwnership();
            }
        }
    }
}
