package org.apache.maven.diagrams.connectors.classes.test_classes;

public class Triangle extends Shape
{
    private Integer a;
    private Integer b;
    private Integer c;
    
    public Triangle()
    {
        a=0;
        b=0;
        c=0;
    }
    
    public Triangle(Integer a)
    {
        this.a=a;
        this.b=a;
        this.c=a;
    }
    
    public Triangle(Integer a, Integer b, Integer c)
    {
        this.a=a;
        this.b=b;
        this.c=c;
    }

    @Override
    public double countAreaSize()
    {
        return heron(a,b,c);
    }
    
    protected static double heron(int a, int b, int c)
    {
        Integer p=(a+b+c)/2;
        return  Math.sqrt(p*(a-p)*(a-b)*(a-c));
    }

}
