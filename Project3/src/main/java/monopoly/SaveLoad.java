package monopoly;

import java.io.*;
import java.util.*;

public class SaveLoad {
    //? Save Data
    public static boolean Save(
            String fileName,
            Board board,
            List<Player> players,
            int currentPlayerIndex
    ) {
        String path;

        try {
            path = "savedata/"+fileName+".txt";
        } catch (Exception e) {
            System.out.println("File name must contain valid characters!");
            return false;
        }

        try (PrintWriter Output = new PrintWriter(new FileWriter(path))) {
            //! Save last known "current" turn (Line 1)
            Output.println(currentPlayerIndex);

            //! Save # of players/size (Line 2)
            Output.println(players.size());

            //! Each line saved is player's data, formatted: name|cash|position|inJail|doubles|isBankrupt (Line 3)
            for (Player p : players) {
                Output.println(p.name + "|" + p.cash + "|" + p.position + "|" + p.inJail + "|" + p.consecutiveDoubles + "|" + p.isBankrupt);
            }

            //! Save Property data of ALL properties, including who knows it. If unowned, use -1. (Last Line)
            List<String> pairs = new ArrayList<>();

            for (int i = 0; i < board.getSize(); i++) {
                if (board.isProperty(i)) {
                    Property property = board.getProperty(i);
                    int playerIndex = -1;

                    if (property.owner != null) { //? Does property have an owner (player) attached to it? If not, mark as -1 (unowned)
                        playerIndex = players.indexOf(property.owner);
                    }

                    pairs.add(i + ":" + playerIndex);
                }
            }

            Output.println(String.join(",", pairs));
            return true; //! Save success! :)
        } catch (IOException e) {
            return false; //! Save failed! :(
        }
    }

    //? Load Data
    public static boolean Load(
            String fileName,
            Rules[] rulesOut,
            Board[] boardOut,
            List<Player> playersOut,
            int[] currentIndexOut
    ) {
        String path;

        try {
            path = "savedata/"+fileName+".txt";
        } catch (Exception e) {
            System.out.println("File name must contain valid characters!");
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            Rules rules = new Rules();
            Board board = new Board(rules);

            int currentIndex = Integer.parseInt(nonNull(br)); //! Last "Current" Turn (Line 1)
            int players = Integer.parseInt(nonNull(br)); //! Number of players playing (Line 2)

            playersOut.clear();
            for (int i = 0; i < players; i++) {
                String[] parts = nonNull(br).split("\\|"); //! Break up the pieces of data from the line (Line X)

                Player player = new Player(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2])); //! Create player object with cash and position
                player.inJail = Boolean.parseBoolean(parts[3]);
                player.consecutiveDoubles = Integer.parseInt(parts[4]);
                player.isBankrupt = Boolean.parseBoolean(parts[5]);

                playersOut.add(player); //! Once created, load the player up in the list
            }

            //! Set up the properties! (Last Line)
            String propLine = nonNull(br).trim();
            if (!propLine.isEmpty()) { //? Safe check.
                String[] entries = propLine.split(",");

                for (String entry : entries) {
                    String[] kv = entry.split(":");

                    int indexPosition = Integer.parseInt(kv[0]);
                    int playerIndex = Integer.parseInt(kv[1]);

                    if (board.isProperty(indexPosition)) {
                        Property property = board.getProperty(indexPosition);
                        property.owner = (playerIndex >= 0 && playerIndex < playersOut.size()) ? playersOut.get(playerIndex) : null;
                        //! ^ If owned, load the owner's id/index to it. If unowned, leave alone!
                    }
                }
            }

            if (currentIndex < 0 || currentIndex >= playersOut.size()) {
                currentIndex = 0;
            }

            rulesOut[0] = rules;
            boardOut[0] = board;
            currentIndexOut[0] = currentIndex;

            return true;
        } catch (Exception e) {
            System.out.println("Load error: " + e.getMessage());
            return false;
        }
    }

    private static String nonNull(BufferedReader br) throws IOException {
        String s = br.readLine();
        if (s == null) throw new EOFException("Unexpected end of save file");
        return s;
    }
}