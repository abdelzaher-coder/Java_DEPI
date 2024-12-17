package paint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PaintApp extends JFrame {
    private DrawingCanvas canvas;
    private Tool currentTool;
    private Color currentColor = Color.BLACK; 

    public PaintApp() {
        initializeApp();
    }

    private void initializeApp() {
        setTitle("Java Paint App");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas = new DrawingCanvas();
        add(canvas, BorderLayout.CENTER);
        JPanel toolPanel = createToolPanel();
        add(toolPanel, BorderLayout.NORTH);
        setTool(new PencilTool(canvas, currentColor));
        setVisible(true);
    }

    private JPanel createToolPanel() {
        JPanel toolPanel = new JPanel();

        JButton brushButton = createBrushButton();
        JButton shapeButton = createShapeButton();
        JButton colorButton = createColorButton();
        JButton blueBrushButton = createBlueBrushButton();
        JButton eraserButton = createEraserButton();
        JButton clearButton = createClearButton();
        JButton layersButton = createLayersButton();

        toolPanel.add(brushButton);
        toolPanel.add(shapeButton);
        toolPanel.add(colorButton);
        toolPanel.add(blueBrushButton);
        toolPanel.add(eraserButton);
        toolPanel.add(clearButton);
        toolPanel.add(layersButton);

        return toolPanel;
    }

    private JButton createBrushButton() {
        JButton brushButton = new JButton("Brush");
        JPopupMenu brushMenu = new JPopupMenu();

        JMenuItem pencilItem = new JMenuItem("Pencil");
        JMenuItem brushItem = new JMenuItem("Brush");
        JMenuItem penItem = new JMenuItem("Pen");
        JMenuItem sprayBrushItem = new JMenuItem("Spray Brush");
        JMenuItem calligraphyBrushItem = new JMenuItem("Calligraphy Brush");

        brushMenu.add(pencilItem);
        brushMenu.add(brushItem);
        brushMenu.add(penItem);
        brushMenu.add(sprayBrushItem);
        brushMenu.add(calligraphyBrushItem);

        brushButton.addActionListener(e -> brushMenu.show(brushButton, brushButton.getWidth() / 2, brushButton.getHeight() / 2));

        pencilItem.addActionListener(e -> setTool(new PencilTool(canvas, currentColor)));
        brushItem.addActionListener(e -> setTool(new BrushTool(canvas, currentColor)));
        penItem.addActionListener(e -> setTool(new PenTool(canvas, currentColor)));
        sprayBrushItem.addActionListener(e -> setTool(new SprayBrushTool(canvas, currentColor)));
        calligraphyBrushItem.addActionListener(e -> setTool(new CalligraphyBrushTool(canvas, currentColor)));

        return brushButton;
    }

    private JButton createShapeButton() {
        JButton shapeButton = new JButton("Shape");
        JPopupMenu shapeMenu = new JPopupMenu();

        JMenuItem rectangleItem = new JMenuItem("Rectangle");
        JMenuItem circleItem = new JMenuItem("Circle");
        JMenuItem triangleItem = new JMenuItem("Triangle");
        JMenuItem squareItem = new JMenuItem("Square");

        shapeMenu.add(rectangleItem);
        shapeMenu.add(circleItem);
        shapeMenu.add(triangleItem);
        shapeMenu.add(squareItem);

        shapeButton.addActionListener(e -> shapeMenu.show(shapeButton, shapeButton.getWidth() / 2, shapeButton.getHeight() / 2));

        rectangleItem.addActionListener(e -> setTool(new ShapeTool(canvas, "Rectangle", currentColor)));
        circleItem.addActionListener(e -> setTool(new ShapeTool(canvas, "Circle", currentColor)));
        triangleItem.addActionListener(e -> setTool(new ShapeTool(canvas, "Triangle", currentColor)));
        squareItem.addActionListener(e -> setTool(new ShapeTool(canvas, "Square", currentColor)));

        return shapeButton;
    }

    private JButton createColorButton() {
        JButton colorButton = new JButton("Color");
        colorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(this, "Choose Color", currentColor);
            if (selectedColor != null) {
                currentColor = selectedColor; 
                if (currentTool instanceof ColorableTool) {
                    ((ColorableTool) currentTool).setColor(currentColor);
                }
            }
        });
        return colorButton;
    }

    private JButton createBlueBrushButton() {
        JButton blueBrushButton = new JButton("Blue Brush");
        blueBrushButton.setBackground(Color.BLUE);

        blueBrushButton.addActionListener(e -> {
            currentColor = Color.BLUE; // Update the current color to blue
            setTool(new BrushTool(canvas, currentColor));
        });

        blueBrushButton.addMouseListener(new MouseAdapter() {
            private Color[] colors = {Color.RED, Color.GREEN, Color.YELLOW, Color.MAGENTA};
            private int colorIndex = 0;

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    colorIndex = (colorIndex + 1) % colors.length;
                    Color newColor = colors[colorIndex];
                    blueBrushButton.setBackground(newColor);
                    currentColor = newColor; // Update the current color
                    setTool(new BrushTool(canvas, currentColor));
                }
            }
        });

        return blueBrushButton;
    }

    private JButton createEraserButton() {
        JButton eraserButton = new JButton("Eraser");
        eraserButton.addActionListener(e -> setTool(new EraserTool(canvas)));
        return eraserButton;
    }

    private JButton createClearButton() {
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> canvas.clear());
        return clearButton;
    }

    private JButton createLayersButton() {
        JButton layersButton = new JButton("Layers");
        layersButton.addActionListener(e -> {
            String[] layerNames = canvas.getLayerNames();
            String selectedLayer = (String) JOptionPane.showInputDialog(this, "Select a layer to remove:", "Layers", JOptionPane.PLAIN_MESSAGE, null, layerNames, layerNames[0]);
            if (selectedLayer != null) {
                canvas.removeLayer(selectedLayer);
            }
        });
        return layersButton;
    }

    private void setTool(Tool tool) {
        this.currentTool = tool;
        if (tool instanceof ColorableTool) {
            ((ColorableTool) tool).setColor(currentColor); 
        }
        canvas.setCurrentTool(tool);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PaintApp::new);
    }
}

