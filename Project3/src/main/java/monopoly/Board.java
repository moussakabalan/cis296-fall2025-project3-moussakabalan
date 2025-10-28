package monopoly;

import java.util.HashMap;
import java.util.Map;

public class Board {
    public Rules rules;
    public Map<Integer, Property>  properties = new HashMap<Integer, Property>();

    public Board(Rules rules) {
        this.rules = rules;

        for (int index = 0; index < rules.spaces.size(); index++) {
            Rules.BoardSpace Space = rules.spaces.get(index);

            if (Space.type == Rules.SpaceType.PROP) {
                Property property = new Property(Space.name, Space.price, Space.rent, Space.colorSet, Space.housePrice);
                properties.put(index, property);
            }
        }
    }

    public int getSize() {
        return rules.spaces.size();
    }

    public int getJailIndex() {
        return rules.jailIndex;
    }

    public boolean isProperty(int index) {
        return properties.containsKey(index);
    }

    public Property getProperty(int index) {
        if (!isProperty(index)) {
            return null;
        }

        return properties.get(index);
    }

    public String getSpaceName(int index) {
        return rules.spaces.get(index).name;
    }
}
