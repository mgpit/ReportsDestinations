
/*
 * Copyright 2016 Marco Pauls www.mgp-it.de
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @license APACHE-2.0
 */
package de.mgpit.oracle.reports.plugin.commons;


/**
 * 
 * Poor man's units. Just to avoid "Magic Numbers".
 * 
 * @see Magic
 * 
 * @author mgp
 *
 */
public class Units {
    public static final int ONE_KILOBYTE = 1024;
    public static final int FOUR_KILOBYTE = 4 * ONE_KILOBYTE;
    public static final int EIGHT_KILOBYTE = 8 * ONE_KILOBYTE;
    public static final int SIXTYFOUR_KILOBYTE = 64 * ONE_KILOBYTE;
    public static final int FIFEHUNDREDANDTWELVE = 512 * ONE_KILOBYTE;
    public static final int ONE_MEGABYTE = ONE_KILOBYTE * ONE_KILOBYTE;
    public static final long ONE_GIGABYTE = ONE_MEGABYTE * ONE_KILOBYTE;
    public static final long ONE_TERABYTE = ONE_GIGABYTE * ONE_KILOBYTE;
    public static final long ONE_PETABYTE = ONE_TERABYTE * ONE_KILOBYTE;
}