class DrawingCanvas extends JPanel {
    private List<Layer> layers = new ArrayList<>();
    private Layer currentLayer;
    private Tool currentTool;

    public DrawingCanvas() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        createNewLayer();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (currentTool != null) currentTool.onPress(e);
            }

            public void mouseReleased(MouseEvent e) {
                if (currentTool instanceof ShapeTool && currentLayer.getCurrentShape() != null) {
                    currentLayer.addShape(currentLayer.getCurrentShape());
                    currentLayer.setCurrentShape(null);
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (currentTool != null) {
                    currentTool.onDrag(e);
                    repaint();
                }
            }
        });
    }

    public void setCurrentTool(Tool tool) {
        this.currentTool = tool;
    }

    public void addShape(Shape shape) {
        currentLayer.addShape(shape);
    }

    public void setCurrentShape(Shape shape) {
        currentLayer.setCurrentShape(shape);
    }

    public void clear() {
        layers.clear();
        createNewLayer();
        repaint();
    }

    private void createNewLayer() {
        currentLayer = new Layer("Layer " + (layers.size() + 1));
        layers.add(currentLayer);
    }

    public String[] getLayerNames() {
        return layers.stream().map(Layer::getName).toArray(String[]::new);
    }

    public void removeLayer(String layerName) {
        layers.removeIf(layer -> layer.getName().equals(layerName));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Layer layer : layers) {
            layer.draw(g);
        }
    }
}

class Layer {
    private String name;
    private List<Shape> shapes = new ArrayList<>();
    private Shape currentShape;

    public Layer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
    }

    public Shape getCurrentShape() {
        return currentShape;
    }

    public void setCurrentShape(Shape currentShape) {
        this.currentShape = currentShape;
    }

    public void draw(Graphics g) {
        for (Shape shape : shapes) {
            shape.draw(g);
        }
        if (currentShape != null) {
            currentShape.draw(g);
        }
    }

    public void removeShape(Shape shape) {
        shapes.remove(shape);
    }
}

interface Tool {
    void onPress(MouseEvent e);
    void onDrag(MouseEvent e);
}

interface ColorableTool {
    void setColor(Color color);
}

class PencilTool implements Tool, ColorableTool {
    private DrawingCanvas canvas;
    private int prevX, prevY;
    private Color color;

    public PencilTool(DrawingCanvas canvas, Color color) {
        this.canvas = canvas;
        this.color = color;
    }

    @Override
    public void onPress(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();
    }

