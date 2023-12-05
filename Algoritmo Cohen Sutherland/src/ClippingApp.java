import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class ClippingApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Cohen-Sutherland Clipping");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            JPanel panel = new ClippingPanel();
            frame.add(panel);

            frame.setVisible(true);
        });
    }
}

class ClippingPanel extends JPanel {
    private List<Line2D.Double> lines;
    private List<Line2D.Double> clippedSegments;
    private JTextField x1Field, y1Field, x2Field, y2Field;
    private JTextField xMinField, xMaxField, yMinField, yMaxField;

    public ClippingPanel() {
        setLayout(new BorderLayout());

        lines = new ArrayList<>();
        clippedSegments = new ArrayList<>();

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 4));

        x1Field = new JTextField("250");
        y1Field = new JTextField("250");
        x2Field = new JTextField("550");
        y2Field = new JTextField("550");
        xMinField = new JTextField("200");
        xMaxField = new JTextField("600");
        yMinField = new JTextField("200");
        yMaxField = new JTextField("400");

        inputPanel.add(new JLabel("x1:"));
        inputPanel.add(x1Field);
        inputPanel.add(new JLabel("y1:"));
        inputPanel.add(y1Field);
        inputPanel.add(new JLabel("x2:"));
        inputPanel.add(x2Field);
        inputPanel.add(new JLabel("y2:"));
        inputPanel.add(y2Field);

        inputPanel.add(new JLabel("xMin:"));
        inputPanel.add(xMinField);
        inputPanel.add(new JLabel("xMax:"));
        inputPanel.add(xMaxField);
        inputPanel.add(new JLabel("yMin:"));
        inputPanel.add(yMinField);
        inputPanel.add(new JLabel("yMax:"));
        inputPanel.add(yMaxField);

        add(inputPanel, BorderLayout.NORTH);

        JButton addLineButton = new JButton("Adicionar Linha");
        JButton clipButton = new JButton("Recortar");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addLineButton);
        buttonPanel.add(clipButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addLineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double x1 = Double.parseDouble(x1Field.getText());
                double y1 = Double.parseDouble(y1Field.getText());
                double x2 = Double.parseDouble(x2Field.getText());
                double y2 = Double.parseDouble(y2Field.getText());

                Line2D.Double line = new Line2D.Double(x1, y1, x2, y2);
                lines.add(line);
                repaint();
            }
        });

        clipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double xMin = Double.parseDouble(xMinField.getText());
                double xMax = Double.parseDouble(xMaxField.getText());
                double yMin = Double.parseDouble(yMinField.getText());
                double yMax = Double.parseDouble(yMaxField.getText());

                clippedSegments.clear();

                for (Line2D.Double line : lines) {
                    CohenSutherlandClipping clipper = new CohenSutherlandClipping(xMin, xMax, yMin, yMax);
                    List<Line2D.Double> segments = clipper.clip(line);
                    clippedSegments.addAll(segments);
                }

                updateResult();
                repaint();
            }
        });
    }

    private void updateResult() {
        for (int i = 0; i < lines.size(); i++) {
            Line2D.Double line = lines.get(i);
            if (clippedSegments.size() > i) {
                Line2D.Double clipped = clippedSegments.get(i);
                double originalLength = line.getP1().distance(line.getP2());
                double clippedLength = clipped.getP1().distance(clipped.getP2());
                if (originalLength == clippedLength) {
                    System.out.println("Linha " + (i + 1) + " - Totalmente dentro do limite");
                } else {
                    double percentage = (clippedLength / originalLength) * 100;
                    String side = calculateSide(line, clipped);
                    System.out.println("Linha " + (i + 1) + " - Cortada pelo lado: " + side +
                            " - Quantidade cortada: " + clippedLength);
                }
            } else {
                System.out.println("Linha " + (i + 1) + " - Não cortada");
            }
        }
    }

    private String calculateSide(Line2D.Double original, Line2D.Double clipped) {
        double x1 = original.getX1();
        double y1 = original.getY1();
        double x2 = original.getX2();
        double y2 = original.getY2();
        double x = clipped.getX1();
        double y = clipped.getY1();

        double crossProduct = (x2 - x1) * (y - y1) - (y2 - y1) * (x - x1);

        if (crossProduct > 0) {
            return "Direita";
        } else if (crossProduct < 0) {
            return "Esquerda";
        } else {
            if (y1 < y2) {
                return "Cima";
            } else {
                return "Baixo";
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // Desenhe a janela de recorte
        g2d.setColor(Color.BLUE);
        g2d.drawRect((int) Double.parseDouble(xMinField.getText()), (int) Double.parseDouble(yMinField.getText()),
                (int) (Double.parseDouble(xMaxField.getText()) - Double.parseDouble(xMinField.getText())),
                (int) (Double.parseDouble(yMaxField.getText()) - Double.parseDouble(yMinField.getText())));

        // Desenhe todas as linhas
        g2d.setColor(Color.BLACK);
        for (Line2D.Double line : lines) {
            g2d.draw(line);
        }

        // Desenhe os segmentos recortados
        g2d.setColor(Color.RED);
        for (Line2D.Double segment : clippedSegments) {
            g2d.draw(segment);
        }
    }
}

class CohenSutherlandClipping {
    private double xMin, xMax, yMin, yMax;
    private static final int INSIDE = 0;

    public CohenSutherlandClipping(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public List<Line2D.Double> clip(Line2D.Double line) {
        List<Line2D.Double> clippedSegments = new ArrayList<>();
        double x1 = line.getX1();
        double y1 = line.getY1();
        double x2 = line.getX2();
        double y2 = line.getY2();

        int code1 = calculateCode(x1, y1);
        int code2 = calculateCode(x2, y2);

        while (true) {
            if ((code1 | code2) == 0) {
                clippedSegments.add(new Line2D.Double(x1, y1, x2, y2));
                break;
            } else if ((code1 & code2) != 0) {
                break;
            } else {
                int outsideCode = (code1 != INSIDE) ? code1 : code2;
                double newX, newY;

                if ((outsideCode & 8) != 0) {
                    newX = x1 + (x2 - x1) * (yMax - y1) / (y2 - y1);
                    newY = yMax;
                } else if ((outsideCode & 4) != 0) {
                    newX = x1 + (x2 - x1) * (yMin - y1) / (y2 - y1);
                    newY = yMin;
                } else if ((outsideCode & 2) != 0) {
                    newY = y1 + (y2 - y1) * (xMax - x1) / (x2 - x1);
                    newX = xMax;
                } else {
                    newY = y1 + (y2 - y1) * (xMin - x1) / (x2 - x1);
                    newX = xMin;
                }

                if (outsideCode == code1) {
                    x1 = newX;
                    y1 = newY;
                    code1 = calculateCode(x1, y1);
                } else {
                    x2 = newX;
                    y2 = newY;
                    code2 = calculateCode(x2, y2);
                }
            }
        }

        return clippedSegments;
    }

    private int calculateCode(double x, double y) {
        int code = INSIDE;

        if (x < xMin) {
            code |= 1;
        } else if (x > xMax) {
            code |= 2;
        }

        if (y < yMin) {
            code |= 4;
        } else if (y > yMax) {
            code |= 8;
        }

        return code;
    }
}
