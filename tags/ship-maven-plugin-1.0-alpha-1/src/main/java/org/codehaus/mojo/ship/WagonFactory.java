package org.codehaus.mojo.ship;

import org.apache.maven.wagon.Wagon;

/**
 * Created by IntelliJ IDEA.
 * User: stephenc
 * Date: 29/04/2011
 * Time: 18:19
 * To change this template use File | Settings | File Templates.
 */
public interface WagonFactory
{
    Wagon createWagon( String id, String url );
}