    @Override
    public void onDrag(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        LineShape line = new LineShape(prevX, prevY, x, y, color, 1);
        canvas.addShape(line);
        prevX = x;
        prevY = y;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

class BrushTool implements Tool, ColorableTool {
    private DrawingCanvas canvas;
    private int prevX, prevY;
    private Color color;

    public BrushTool(DrawingCanvas canvas, Color color) {
        this.canvas = canvas;
        this.color = color;
    }

    @Override
    public void onPress(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();
    }

    @Override
    public void onDrag(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        LineShape line = new LineShape(prevX, prevY, x, y, color, 5);
        canvas.addShape(line);
        prevX = x;
        prevY = y;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

class PenTool implements Tool, ColorableTool {
    private DrawingCanvas canvas;
    private int prevX, prevY;
    private Color color;

    public PenTool(DrawingCanvas canvas, Color color) {
        this.canvas = canvas;
        this.color = color;
    }

    @Override
    public void onPress(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();
    }

    @Override
    public void onDrag(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        LineShape line = new LineShape(prevX, prevY, x, y, color, 3);
        canvas.addShape(line);
        prevX = x;
        prevY = y;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

class SprayBrushTool implements Tool, ColorableTool {
    private DrawingCanvas canvas;
    private Color color;
    private Random random = new Random();

    public SprayBrushTool(DrawingCanvas canvas, Color color) {
        this.canvas = canvas;
        this.color = color;
    }

    @Override
    public void onPress(MouseEvent e) {
        spray(e.getX(), e.getY());
    }

    @Override
    public void onDrag(MouseEvent e) {
        spray(e.getX(), e.getY());
    }

    private void spray(int x, int y) {
        for (int i = 0; i < 50; i++) {
            int offsetX = random.nextInt(20) - 10;
            int offsetY = random.nextInt(20) - 10;
            LineShape dot = new LineShape(x + offsetX, y + offsetY, x + offsetX, y + offsetY, color, 1);
            canvas.addShape(dot);
        }
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

class CalligraphyBrushTool implements Tool, ColorableTool {
    private DrawingCanvas canvas;
    private int prevX, prevY;
    private Color color;

    public CalligraphyBrushTool(DrawingCanvas canvas, Color color) {
        this.canvas = canvas;
        this.color = color;
    }

    @Override
    public void onPress(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();
    }

    @Override
    public void onDrag(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        LineShape line = new LineShape(prevX, prevY, x, y, color, 8);
        canvas.addShape(line);
        prevX = x;
        prevY = y;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
class EraserTool implements Tool {
    private DrawingCanvas canvas;
    private int eraserSize = 20;

    public EraserTool(DrawingCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void onPress(MouseEvent e) {
        erase(e.getX(), e.getY());
    }

    @Override
    public void onDrag(MouseEvent e) {
        erase(e.getX(), e.getY());
    }

    private void erase(int x, int y) {
        RectangleShape eraser = new RectangleShape(x, y, eraserSize, eraserSize, Color.WHITE, eraserSize);
        canvas.addShape(eraser);
        canvas.repaint();
    }
}

class ShapeTool implements Tool, ColorableTool {
    private DrawingCanvas canvas;
    private String shapeType;
    private int startX, startY;
    private Color color;

    public ShapeTool(DrawingCanvas canvas, String shapeType, Color color) {
        this.canvas = canvas;
        this.shapeType = shapeType;
        this.color = color;
    }

    @Override
    public void onPress(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
    }

    @Override
    public void onDrag(MouseEvent e) {
        int width = e.getX() - startX;
        int height = e.getY() - startY;
        Shape shape = null;

        switch (shapeType) {
            case "Rectangle":
                shape = new RectangleShape(startX, startY, width, height, color, 2);
                break;
            case "Circle":
                int diameter = Math.min(Math.abs(width), Math.abs(height));
                shape = new CircleShape(startX, startY, diameter, color, 2);
                break;
            case "Triangle":
                shape = new TriangleShape(startX, startY, e.getX(), e.getY(), color, 2);
                break;
            case "Square":
                int side = Math.min(Math.abs(width), Math.abs(height));
                shape = new SquareShape(startX, startY, side, color, 2);
                break;
        }

        if (shape != null) {
            canvas.setCurrentShape(shape);
        }
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }
}
abstract class Shape {
    protected int x1, y1, x2, y2;
    protected Color color;
    protected int thickness;

    public Shape(int x1, int y1, int x2, int y2, Color color, int thickness) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.thickness = thickness;
    }

    public abstract void draw(Graphics g);
}

class LineShape extends Shape {
    public LineShape(int x1, int y1, int x2, int y2, Color color, int thickness) {
        super(x1, y1, x2, y2, color, thickness);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawLine(x1, y1, x2, y2);
    }
}

class RectangleShape extends Shape {
    public RectangleShape(int x1, int y1, int width, int height, Color color, int thickness) {
        super(x1, y1, x1 + width, y1 + height, color, thickness);
    }

  
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawRect(x1, y1, x2 - x1, y2 - y1);
    }
}

class CircleShape extends Shape {
    public CircleShape(int x1, int y1, int diameter, Color color, int thickness) {
        super(x1, y1, x1 + diameter, y1 + diameter, color, thickness);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawOval(x1, y1, x2 - x1, y2 - y1);
    }
}

class SquareShape extends Shape {
    public SquareShape(int x1, int y1, int side, Color color, int thickness) {
        super(x1, y1, x1 + side, y1 + side, color, thickness);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawRect(x1, y1, x2 - x1, y2 - y1);
    }
}

class TriangleShape extends Shape {
    public TriangleShape(int x1, int y1, int x2, int y2, Color color, int thickness) {
        super(x1, y1, x2, y2, color, thickness);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int[] xPoints = {x1, (x1 + x2) / 2, x2};
        int[] yPoints = {y2, y1, y2};
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawPolygon(xPoints, yPoints, 3);
    }
}
