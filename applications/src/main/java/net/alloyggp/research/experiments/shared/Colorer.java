package net.alloyggp.research.experiments.shared;

import java.awt.Color;

public interface Colorer {
    Color getEmptyColor();
    Color getP1TextColor();
    Color getP2TextColor();
    // Scale of 0.0 to 1.0
    Color getColor(double player1Avg);


    public static final Colorer MUTED_RED_WHITE_BLUE = new Colorer() {
        @Override
        public Color getEmptyColor() {
            return Color.BLACK;
        }

        @Override
        public Color getColor(double player1Avg) {
            if (player1Avg < 0.5) {
                // 127.5 at 0, 255 at 0.5
                int val = (int) (player1Avg * 0.6 * 255.0 + 127.5 * 1.4);
                // light red at 0, white at 0.5
                return new Color(255, val, val);
            } else {
                // 255 at 0.5, 127.5 at 1.0
                int val = (int) ((1.0 - player1Avg) * 0.6 * 255.0 + 127.5 * 1.4);
                // white at 0.5, light blue at 1.0
                return new Color(val, val, 255);
            }
        }

        @Override
        public Color getP1TextColor() {
            return Color.BLACK;
        }

        @Override
        public Color getP2TextColor() {
            return Color.BLACK;
        }
    };


    public static final Colorer GRAYSCALE_COLORER = new Colorer() {
        @Override
        public Color getEmptyColor() {
            return Color.RED;
        }

        @Override
        public Color getColor(double player1Avg) {
            int val = (int) (player1Avg * 255.0);
            return new Color(val, val, val);
        }

        @Override
        public Color getP1TextColor() {
            return Color.BLACK;
        }

        @Override
        public Color getP2TextColor() {
            return Color.WHITE;
        }
    };

    public static final Colorer RED_WHITE_BLUE_COLORER = new Colorer() {
        @Override
        public Color getEmptyColor() {
            return Color.BLACK;
        }

        @Override
        public Color getColor(double player1Avg) {
            if (player1Avg < 0.5) {
                // 0 at 0, 255 at 0.5
                int val = (int) (player1Avg * 2 * 255.0);
                // red at 0, white at 0.5
                return new Color(255, val, val);
            } else {
                // 255 at 0.5, 0 at 1.0
                int val = (int) ((1.0 - player1Avg) * 2 * 255.0);
                // white at 0.5, blue at 1.0
                return new Color(val, val, 255);
            }
        }

        @Override
        public Color getP1TextColor() {
            return Color.WHITE;
        }

        @Override
        public Color getP2TextColor() {
            return Color.WHITE;
        }
    };

    public static final Colorer RED_GRAY_BLUE_COLORER = new Colorer() {
        @Override
        public Color getEmptyColor() {
            return Color.BLACK;
        }

        @Override
        public Color getColor(double player1Avg) {
            if (player1Avg < 0.5) {
                // 0 at 0, 255/3 at 0.5
                int val = (int) (player1Avg * 2 * 255.0 / 3.0);
                // red at 0, white at 0.5
                return new Color(255 - (2*val), val, val);
            } else {
                // 255 at 0.5, 0 at 1.0
                double val = ((1.0 - player1Avg) * 2 * 255.0 / 3.0);
                // white at 0.5, blue at 1.0
                return new Color((int) val, (int) val, (int) (255 - (2*val)));
            }
        }

        @Override
        public Color getP1TextColor() {
            return Color.WHITE;
        }

        @Override
        public Color getP2TextColor() {
            return Color.WHITE;
        }
    };

}
