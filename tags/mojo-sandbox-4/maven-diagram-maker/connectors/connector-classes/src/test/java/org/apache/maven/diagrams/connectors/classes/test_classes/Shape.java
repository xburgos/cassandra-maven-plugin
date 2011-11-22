package org.apache.maven.diagrams.connectors.classes.test_classes;

public abstract class Shape
{
    private Color color;
    
    abstract public double countAreaSize();
    
    public Color getColor()
    {
        return color;
    }
    
    public void setColor( Color color )
    {
        this.color = color;
    }
    
}
