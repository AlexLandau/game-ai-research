package net.alloyggp.research.experiments;

import java.awt.Color;

public class Reports {
    private Reports() {
        // Not instantiable
    }

    public static String getCssRgbString(Color color) {
        return "rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+")";
    }

}
