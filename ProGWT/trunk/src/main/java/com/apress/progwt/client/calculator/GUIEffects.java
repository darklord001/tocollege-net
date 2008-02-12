/*
 * Copyright 2008 Jeff Dwyer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.apress.progwt.client.calculator;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

public class GUIEffects {

    private abstract class GUITimer extends Timer {
        public static final int FREQ = 100;

        private int curStep;
        private Element element;
        private int steps;

        public GUITimer(Widget widget, int steps) {
            this.element = widget.getElement();
            this.steps = steps;
            curStep = 0;
        }

        protected abstract void doStep(double pct);

        public Element getElement() {
            return element;
        }

        public int getSteps() {
            return steps;
        }

        @Override
        public void run() {
            doStep(curStep / (double) steps);

            curStep++;
            if (curStep > steps) {
                cancel();
            } else {
                schedule(FREQ);
            }
        }

    }

    private class MoveTimer extends GUITimer {

        private int curX;
        private int curY;

        private int dx;
        private int dy;

        public MoveTimer(Widget widget, int x, int y, int steps) {
            super(widget, steps);
            this.dx = x / steps;
            this.dy = y / steps;
            this.curX = DOM.getIntStyleAttribute(getElement(), "left");
            this.curY = DOM.getIntStyleAttribute(getElement(), "top");
        }

        protected void doStep(double pct) {

            DOM.setIntStyleAttribute(getElement(), "left", curX);
            DOM.setIntStyleAttribute(getElement(), "top", curY);

            curX += dx;
            curY += dy;
        }
    }

    private class OpacityTimer extends GUITimer {

        private double diff;

        private double start;

        public OpacityTimer(Widget widget, double from, double to,
                int steps) {
            super(widget, steps);
            this.start = from;
            this.diff = to - from;

        }

        protected void doStep(double pct) {

            double cur = pct * diff + start;

            String ieStr = "alpha(opacity = " + (int) (cur * 100) + ")";

            DOM.setStyleAttribute(getElement(), "filter", ieStr);
            DOM.setStyleAttribute(getElement(), "-moz-opacity", cur + "");
            DOM.setStyleAttribute(getElement(), "opacity", cur + "");
            DOM.setStyleAttribute(getElement(), "-khtml-opacity", cur
                    + "");
        }
    }

    private static final int HIGHLIGHT_DURATION = 1000;

    private static GUIEffects singleInstance = new GUIEffects();

    /**
     * 
     * @param toAppear
     * @param duration
     */
    public static void appear(Widget toAppear, int duration) {
        opacity(toAppear, .1, 1.0, duration);
    }

    /**
     * Hook into the native close method.
     * 
     * NOTE some eclipse styling errors will occur when these comments
     * begin on the close() line. Each time we save the file, it will push
     * the native code to the right.
     */
    public static native void close()/*-{
                                                                                                                    $wnd.close();
                                                                                                                }-*/;

    public static void fade(Widget w, int duration) {
        opacity(w, 1.0, 0.0, duration);
    }

    public static void fadeAndRemove(Widget w, int removeInX) {
        fade(w, 500);

        removeInXMilSecs(w, removeInX);
    }

    /**
     * Utility to fade & remove a widget after a short time using 2 timers
     * 
     * @param w
     * @param i
     */
    public static void fadeAndRemove(final Widget w, int fadeInX,
            int removeInX) {

        fade(w, fadeInX);

        removeInXMilSecs(w, removeInX);
    }

    /**
     * highligh from FFFE7F to FFFFFF
     * 
     * @param toHighlight
     */
    public static void highlight(Widget toHighlight) {
        highlight(toHighlight, "FF", "FE", "7F", "FF", "FF", "FF");
    }

    private static void highlight(Widget toHighlight, String sR,
            String sG, String sB, String eR, String eG, String eB) {

        highlight(toHighlight, Integer.parseInt(sR, 16), Integer
                .parseInt(sG, 16), Integer.parseInt(sB, 16), Integer
                .parseInt(eR, 16), Integer.parseInt(eG, 16), Integer
                .parseInt(eB, 16), HIGHLIGHT_DURATION);
    }

    /**
     * highlight from start color to end color
     * 
     * @param toHighlight -
     *            the Widget to highlight
     * @param startColor -
     *            start color such as "FFFE7F" note no leading #
     * @param endColor -
     *            end color such as "FFFFFF" note no leading #
     */
    public static void highlight(Widget toHighlight, String startColor,
            String endColor) {

        Log.debug("start color: " + startColor);
        Log.debug("end color: " + endColor);

        final int startR = Integer.parseInt(startColor.substring(0, 2),
                16);
        final int endR = Integer.parseInt(endColor.substring(0, 2), 16);
        final int startG = Integer.parseInt(startColor.substring(2, 4),
                16);
        final int endG = Integer.parseInt(endColor.substring(2, 4), 16);
        final int startB = Integer.parseInt(startColor.substring(4, 6),
                16);
        final int endB = Integer.parseInt(endColor.substring(4, 6), 16);

        highlight(toHighlight, startR, startG, startB, endR, endG, endB,
                HIGHLIGHT_DURATION);
    }

    /**
     * Highlight with rgb parameters split out. To get a smooth transition
     * we must move each color element individually
     * 
     * @param toHighlight
     * @param startR
     * @param startG
     * @param startB
     * @param endR
     * @param endG
     * @param endB
     */
    public static void highlight(Widget toHighlight, final int startR,
            final int startG, final int startB, int endR, int endG,
            int endB, int duration) {
        int steps = duration / MoveTimer.FREQ;

        final int diffR = endR - startR;
        final int diffG = endG - startG;
        final int diffB = endB - startB;

        GUITimer mover = singleInstance.new GUITimer(toHighlight, steps) {

            @Override
            protected void doStep(double pct) {

                int cR = (int) (startR + diffR * pct);
                int cG = (int) (startG + diffG * pct);
                int cB = (int) (startB + diffB * pct);

                StringBuffer sb = new StringBuffer("#");
                sb.append(Integer.toHexString(cR));
                sb.append(Integer.toHexString(cG));
                sb.append(Integer.toHexString(cB));

                // note NOT background-color. the '-' in style names
                // breaks.
                DOM.setStyleAttribute(getElement(), "backgroundColor", sb
                        .toString());
            }
        };

        mover.schedule(100);
    }

    /**
     * options are not safe in Hosted GWT
     * 
     * @param toMove
     * @param i
     * @param x
     * @param cloud_move_sec
     */
    public static void move(Widget toMove, int x, int y, int duration) {

        int steps = duration / MoveTimer.FREQ;

        MoveTimer mover = singleInstance.new MoveTimer(toMove, x, y,
                steps);

        mover.schedule(100);

    }

    public static void opacity(Widget widget, double from, double to,
            int duration) {

        int steps = duration / OpacityTimer.FREQ;

        OpacityTimer opacity = singleInstance.new OpacityTimer(widget,
                from, to, steps);

        opacity.schedule(100);

    }

    /**
     * Utility to remove a widget after a short time, for instance after
     * we Effect.fade()
     * 
     * @param w
     * @param i
     */
    public static void removeInXMilSecs(final Widget w, int i) {
        Timer t = new Timer() {
            public void run() {
                w.removeFromParent();
            }
        };
        t.schedule(i);
    }

}
