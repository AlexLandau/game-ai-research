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
 */
// TODO: Unit test to ensure no two have same display name
public enum Game {
    BLOCKER("Blocker", GGPBaseGame.fromProver("blocker"), Simultaneous.YES),
    TIC_TAC_TOE("Tic-tac-toe", GGPBaseGame.fromProver("ticTacToe"), Simultaneous.NO),
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
