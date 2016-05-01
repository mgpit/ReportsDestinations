package de.mgpit.oracle.reports.plugin.commons;

/**
 * 
 * @author mgp
 * Poor man's units. Just to avoid "Magic Numbers".
 *
 */
public class Units {
    public static final int ONE_KILOBYTE = 1024;
    public static final int FOUR_KILOBYTE = 4*ONE_KILOBYTE;
    public static final int EIGHT_KILOBYTE = 8*ONE_KILOBYTE;
    public static final int SIXTYFOUR_KILOBYTE = 64*ONE_KILOBYTE;
    public static final int FIFEHUNDREDANDTWELVE = 512*ONE_KILOBYTE;
    public static final int ONE_MEGABYTE = ONE_KILOBYTE * ONE_KILOBYTE;
    public static final int ONE_GIGABYTE = ONE_MEGABYTE * ONE_KILOBYTE;
    public static final long ONE_TERABYTE = ONE_GIGABYTE * ONE_KILOBYTE;
    public static final long ONE_PETABYTE = ONE_TERABYTE * ONE_KILOBYTE;
}
