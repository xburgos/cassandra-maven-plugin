package org.apache.maven.diagrams.gui.swing_helpers;

public interface ObjectToStringConverter<Type>
{
    public String convert(Type object);
}
