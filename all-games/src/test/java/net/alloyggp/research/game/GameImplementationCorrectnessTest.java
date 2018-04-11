package net.alloyggp.research.game;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import net.alloyggp.research.GameState;
import net.alloyggp.research.GameTreeProvider;
import net.alloyggp.research.Move;
import net.alloyggp.research.game.impl.SheepAndWolf;

public class GameImplementationCorrectnessTest {
    @Ignore("Intended to be run manually")
    @Test
    public void testSheepAndWolf() {
        testGameDescriptionsEqual(GGPBaseGame.usingSanchoEngine("sheepAndWolf"),
                new SheepAndWolf(),
                10000);
    }

    private void testGameDescriptionsEqual(GameTreeProvider reference, GameTreeProvider candidate, int numTests) {
        for (int i = 0; i < numTests; i++) {
            Random random = new RandomAdaptor(new MersenneTwister(i));
            GameState refState = reference.getInitialState();
            GameState canState = candidate.getInitialState();

            Assert.assertEquals(refState.getNumRoles(), canState.getNumRoles());

            while (!refState.isTerminal()) {
                Assert.assertFalse(canState.isTerminal());

                List<Move> chosenRefMoves = assertLegalMovesEqual(refState, canState, random);
                List<Move> chosenCanMoves = translateMoves(chosenRefMoves, canState);

                refState = refState.getNextState(chosenRefMoves);
                canState = canState.getNextState(chosenCanMoves);
            }

            Assert.assertTrue(canState.toString(), canState.isTerminal());

            List<Double> refOutcomes = refState.getOutcomes();
            List<Double> canOutcomes = canState.getOutcomes();
            Assert.assertEquals(refOutcomes, canOutcomes);
        }
    }

    private List<Move> translateMoves(List<Move> chosenRefMoves, GameState canState) {
        List<Move> chosenCanMoves = Lists.newArrayList();
        for (int r = 0; r < canState.getNumRoles(); r++) {
            chosenCanMoves.add(canState.getMoveWithName(r, chosenRefMoves.get(r).getName()));
        }
        return chosenCanMoves;
    }

    private List<Move> assertLegalMovesEqual(GameState refState, GameState canState, Random random) {
        List<Move> chosenRefMoves = Lists.newArrayList();
        for (int r = 0; r < refState.getNumRoles(); r++) {
            List<Move> refMoves = refState.getPossibleMovesForRole(r);
            List<Move> canMoves = canState.getPossibleMovesForRole(r);

            int chosenIndex = random.nextInt(refMoves.size());
            chosenRefMoves.add(refMoves.get(chosenIndex));

            Set<String> refMoveNames = refMoves.stream().map(move -> move.getName()).collect(Collectors.toSet());
            Assert.assertEquals(refMoves.size(), refMoveNames.size());
            Set<String> canMoveNames = canMoves.stream().map(Move::getName).collect(Collectors.toSet());
            Assert.assertEquals(canMoves.size(), canMoveNames.size());

            Assert.assertEquals(refMoveNames, canMoveNames);
        }
        return chosenRefMoves;
    }
}
