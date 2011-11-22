package org.apache.maven.diagrams.connectors.classes.test_classes;

public class Color
{
    private int r;
    private int g;
    private int b;
    
    public Color(int a_r, int a_g, int a_b)
    {
        r=a_r;
        g=a_g;
        b=a_b;
    }
    
    public int getRed()
    {
        return r;
    }
    
    public int getGreen()
    {
        return g;
    }
    
    public int getBlue()
    {
        return b;
    }    
}
