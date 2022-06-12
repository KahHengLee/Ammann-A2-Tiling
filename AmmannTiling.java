package com.company;

import java.awt.*;
import java.util.List;
import java.awt.geom.Path2D;
import java.util.*;
import javax.swing.*;
import static java.lang.Math.*;
import static java.util.stream.Collectors.toList;

public class AmmannTiling extends JPanel {
    // ignores missing hash code
    class Tile {
        double x, y, angle, size, sign;
        Type type;

        Tile(Type t, double x, double y, double a, double s, double z) {
            type = t; //which tile - Type.Big, Type.Small
            this.x = x; // Coordinates
            this.y = y; //-ve Y coordinate
            angle = a; //rotation, starting angle
            size = s; //scale
            sign = z;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Tile) {
                Tile t = (Tile) o;
                return type == t.type && x == t.x && y == t.y && angle == t.angle;
            }
            return false;
        }
    }

    enum Type {
        Big, Small
    }

    static final double G = (1 + sqrt(5)) / 2; // golden ratio
    static final double T = toRadians(36); // theta

    List<Tile> tiles = new ArrayList<>();

    public AmmannTiling() {
        int w = 700, h = 450;
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.white);

        tiles = deflateTiles(setupPrototiles(w, h), 4);//generation
    }

    List<Tile> setupPrototiles(int w, int h) {
        List<Tile> proto = new ArrayList<>();

        // sun, T=36 degree
        //for (double a = PI / 2 + T; a < 3 * PI; a += 2 * T) //generating starting condition
        proto.add(new Tile(Type.Small, w / 2, h / 2, 0, w / 2.5, 1)); //a = starting angle,  Type.Small = using which tile


        return proto;
    }

    List<Tile> deflateTiles(List<Tile> tls, int generation) {
        if (generation <= 0)
            return tls;

        List<Tile> next = new ArrayList<>();

        for (Tile tile : tls) {
            double x = tile.x, y = tile.y, a = tile.angle, nx, ny, mx, my;
            double size = tile.size / sqrt(G);

            if (tile.type == Type.Small) { //adding new tiles information into arrays for next generation
                next.add(new Tile(Type.Big, x, y, a, size, tile.sign));

            } else {

                nx = tile.x + (Math.pow(G,2.5) * tile.size - G * size) * cos(tile.angle);
                ny = tile.y - (Math.pow(G,2.5) * tile.size - G * size) * sin(tile.angle);
                mx = tile.x + Math.pow(G,2.5) * tile.size * cos(tile.angle);
                my = tile.y - Math.pow(G,2.5) * tile.size * sin(tile.angle);
                next.add(new Tile(Type.Small, nx, ny, tile.angle - PI, size,-1 * tile.sign));
                next.add(new Tile(Type.Big, mx, my, tile.angle - tile.sign * PI/2, size, tile.sign));

                }
            }

        // remove duplicates
        tls = next.stream().distinct().collect(toList());

        return deflateTiles(tls, generation - 1);
    }

    void drawTiles(Graphics2D g) {
        double[] dist = {G, sqrt(G*G+G), sqrt((G+1)*(G+1)+G), sqrt((G+1)*(G+1)+(G+2*(G*G)+G*G*G)), Math.pow(G,2.5)};
        double[] sz = {1, 1/sqrt(G)};
        double[] ang = {-Math.PI/2, -Math.PI/2+atan(1/sqrt(G)), -Math.PI/2+atan(sqrt(G)/(G+1)), -Math.PI/2+atan(sqrt(G)), 0};
        for (AmmannTiling.Tile tile : tiles) { //for loop of all tiles existing
            Path2D path = new Path2D.Double();
            path.moveTo(tile.x, tile.y);

            int ord = tile.type.ordinal(); //ord =0 if type is kite
            for (int i = 0; i < 5; i++) {
                double x = tile.x + dist[i] * sz[ord] * tile.size * cos(tile.sign * ang[i]+tile.angle); //important to calculate which 3 other points
                double y = tile.y - dist[i] * sz[ord] * tile.size * sin(tile.sign * ang[i]+tile.angle); //understand this
                path.lineTo(x, y);
            }
            path.closePath();
            g.setColor(ord == 0 ? Color.orange : Color.yellow);
            g.fill(path); //colour of inside
            g.setColor(Color.darkGray);
            g.draw(path); //colour of boundary
        }
    }

    @Override
    public void paintComponent(Graphics og) {
        super.paintComponent(og);
        Graphics2D g = (Graphics2D) og;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(150, -50);//change this number to move image around
        g.scale(0.4,0.4); //change this number to change the size of the image
        drawTiles(g);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("Ammann Tiling");
            f.setResizable(true); //resize window
            f.add(new AmmannTiling(), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}