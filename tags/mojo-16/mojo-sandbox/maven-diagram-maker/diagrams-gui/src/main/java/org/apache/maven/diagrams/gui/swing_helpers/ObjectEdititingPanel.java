package org.apache.maven.diagrams.gui.swing_helpers;

import javax.swing.JPanel;


public abstract class ObjectEdititingPanel<Type> extends JPanel
{
    public abstract void setObject(Type state);
    public abstract Type getObject();
}
