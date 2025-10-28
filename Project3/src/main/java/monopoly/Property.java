package monopoly;

public class Property {
    public String name;
    public int price;
    public int baseRent;
    public String colorSet;
    public int housePrice;
    
    public Player owner;
    public int houses;
    public boolean hasHotel;

    public Property(String name, int price, int rent, String colorSet, int housePrice) {
        this.name = name;
        this.price = price;
        this.baseRent = rent;
        this.colorSet = colorSet;
        this.housePrice = housePrice;
        this.owner = null;
        this.houses = 0;
        this.hasHotel = false;
    }

    public boolean isOwned() {
        return owner != null;
    }

    public void setOwner(Player newOwner) {
        this.owner = newOwner;
    }

    public void releaseOwnership() {
        this.owner = null;
        this.houses = 0;
        this.hasHotel = false;
    }

    public int getRent() {
        if (hasHotel) {
            return baseRent * 6;
        }
        return baseRent * (1 + houses);
    }

    public boolean canBuildHouse() {
        return owner != null && houses < 4 && !hasHotel;
    }

    public boolean canBuildHotel() {
        return owner != null && houses == 4 && !hasHotel;
    }

    public void buildHouse() {
        if (canBuildHouse()) {
            houses++;
        }
    }

    public void buildHotel() {
        if (canBuildHotel()) {
            hasHotel = true;
        }
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public Player getOwner() {
        return owner;
    }
}
