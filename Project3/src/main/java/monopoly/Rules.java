package monopoly;

import java.util.ArrayList;
import java.util.List;

public class Rules {
    public enum SpaceType {
        GO,
        PROP,
        JAIL,
        FREE,
        CHANCE,
        COMMUNITY_CHEST
    }

    public static class BoardSpace {
        public SpaceType type;
        public String name;
        public int index;
        public Integer price;
        public Integer rent;
        public String colorSet;
        public Integer housePrice;
    }

    // RULES SETTINGS (MODIFY WITHIN COMMENTS!)
    public int startCash = 1_500;
    public int goReward = 200;
    public int jailFee = 50;
    public int maxConsecutiveDoubles = 3;
    // Do not modify below!!

    public List<BoardSpace> spaces;
    public int goIndex;
    public int jailIndex;

    private static BoardSpace CreateSpace(int index, SpaceType type, String name, Integer price, Integer rent, String colorSet, Integer housePrice) {
        BoardSpace Space = new BoardSpace();
        Space.type = type;
        Space.name = name;
        Space.index = index;
        Space.price = price;
        Space.rent = rent;
        Space.colorSet = colorSet;
        Space.housePrice = housePrice;

        return Space;
    }

    public Rules() {
        List<BoardSpace> Spaces = new ArrayList<>();

        Spaces.add(CreateSpace(0, SpaceType.GO, "GO", null, null, null, null));
        Spaces.add(CreateSpace(1, SpaceType.PROP, "Oak Street", 60, 20, "Brown", 50));
        Spaces.add(CreateSpace(2, SpaceType.COMMUNITY_CHEST, "Community Chest", null, null, null, null));
        Spaces.add(CreateSpace(3, SpaceType.PROP, "Maple Avenue", 60, 20, "Brown", 50));
        Spaces.add(CreateSpace(4, SpaceType.FREE, "Free Parking", null, null, null, null));
        Spaces.add(CreateSpace(5, SpaceType.PROP, "Cedar Lane", 100, 30, "LightBlue", 50));
        Spaces.add(CreateSpace(6, SpaceType.CHANCE, "Chance", null, null, null, null));
        Spaces.add(CreateSpace(7, SpaceType.PROP, "Pine Road", 120, 40, "LightBlue", 50));
        Spaces.add(CreateSpace(8, SpaceType.JAIL, "Jail", null, null, null, null));
        Spaces.add(CreateSpace(9, SpaceType.PROP, "Elm Street", 140, 50, "Pink", 100));
        Spaces.add(CreateSpace(10, SpaceType.COMMUNITY_CHEST, "Community Chest", null, null, null, null));
        Spaces.add(CreateSpace(11, SpaceType.PROP, "Birch Blvd", 160, 60, "Pink", 100));
        Spaces.add(CreateSpace(12, SpaceType.FREE, "Rest Stop", null, null, null, null));
        Spaces.add(CreateSpace(13, SpaceType.PROP, "Spruce Way", 180, 70, "Orange", 100));
        Spaces.add(CreateSpace(14, SpaceType.CHANCE, "Chance", null, null, null, null));
        Spaces.add(CreateSpace(15, SpaceType.PROP, "Willow Court", 200, 80, "Orange", 100));

        this.spaces = Spaces;

        this.goIndex = 0;
        this.jailIndex = 8;
    }

    //! I know there are unused, but good to have them?
    public int getBoardSize() {
        return this.spaces.size();
    }

    public BoardSpace getBoardSpace(int index) {
        return this.spaces.get(index);
    }
}
