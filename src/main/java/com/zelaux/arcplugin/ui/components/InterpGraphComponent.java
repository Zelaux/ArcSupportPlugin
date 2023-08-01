package com.zelaux.arcplugin.ui.components;

import arc.math.*;
import com.intellij.util.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="https://github.com/FuzzyCat444">FuzzyCat444</a>
 */
public class InterpGraphComponent extends JPanel implements MouseWheelListener, KeyListener{
    public final int WIDTH;
    public final int HEIGHT;
    public float stroke = 3f;
    public float fontSize = 40f;
    private Interp function;
    private BufferedImage buff;
    private Graphics2D g2d;
    private double windowX, windowY, windowWidth, windowHeight;
    private Point mousePt;

    public InterpGraphComponent(Interp function){
        this(function, 1024, 768);
    }

    public InterpGraphComponent(Interp function, int WIDTH, int HEIGHT){
        this.function = function;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        addMouseWheelListener(this);
        addKeyListener(this);
        this.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                mousePt = e.getPoint();
                repaint();
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseDragged(MouseEvent e){
                int dx = e.getX() - mousePt.x;
                int dy = e.getY() - mousePt.y;
                windowX -= dx / (double)WIDTH * windowWidth;
                windowY += dy / (double)HEIGHT * windowHeight;
                mousePt = e.getPoint();
                repaint();
            }
        });
        setFocusable(true);
        requestFocusInWindow();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setMaximumSize(new Dimension(WIDTH, HEIGHT));
        buff = ImageUtil.createImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g2d = buff.createGraphics();

        windowX = 0.0;
        windowY = 0.0;
        windowHeight = 2.0;
        windowWidth = windowHeight * WIDTH / HEIGHT;
    }

    public Interp getFunction(){
        return function;
    }

    public void setFunction(Interp function){
        this.function = function;
        repaint();
    }

    @SuppressWarnings("UseJBColor")
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        synchronized(this){
            List<Double> xs = new ArrayList<>();
            List<Double> ys = new ArrayList<>();

            for(int x = 0; x < WIDTH; x++){
                double xx = toRealX(x);

                double yy = 0.0;
                if(function != null) yy = function.apply((float)xx);

                double scaledX = x;
                double scaledY = toScreenY(yy);
                scaledY = Math.min(Math.max(scaledY, -5), HEIGHT + 5);

                xs.add(scaledX);
                ys.add(scaledY);
            }

            int[] xa = new int[xs.size()];
            int[] ya = new int[ys.size()];
            for(int i = 0; i < xa.length; i++){
                xa[i] = xs.get(i).intValue();
            }
            for(int i = 0; i < ya.length; i++){
                ya[i] = ys.get(i).intValue();
            }

            g2d.setColor(Color.BLACK);
            int xAxisY = toScreenY(0.0);
            g2d.drawLine(0, xAxisY, WIDTH, xAxisY);
            int yAxisX = toScreenX(0.0);
            g2d.drawLine(yAxisX, 0, yAxisX, HEIGHT);
            drawDivisions:
            {
                Color gridColor = Color.GRAY;
                Color lineColor = Color.BLACK;
                int stroke = (int)(10 / 3f * this.stroke);
                int hstroke = stroke / 2;

                int startY = (int)toRealY(HEIGHT);
                int endY = (int)toRealY(0);

                int startX = (int)toRealX(0);
                int endX = (int)toRealX(WIDTH);
                for(int curY = startY - 1; curY <= endY + 1; curY++){
                    if(curY == 0) continue;
                    int screenY = toScreenY(curY);


                    g2d.setColor(gridColor);
                    g2d.drawLine(0, screenY, WIDTH, screenY);
                    g2d.setColor(lineColor);
                    g2d.drawLine(yAxisX - hstroke, screenY, yAxisX + hstroke, screenY);
                    String str = curY + "";
//                    int width = g2d.getFontMetrics().stringWidth(str);
                    g2d.drawString(str, yAxisX + hstroke + 10, screenY);
                }
                for(int curX = startX - 1; curX <= endX + 1; curX++){
                    int screenX = toScreenX(curX);
                    if(curX == 0) continue;

                    g2d.setColor(gridColor);
                    g2d.drawLine(screenX, 0, screenX, HEIGHT);
                    g2d.setColor(lineColor);
                    g2d.drawLine(screenX, xAxisY - hstroke, screenX, xAxisY + hstroke);
                    int titleY = xAxisY - hstroke - 10;
//                    if (titleY >=0 && screenX<=)
                    g2d.drawString(curX + "", screenX, titleY);

                }
            }
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(50, 50, 180));
            g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
            g2d.drawPolyline(xa, ya, xa.length);

            g2d.setFont(new Font("courier new", Font.ITALIC, (int)((fontSize * 2f) / Math.sqrt(windowWidth))));
            int fontHeight = g2d.getFontMetrics().getHeight();
            g2d.setColor(Color.BLACK);

            g2d.drawString("x", 0, xAxisY + fontHeight);
            g2d.drawString("y", yAxisX + fontHeight / 4f, fontHeight * 0.5f);
        }

        g.drawImage(buff, 0, 0, null);
    }

    @Override
    public void keyTyped(KeyEvent e){

    }

    @Override
    public void keyPressed(KeyEvent e){

    }


    @Override
    public void keyReleased(KeyEvent e){

    }

    private double bottom(){
        return windowY - halfWindowHeight();
    }

    private double right(){
        return windowX - halfWindowWidth();
    }

    private double toRealX(int screenX){
        return screenX / (double)WIDTH * windowWidth + right();
    }

    private double toRealY(int screenY){
        return (HEIGHT - screenY) / (double)HEIGHT * windowHeight + bottom();
    }

    private int toScreenX(double realX){
        return (int)((realX - right()) / windowWidth * WIDTH);
    }

    private int toScreenY(double realY){
        return HEIGHT - (int)((realY - bottom()) / windowHeight * HEIGHT);
    }

    private double halfWindowWidth(){
        return windowWidth / 2.0;
    }

    private double halfWindowHeight(){
        return windowHeight / 2.0;
    }

    public void setWindow(double x, double y, double width, double height){
        this.windowX = x - width / 2f;
        this.windowY = y - height / 2f;
        this.windowWidth = width;
        this.windowHeight = height;
    }

    public void setWindowCenter(double x, double y, double width, double height){
        this.windowX = x;
        this.windowY = y;
        this.windowWidth = width;
        this.windowHeight = height;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e){
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
}
