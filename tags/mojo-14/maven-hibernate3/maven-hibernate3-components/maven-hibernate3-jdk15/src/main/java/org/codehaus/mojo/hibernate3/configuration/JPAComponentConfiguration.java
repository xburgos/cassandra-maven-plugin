package org.codehaus.mojo.hibernate3.configuration;

import org.hibernate.cfg.Configuration;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.HibernateException;

import java.util.HashMap;

public class JPAComponentConfiguration
    extends AbstractComponentConfiguration
{
// --------------------- Interface ComponentConfiguration ---------------------

    public String getName()
    {
        return "jpaconfiguration";
    }

    protected Configuration createConfiguration()
    {
        String persistenceUnit = getExporterMojo().getComponentProperty( "persistenceunit" );

        try
        {
            Ejb3Configuration ejb3cfg = new Ejb3Configuration();
            if ( ejb3cfg.configure( persistenceUnit, new HashMap() ) == null )
            {
                getExporterMojo().getLog().error( "Persistence unit not found: '" + persistenceUnit + "'." );
                return null;
            }
            return ejb3cfg.getHibernateConfiguration();
        }
        catch ( HibernateException he )
        {
            getExporterMojo().getLog().error( he.getMessage() );
            return null;
        }
    }
}
