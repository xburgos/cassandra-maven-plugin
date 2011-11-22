package org.codehaus.mojo.fitnesse;

public class ClassPathSubstitution
{

    /**
     * String to search.
     * 
     * @parameter
     * @required
     */
    private String search ;

    /**
     * String to replace.
     * 
     * @parameter
     * @required
     */
    private String replaceWith ;

    
    public ClassPathSubstitution()
    {
    }

    public ClassPathSubstitution( String search, String replaceWith )
    {
        super();
        this.search = search;
        this.replaceWith = replaceWith;
    }

    public String getReplaceWith()
    {
        return replaceWith;
    }

    public String getSearch()
    {
        return search;
    }


}
