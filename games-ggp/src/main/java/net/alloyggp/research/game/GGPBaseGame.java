package net.alloyggp.research.game;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.sancho.SanchoStateMachineFactory;

import net.alloyggp.research.GameState;
import net.alloyggp.research.GameTreeProvider;
import net.alloyggp.research.Move;

public final class GGPBaseGame implements GameTreeProvider {
    public class GGPBaseMove extends Move {
        public final org.ggp.base.util.statemachine.Move move;

        public GGPBaseMove(org.ggp.base.util.statemachine.Move move) {
            this.move = move;
        }

        @Override
        public String getName() {
            return move.toString();
        }

        @Override
        public String toString() {
            return move.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((move == null) ? 0 : move.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            GGPBaseMove other = (GGPBaseMove) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (move == null) {
                if (other.move != null)
                    return false;
            } else if (!move.equals(other.move))
                return false;
            return true;
        }

        private GGPBaseGame getOuterType() {
            return GGPBaseGame.this;
        }
    }

    public final class GGPBaseGameState implements GameState {
        private final StateMachine stateMachine;
        private final MachineState state;

        private GGPBaseGameState(StateMachine stateMachine,
                MachineState state) {
            this.stateMachine = stateMachine;
            this.state = state;
        }

        @Override
        public Move getMoveWithName(String name) {
            return new GGPBaseMove(org.ggp.base.util.statemachine.Move.create(name));
        }

        @Override
        public List<Move> getPossibleMovesForRole(int role) {
            try {
                return stateMachine.getLegalMoves(state, stateMachine.getRoles().get(role)).stream()
                        .map(ggpBaseMove -> new GGPBaseMove(ggpBaseMove))
                        .collect(Collectors.toList());
            } catch (MoveDefinitionException e) {
                throw new RuntimeException("Error in GGP-Base game " + gameKey, e);
            }
        }

        @Override
        public GameState getNextState(List<Move> movesTaken) {
            List<org.ggp.base.util.statemachine.Move> ggpBaseMoves = movesTaken.stream()
                .map(move -> ((GGPBaseMove) move).move)
                .collect(Collectors.toList());
            try {
                MachineState nextState = stateMachine.getNextState(state, ggpBaseMoves);
                return new GGPBaseGameState(stateMachine, nextState);
            } catch (TransitionDefinitionException e) {
                throw new RuntimeException("Error in GGP-Base game " + gameKey, e);
            }
        }

        @Override
        public boolean isTerminal() {
            return stateMachine.isTerminal(state);
        }

        @Override
        public double getOutcomeForRole(int role) {
            try {
                int intValue = stateMachine.getGoal(state, stateMachine.getRoles().get(role));
                return intValue / 100.0;
            } catch (GoalDefinitionException e) {
                throw new RuntimeException("Error in GGP-Base game " + gameKey, e);
            }
        }
    }

    private final String gameKey;
    private final Supplier<StateMachine> stateMachineSupplier;

    private GGPBaseGame(String gameKey,
            Supplier<StateMachine> stateMachineSupplier) {
        this.gameKey = gameKey;
        this.stateMachineSupplier = stateMachineSupplier;
    }

    public static GameTreeProvider usingProver(String gameKey) {
        Supplier<StateMachine> stateMachineSupplier = () -> {
            List<Gdl> rules = GameRepository.getDefaultRepository().getGame(gameKey).getRules();

            ProverStateMachine stateMachine = new ProverStateMachine();
            stateMachine.initialize(rules);
            return stateMachine;
        };

        return new GGPBaseGame(gameKey, stateMachineSupplier);
    }

    private static final ThreadLocal<StateMachine> SANCHO_ENGINE_CACHE = new ThreadLocal<StateMachine>();
    private static final ThreadLocal<String> SANCHO_ENGINE_CACHE_KEY = new ThreadLocal<String>();

    public static GameTreeProvider usingSanchoEngine(String gameKey) {
        Supplier<StateMachine> stateMachineSupplier = () -> {
            if (gameKey.equals(SANCHO_ENGINE_CACHE_KEY.get())) {
                return SANCHO_ENGINE_CACHE.get();
            } else {
                SANCHO_ENGINE_CACHE.remove(); // just in case this reduces memory pressure
                List<Gdl> rules = GameRepository.getDefaultRepository().getGame(gameKey).getRules();
                // Note: This takes 5+ seconds.
                StateMachine stateMachine = SanchoStateMachineFactory.INSTANCE.buildInitializedForRules(rules);
                SANCHO_ENGINE_CACHE_KEY.set(gameKey);
                SANCHO_ENGINE_CACHE.set(stateMachine);
                return stateMachine;
            }
        };

        return new GGPBaseGame(gameKey, stateMachineSupplier);
    }

    @Override
    public GameState getInitialState() {
        StateMachine stateMachine = stateMachineSupplier.get();
        return new GGPBaseGameState(stateMachine, stateMachine.getInitialState());
    }
}
