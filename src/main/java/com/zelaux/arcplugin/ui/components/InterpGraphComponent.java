package com.zelaux.arcplugin.ui.components;

import arc.math.Interp;
import arc.struct.Bits;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class InterpGraphComponent extends JPanel implements MouseWheelListener, KeyListener {
    final static Subdivision[] mySubdivisions = processSubdivisions(
//            new Subdivision(10,ColorContainer::getSubGridColor),
            new Subdivision(8, it -> JBColor.GRAY),
            new Subdivision(4, it -> JBColor.GRAY)
    );
    final static int deltaDivisionStroke = 1;
    public final int myWidth;
    public final int myHeight;
    public float stroke = 3f;
    public float fontSize = 40f;
    private double windowX, windowY, windowWidth, windowHeight;
    private Interp function;
    private BufferedImage buff;
    private Graphics2D g2d;
    private Point mousePt;
    private InterpGraphComponentColors colors = new InterpGraphComponentColors();

    public InterpGraphComponent(Interp function) {
        this(function, 1024, 768);
    }

    public InterpGraphComponent(Interp function, int width, int height) {
        this.function = function;
        this.myWidth = width;
        this.myHeight = height;
        addMouseWheelListener(this);
//        addKeyListener(this);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mousePt = e.getPoint();
                repaint();
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - mousePt.x;
                int dy = e.getY() - mousePt.y;
                windowX -= dx / (double) width * windowWidth;
                windowY += dy / (double) height * windowHeight;
                mousePt = e.getPoint();
                repaint();
            }
        });
        setFocusable(true);
        requestFocusInWindow();
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        buff = ImageUtil.createImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2d = buff.createGraphics();

        windowX = 0.5;
        windowY = 0.5;
        windowHeight = 1.1;
        windowWidth = windowHeight * width / height;
    }

    protected static Subdivision[] processSubdivisions(Subdivision... subdivisions) {
        for (Subdivision subdivision : subdivisions) {
            for (Subdivision b : subdivisions) {
                if (b == subdivision) continue;
                if (subdivision.subdivision % b.subdivision == 0) {
                    int step = subdivision.subdivision / b.subdivision;
                    for (int i = 0; i < subdivision.subdivision; i += step) {
                        subdivision.filter.clear(i);
                    }
                }
            }
        }
        return subdivisions;
    }

    public Interp getFunction() {
        return function;
    }

    public void setFunction(Interp function) {
        this.function = function;
        repaint();
    }

    @SuppressWarnings("UseJBColor")
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g2d.setColor(colors.getBackgroundColor());
        g2d.fillRect(0, 0, myWidth, myHeight);
        synchronized (this) {
            drawSync();
        }
        g.drawImage(buff, 0, 0, null);
    }

    private void drawSync() {
        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();

        for (int x = 0; x < myWidth; x++) {
            double xx = toRealX(x);

            double yy = 0.0;
            if (function != null) yy = function.apply((float) xx);

            double scaledX = x;
            double scaledY = toScreenY(yy);
            scaledY = Math.min(Math.max(scaledY, -5), myHeight + 5);

            xs.add(scaledX);
            ys.add(scaledY);
        }

        int[] xa = new int[xs.size()];
        int[] ya = new int[ys.size()];
        for (int i = 0; i < xa.length; i++) {
            xa[i] = xs.get(i).intValue();
        }
        for (int i = 0; i < ya.length; i++) {
            ya[i] = ys.get(i).intValue();
        }
        int xAxisY = toScreenY(0.0);
        int yAxisX = toScreenX(0.0);

        drawDivisions:
        {

            Color fontColor = colors.getFontColor();
            Color lineColor = colors.getMainMarkersColor();
            Color gridColor = colors.getMainGridColor();
//            int stroke = (int) (10 / 3f * this.stroke);
            int hstroke = (int) (stroke / 2);

            int startY = ceil(toRealY(myHeight));
            int endY = ceil(toRealY(0));

            int startX = ceil(toRealX(0));
            int endX = ceil(toRealX(myWidth));
            int counter = mySubdivisions.length + 1;
            for (Subdivision subdivision : mySubdivisions) {
                counter--;

                g2d.setColor(subdivision.colorProvider.getColor(colors));
                g2d.setStroke(new BasicStroke(Math.max(1, stroke - counter * deltaDivisionStroke), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
//                int screenY = toScreenY(0);
//                int screenX = toScreenX(0);

                for (int curY = startY - 1, curX = startX - 1; curY <= endY + 1 || curX <= endX + 1; curY -= (curX++ - curX)/*curX++, curY++*/) {
                    int screenY = toScreenY(curY);
                    int screenX = toScreenX(curX);
                    int deltaY = toScreenY(curY + 1) - screenY;
                    int deltaX = toScreenX(curX + 1) - screenX;
                    for (int i = 1; i < subdivision.subdivision; i++) {
//                    if(!subdivision.filter.get(i))continue ;
                        int y = screenY + deltaY * i / subdivision.subdivision;
                        g2d.drawLine(0, y, myWidth, y);
                        int x = screenX + deltaX * i / subdivision.subdivision;
                        g2d.drawLine(x, 0, x, myHeight);
                    }
                }
            }
            g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
            for (int curY = startY - 1, curX = startX - 1; curY <= endY + 1 || curX <= endX + 1; curY -= (curX++ - curX)/*curX++, curY++*/) {
                int screenY = toScreenY(curY);
                int screenX = toScreenX(curX);

                if (curY != 0) {
                    g2d.setColor(gridColor);
                    g2d.drawLine(0, screenY, myWidth, screenY);
                    g2d.setColor(lineColor);
                    g2d.drawLine(yAxisX - hstroke, screenY, yAxisX + hstroke, screenY);
                    g2d.setColor(fontColor);
                    g2d.drawString(String.valueOf(curY), yAxisX - hstroke - 10, screenY-fontSize / 2f);
                }
                if (curX != 0) {
                    g2d.setColor(gridColor);
                    g2d.drawLine(screenX, 0, screenX, myHeight);
                    g2d.setColor(lineColor);
                    g2d.drawLine(yAxisX - hstroke, screenY, yAxisX + hstroke, screenY);
                    g2d.setColor(fontColor);
                    g2d.drawString(String.valueOf(curX), screenX + fontSize / 2f, xAxisY - hstroke - 10);
                }
            }
        }
        gridOutline:
        {
//            int stroke = (int) (10 / 3f * this.stroke);
//            int hstroke = stroke / 2;
//            Color lineColor = colors.getMainMarkersColor();
//            Color gridColor = colors.getMainGridColor();
            /*//Y line
            g2d.setColor(gridColor);
            int screenY = toScreenY(1);
            g2d.drawLine(0, screenY, myWidth, screenY);

            g2d.setColor(lineColor);
            g2d.drawLine(yAxisX - hstroke, screenY, yAxisX + hstroke, screenY);

            //X line
            int screenX = toScreenX(1);

            g2d.setColor(gridColor);
            g2d.drawLine(screenX, 0, screenX, myHeight);
            g2d.setColor(lineColor);
            g2d.drawLine(screenX, xAxisY - hstroke, screenX, xAxisY + hstroke);*/
        }
        axis:
        {
            g2d.setColor(colors.getAxisLineColor());
            g2d.drawLine(0, xAxisY, myWidth, xAxisY);
            g2d.drawLine(yAxisX, 0, yAxisX, myHeight);
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(colors.getMainLineColor());
        g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
        g2d.drawPolyline(xa, ya, xa.length);

        g2d.setFont(new Font("courier new", Font.ITALIC, (int) ((fontSize * 2f) / Math.sqrt(windowWidth))));
        int fontHeight = g2d.getFontMetrics().getHeight();
        g2d.setColor(colors.getFontColor());

//            g2d.drawString("x", 0, xAxisY + fontHeight);
//            g2d.drawString("y", yAxisX - fontSize, fontHeight * 0.5f);

    }

    private int ceil(double x) {
        return (int) x;
//        return (int)((x*2+1)/2);
    }

    private int round(double x) {
        return (int) ((x * 2 + 1) / 2);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private double bottom() {
        return windowY - halfWindowHeight();
    }

    private double right() {
        return windowX - halfWindowWidth();
    }

    private double toRealX(int screenX) {
        return screenX / (double) myWidth * windowWidth + right();
    }

    private double toRealY(int screenY) {
        return (myHeight - screenY) / (double) myHeight * windowHeight + bottom();
    }

    private int toScreenX(double realX) {
        return (int) ((realX - right()) / windowWidth * myWidth);
    }

    private int toScreenY(double realY) {
        return myHeight - (int) ((realY - bottom()) / windowHeight * myHeight);
    }

    private double halfWindowWidth() {
        return windowWidth / 2.0;
    }

    private double halfWindowHeight() {
        return windowHeight / 2.0;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double scale = Math.pow(1.15, e.getPreciseWheelRotation());
        double mxReal = toRealX(e.getX());
        double myReal = toRealY(e.getY());
        double sx = (windowX - mxReal) / windowWidth;
        double sy = (windowY - myReal) / windowHeight;
        windowWidth *= scale;
        windowHeight *= scale;
        windowX = mxReal + sx * windowWidth;
        windowY = myReal + sy * windowHeight;
        repaint();
    }

    public static final class Subdivision {
        public final int subdivision;
        public final Bits filter = new Bits();
        public final ColorProvider colorProvider;

        public Subdivision(int subdivision, ColorProvider colorProvider) {
            this.subdivision = subdivision;
            this.colorProvider = colorProvider;
            for (int i = 0; i < subdivision; i++) {
                filter.set(i, true);
            }
        }

        @FunctionalInterface
        public interface ColorProvider {
            Color getColor(InterpGraphComponentColors container);
        }
    }
}
