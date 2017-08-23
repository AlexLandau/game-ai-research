package net.alloyggp.research.applications;

import net.alloyggp.research.GameState;
import net.alloyggp.research.GameTreeProvider;
import net.alloyggp.research.game.GGPBaseGame;

/*
 * TODO: Other things that we might consider as properties:
 * - If we end up supporting non-zero-sum games: is it truly zero-sum?
 * - Are the only available outcome values 1 and 0?
 * - Number of players, if we end up supporting numbers other than 2
 *
 * TODO: Games to add that aren't working with the Sancho engine:
 * - Atari Go
 */
// TODO: Unit test to ensure no two have same display name
public enum Game {
    BIDDING_TIC_TAC_TOE("Bidding tic-tac-toe", GGPBaseGame.usingSanchoEngine("biddingTicTacToe"), Simultaneous.YES),
    BLOCKER("Blocker", GGPBaseGame.usingSanchoEngine("blocker"), Simultaneous.YES),
    BREAKTHROUGH("Breakthrough", GGPBaseGame.usingSanchoEngine("breakthrough"), Simultaneous.NO),
    BREAKTHROUGH_6x6("Breakthrough (6x6)", GGPBaseGame.usingSanchoEngine("breakthroughSmall"), Simultaneous.NO),
    CONNECT_4_8x6("Connect Four (8x6)", GGPBaseGame.usingSanchoEngine("connectFour"), Simultaneous.NO),
    CONNECT_4_9x6("Connect Four (9x6)", GGPBaseGame.usingSanchoEngine("connectFour_9x6"), Simultaneous.NO),
    DOTS_AND_BOXES("Dots and Boxes (5x5)", GGPBaseGame.usingSanchoEngine("dotsAndBoxes"), Simultaneous.NO),
    ENGLISH_DRAUGHTS("English Draughts", GGPBaseGame.usingSanchoEngine("englishDraughts"), Simultaneous.NO),
    HEX("Hex", GGPBaseGame.usingSanchoEngine("hex"), Simultaneous.NO),
    HEX_PIE("Hex (with pie rule)", GGPBaseGame.usingSanchoEngine("hexPie"), Simultaneous.NO),
    MAJORITIES("Majorities", GGPBaseGame.usingSanchoEngine("majorities"), Simultaneous.NO),
    PENTAGO("Pentago", GGPBaseGame.usingSanchoEngine("pentago"), Simultaneous.NO),
    QUARTO("Quarto", GGPBaseGame.usingSanchoEngine("quarto"), Simultaneous.NO),
    REVERSI("Reversi", GGPBaseGame.usingSanchoEngine("reversi"), Simultaneous.NO),
    TIC_TAC_TOE("Tic-tac-toe", GGPBaseGame.usingSanchoEngine("ticTacToe"), Simultaneous.NO),
    ;
    private final String displayName;
    private final GameTreeProvider treeProvider;
    private final Simultaneous simultaneous;

    private Game(String displayName, GameTreeProvider treeProvider,
            Simultaneous simultaneous) {
        this.displayName = displayName;
        this.treeProvider = treeProvider;
        this.simultaneous = simultaneous;
    }

    public static enum Simultaneous {
        YES,
        NO,
    }

    public String getDisplayName() {
        return displayName;
    }

    public GameState getInitialState() {
        return treeProvider.getInitialState();
    }

    public String getId() {
        return this.name();
    }
}
