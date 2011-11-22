package org.codehaus.mojo.rat;


/**
 * An instance of this class is used to report
 * statistics.
 */
public class RatStatistics
{
    private int numUnapprovedLicenses;
    private int numApprovedLicenses;

    /**
     * Returns the number of files with unapproved licenses.
     */
    public int getNumUnapprovedLicenses()
    {
        return numUnapprovedLicenses;
    }

    /**
     * Sets the number of files with unapproved licenses.
     */
    public void setNumUnapprovedLicenses( int numUnapprovedLicenses )
    {
        this.numUnapprovedLicenses = numUnapprovedLicenses;
    }

    /**
     * Returns the number of files with unapproved licenses.
     */
    public int getNumApprovedLicenses()
    {
        return numApprovedLicenses;
    }

    /**
     * Sets the number of files with approved licenses.
     */
    public void setNumApprovedLicenses( int numApprovedLicenses )
    {
        this.numApprovedLicenses = numApprovedLicenses;
    }
}
