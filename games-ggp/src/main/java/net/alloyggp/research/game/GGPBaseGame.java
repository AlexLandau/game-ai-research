package net.alloyggp.research.game;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.factory.GdlFactory;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.ruleengine.GameDescriptionException;
import org.ggp.base.util.ruleengine.RuleEngine;
import org.ggp.base.util.ruleengine.RuleEngineState;
import org.ggp.base.util.ruleengine.Translator;
import org.ggp.base.util.ruleengine.prover.ProverRuleEngineFactory;
import org.ggp.base.util.statemachine.sancho.ForwardDeadReckonPropnetRuleEngine;
import org.ggp.base.util.statemachine.sancho.SanchoRuleEngineFactory;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;

import net.alloyggp.research.GameState;
import net.alloyggp.research.GameTreeProvider;
import net.alloyggp.research.Move;

public final class GGPBaseGame implements GameTreeProvider {
    public class GGPBaseMove<M> extends Move {
        public final M move;
        private final Translator<M, ?> translator;

        public GGPBaseMove(M move, Translator<M, ?> translator) {
            this.move = move;
            this.translator = translator;
        }

        private transient volatile GdlTerm gdlMove;
        private GdlTerm getGdlMove() {
            if (gdlMove == null) {
                gdlMove = translator.getGdlMove(move);
            }
            return gdlMove;
        }

        @Override
        public String getName() {
            return getGdlMove().toString();
        }

        @Override
        public String toString() {
            return getGdlMove().toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((getGdlMove() == null) ? 0 : getGdlMove().hashCode());
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
            if (getGdlMove() == null) {
                if (other.getGdlMove() != null)
                    return false;
            } else if (!getGdlMove().equals(other.getGdlMove()))
                return false;
            return true;
        }

        private GGPBaseGame getOuterType() {
            return GGPBaseGame.this;
        }
    }

    public final class GGPBaseGameState<NativeMove, State extends RuleEngineState<NativeMove, State>> implements GameState {
        private final RuleEngine<NativeMove, State> ruleEngine;
        private final State state;

        private GGPBaseGameState(RuleEngine<NativeMove, State> ruleEngine, State state) {
            this.ruleEngine = ruleEngine;
            this.state = state;
        }

        @Override
        public Move getMoveWithName(int roleIndex, String name) {
            try {
                GdlTerm term = GdlFactory.createTerm(name);
                NativeMove nativeMove = ruleEngine.getTranslator().getNativeMove(this.state, roleIndex, term);

                return new GGPBaseMove<>(nativeMove, ruleEngine.getTranslator());
            } catch (SymbolFormatException e) {
                throw new RuntimeException("Couldn't parse " + name + " as a GDL move", e);
            }
        }

        @Override
        public List<Move> getPossibleMovesForRole(int role) {
            try {
                return ruleEngine.getLegalMoves(state, role).stream()
                        .map(nativeMove -> new GGPBaseMove<>(nativeMove, ruleEngine.getTranslator()))
                        .collect(Collectors.toList());
            } catch (GameDescriptionException e) {
                throw new RuntimeException("Error in GGP-Base game " + gameKey, e);
            }
        }

        @Override
        public GameState getNextState(List<Move> movesTaken) {
            try {
                State nextState = ruleEngine.getNextState(this.state, toNativeMoves(movesTaken));

                return new GGPBaseGameState<>(ruleEngine, nextState);
            } catch (GameDescriptionException e) {
                throw new RuntimeException("Error in GGP-Base game " + gameKey, e);
            }
        }

        private List<NativeMove> toNativeMoves(List<Move> moves) {
            return moves.stream()
                    .map(move -> (GGPBaseMove<NativeMove>) move)
                    .map(move -> move.move)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean isTerminal() {
            return ruleEngine.isTerminal(state);
        }

        @Override
        public double getOutcomeForRole(int role) {
            try {
                int intValue = ruleEngine.getGoal(state, role);
                return intValue / 100.0;
            } catch (GameDescriptionException e) {
                throw new RuntimeException("Error in GGP-Base game " + gameKey, e);
            }
        }

        @Override
        public int getNumRoles() {
            return ruleEngine.getNumRoles();
        }
    }

    private final String gameKey;
    private final Supplier<RuleEngine<?, ?>> ruleEngineSupplier;

    private GGPBaseGame(String gameKey,
            Supplier<RuleEngine<?, ?>> ruleEngineSupplier) {
        this.gameKey = gameKey;
        this.ruleEngineSupplier = ruleEngineSupplier;
    }

    public static GameTreeProvider usingProver(String gameKey) {
        Supplier<RuleEngine<?, ?>> ruleEngineSupplier = () -> {
            List<Gdl> rules = GameRepository.getDefaultRepository().getGame(gameKey).getRules();

            return ProverRuleEngineFactory.createNormal().buildEngineForRules(rules);
        };

        return new GGPBaseGame(gameKey, ruleEngineSupplier);
    }

    private static final ThreadLocal<ForwardDeadReckonPropnetRuleEngine> SANCHO_ENGINE_CACHE = new ThreadLocal<>();
    private static final ThreadLocal<String> SANCHO_ENGINE_CACHE_KEY = new ThreadLocal<>();

    public static GameTreeProvider usingSanchoEngine(String gameKey) {
        Supplier<RuleEngine<?, ?>> ruleEngineSupplier = () -> {
            if (gameKey.equals(SANCHO_ENGINE_CACHE_KEY.get())) {
                return SANCHO_ENGINE_CACHE.get();
            } else {
                SANCHO_ENGINE_CACHE.remove(); // just in case this reduces memory pressure
                List<Gdl> rules = GameRepository.getDefaultRepository().getGame(gameKey).getRules();
                // Note: This takes 5+ seconds.
                ForwardDeadReckonPropnetRuleEngine ruleEngine = SanchoRuleEngineFactory.INSTANCE.buildEngineForRules(rules);
                SANCHO_ENGINE_CACHE_KEY.set(gameKey);
                SANCHO_ENGINE_CACHE.set(ruleEngine);
                return ruleEngine;
            }
        };

        return new GGPBaseGame(gameKey, ruleEngineSupplier);
    }

    @Override
    public GameState getInitialState() {
        RuleEngine<?, ?> ruleEngine = ruleEngineSupplier.get();
        return getInitialState(ruleEngine);
    }

    private <M, S extends RuleEngineState<M, S>> GameState getInitialState(RuleEngine<M, S> ruleEngine) {
        return new GGPBaseGameState<>(ruleEngine, ruleEngine.getInitialState());
    }
}
