package org.apache.maven.diagrams.gui.connector.classes;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.apache.maven.diagrams.connectors.classes.config.AggregateEdgeType;
import org.apache.maven.diagrams.connectors.classes.config.ClassesConnectorConfiguration;
import org.apache.maven.diagrams.connectors.classes.config.ExcludeClasses;
import org.apache.maven.diagrams.connectors.classes.config.ImplementEdgeType;
import org.apache.maven.diagrams.connectors.classes.config.IncludeClasses;
import org.apache.maven.diagrams.connectors.classes.config.InheritanceEdgeType;
import org.apache.maven.diagrams.gui.connector.AbstractConnectorConfigurationPanel;
import org.apache.maven.diagrams.gui.swing_helpers.ObjectToStringConverter;
import org.apache.maven.diagrams.gui.swing_helpers.OrderedStringListPanel;

public class ClassesConnectorConfigurationPanel
    extends AbstractConnectorConfigurationPanel<ClassesConnectorConfiguration>
{
    /* data */
    private ClassesConnectorConfiguration conf;

    /* controllers */
    private OrderedStringListPanel<IncludeClasses> includedPanel;

    private OrderedStringListPanel<ExcludeClasses> excludedPanel;

    private JCheckBox fullInheritancePathCheckBox;

    /* edge type controllers */
    private JPanel edgeTypesPanel;

    private JCheckBox aggregateEdgesCheckBox;

    private JCheckBox inheritanceEdgesCheckBox;

    private JCheckBox implementEdgesCheckBox;

    /* nodes details controllers */
    private JPanel nodesPanel;

    private JCheckBox compressJavaBeanProperties;

    private JCheckBox propagateInheritedMethods;

    private JCheckBox propagateInheritedFields;

    /**
     * 
     */
    private static final long serialVersionUID = -6691930260152705247L;

    public ClassesConnectorConfigurationPanel()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        this.setPreferredSize( new Dimension( 200, 500 ) );
        gridBagLayout.rowWeights = new double[] { 1.0, 1.0, 0.0, 0.0, 0.0, 0.0 };
        this.setLayout( gridBagLayout );
        GridBagConstraints g = new GridBagConstraints();

        g.anchor = GridBagConstraints.NORTHWEST;
        g.gridx = 0;
        g.gridy = 0;
        g.fill = GridBagConstraints.BOTH;
        includedPanel =
            new OrderedStringListPanel<IncludeClasses>( null, new IncludeClassesEditingPanel(),
                                                        new ObjectToStringConverter<IncludeClasses>()
                                                        {
                                                            public String convert( IncludeClasses object )
                                                            {
                                                                return object.getPattern();
                                                            }
                                                        }, "Includes: " );
        this.add( includedPanel, g );

        g.gridy++;
        excludedPanel =
            new OrderedStringListPanel<ExcludeClasses>( null, new ExcludeClassesEditingPanel(),
                                                        new ObjectToStringConverter<ExcludeClasses>()
                                                        {
                                                            public String convert( ExcludeClasses object )
                                                            {
                                                                return object.getPattern()
                                                                                + ( object.getKeepEdges() ? " [keep edges]"
                                                                                                : "" );
                                                            }

                                                        }, "Excludes: " );
        this.add( excludedPanel, g );

        g.gridy++;
        fullInheritancePathCheckBox = new JCheckBox( "Full inheritance path" );
        this.add( fullInheritancePathCheckBox, g );

        // ----------------------- EDGE TYPES panel --------------------------------
        aggregateEdgesCheckBox = new JCheckBox( "Add class aggregation edges" );
        inheritanceEdgesCheckBox = new JCheckBox( "Add class inheritence edges" );
        implementEdgesCheckBox = new JCheckBox( "Add interface implement edges" );

        edgeTypesPanel = new JPanel();
        edgeTypesPanel.setBorder( new TitledBorder( "Edges" ) );
        edgeTypesPanel.setLayout( new GridLayout( 3, 1 ) );
        edgeTypesPanel.add( aggregateEdgesCheckBox );
        edgeTypesPanel.add( inheritanceEdgesCheckBox );
        edgeTypesPanel.add( implementEdgesCheckBox );

        g.gridy++;
        this.add( edgeTypesPanel, g );
        // ----------------------- Nodes types panel -------------------------------

        compressJavaBeanProperties = new JCheckBox( "Compress Java Bean properties" );
        propagateInheritedMethods = new JCheckBox( "Propagate inherited methods" );
        propagateInheritedFields = new JCheckBox( "Propagate inherited fields" );

        nodesPanel = new JPanel();
        nodesPanel.setBorder( new TitledBorder( "Nodes" ) );
        nodesPanel.setLayout( new GridLayout( 3, 1 ) );
        nodesPanel.add( compressJavaBeanProperties );
        nodesPanel.add( propagateInheritedFields );
        nodesPanel.add( propagateInheritedMethods );
        g.gridy++;
        this.add( nodesPanel, g );
    }

    public void setCurrentConfiguration( ClassesConnectorConfiguration classesConnectorConfigurationPanel )
    {
        conf = classesConnectorConfigurationPanel;
        includedPanel.setItems( conf.getIncludes() );
        excludedPanel.setItems( conf.getExcludes() );

        fullInheritancePathCheckBox.setSelected( conf.getFullInheritancePaths() );

        aggregateEdgesCheckBox.setSelected( isOneInstanceOf( AggregateEdgeType.class, conf.getEdges() ) );
        inheritanceEdgesCheckBox.setSelected( isOneInstanceOf( InheritanceEdgeType.class, conf.getEdges() ) );
        implementEdgesCheckBox.setSelected( isOneInstanceOf( ImplementEdgeType.class, conf.getEdges() ) );

        compressJavaBeanProperties.setSelected( conf.getNodes().getCompressJavaBeanProperties() );
        propagateInheritedMethods.setSelected( conf.getNodes().getPropagateInheritedMethods() );
        propagateInheritedFields.setSelected( conf.getNodes().getPropagateInheritedFields() );
    }

    public ClassesConnectorConfiguration getCurrentConfiguration()
    {
        updateConfState();
        return conf;
    }

    private void updateConfState()
    {
        conf.setFullInheritancePaths( fullInheritancePathCheckBox.isSelected() );

        conf.getEdges().clear();
        if ( aggregateEdgesCheckBox.isSelected() )
            conf.getEdges().add( new AggregateEdgeType() );
        if ( inheritanceEdgesCheckBox.isSelected() )
            conf.getEdges().add( new InheritanceEdgeType() );
        if ( implementEdgesCheckBox.isSelected() )
            conf.getEdges().add( new ImplementEdgeType() );

        conf.getNodes().setCompressJavaBeanProperties( compressJavaBeanProperties.isSelected() );
        conf.getNodes().setPropagateInheritedMethods( propagateInheritedMethods.isSelected() );
        conf.getNodes().setPropagateInheritedFields( propagateInheritedFields.isSelected() );
    }

    private boolean isOneInstanceOf( Class<? extends Object> clas, Collection<? extends Object> edges )
    {
        for ( Object o : edges )
        {
            if ( clas.isInstance( o ) )
                return true;
        }
        return false;
    }

    /* TODO: For testing purposes - remove later */

    public static void main( String[] args )
    {
        ClassesConnectorConfigurationPanel c = new ClassesConnectorConfigurationPanel();
        c.setCurrentConfiguration( new ClassesConnectorConfiguration() );
        c.run();
    }

    private void createAndShowGUI()
    {

        // Create and set up the window.
        JFrame frame = new JFrame( "Maven diagram GUI" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.getContentPane().add( this );
        frame.setPreferredSize( new Dimension( 300, 300 ) );

        // Display the window.
        frame.pack();
        frame.setVisible( true );
    }

    public void run()
    {
        javax.swing.SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                createAndShowGUI();
            }
        } );
    }
}
