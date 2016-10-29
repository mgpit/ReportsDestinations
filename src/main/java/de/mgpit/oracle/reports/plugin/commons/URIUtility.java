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


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import de.mgpit.oracle.reports.plugin.commons.io.IOUtility;
import de.mgpit.types.Filename;

/**
 * 
 * Provider of methods for working with {@link URI}s.
 * 
 * @author mgp
 */
public class URIUtility {

    /**
     * Converts the query part of an URI into {@link Properties}.
     * 
     * @param query
     *            the query part of the URI
     * @return {@link Properties} containing the query parameters
     */
    public static Properties queryStringAsProperties( final String query ) {
        final Properties queryParameters = new Properties();
        if ( query != null ) {
            final String[] parameters = query.split( "&" );
            for ( int index = 0; index < parameters.length; index++ ) {
                final String parameter = parameters[index];
                final String[] keyValuePair = parameter.split( "=" );
                final String key = keyValuePair[0];
                final String value = (keyValuePair.length == 1) ? null : keyValuePair[1];
                queryParameters.put( key, value );
            }
        }
        return queryParameters;
    }

    /**
     * Converts the query part of an URI into {@link Properties}.
     * 
     * @param query
     *            the query part of the URI
     * @return {@link Properties} containing the query parameters
     */
    public static Properties queryStringAsProperties( final URI uri ) {
        return queryStringAsProperties( uri.getQuery() );
    }

    /**
     * Splits the relevant path into its <code>wmq-queue</code> and <code>"@" wmq-qmgr</code> parts
     * 
     * @param path
     *            relevant path part of the URI
     * @return a 2 element array with <code>wmq-queue</code> and <code>"@" wmq-qmgr</code> parts. If there is no<code>"@" wmq-qmgr</code> part the second element will be null.
     */
    public static String[] splitPathIntoElements( final String path ) {
        String[] splitted = path.split( "@" );
        /* Ensure that the caller gets (at least) 2 elements */
        if ( splitted.length == 1 ) {
            // No Arrays.copyOf in Java 1.4
            final String[] temporary = new String[2];
            temporary[0] = splitted[0];
            temporary[1] = null;
            splitted = temporary;
        }
        return splitted;
    }

    /**
     * Interpret the file as URI and get its path
     * 
     * @param file
     * @return path component of the file
     */
    public static String toUriPathString( File file ) {
        return file.toURI().getPath();
    }

    /**
     * Interpret the filename as URI and get its path
     * 
     * @param filename
     * @return path component of the file
     */
    public static String toUriPathString( final Filename filename ) throws IOException {
        return toUriPathString( IOUtility.fileFromName( filename ) );
    }

    /**
     * Get the URI's path as platform specific path.
     * <p>
     * Makes sense for file URIs, only ...
     * 
     * @param uri
     * @return URIs path as platform specific path
     */
    public static String pathToPlatformPath( final URI uri ) {
        String path = uri.getPath();
        return new File( path ).getPath();
    }

    /**
     * Get the URI's path as platform specific Filename.
     * <p>
     * Makes sense for file URIs, only ...
     * 
     * @param uri
     * @return URIs path as platform specific path
     */
    public static Filename pathToPlatformFilename( final URI uri ) {
        return Filename.of( pathToPlatformPath( uri ) );
    }

    /**
     * Get the URI's path as platform specific absolute path.
     * <p>
     * Makes sense for file URIs, only ...
     * 
     * @param uri
     * @return URIs path as platform specific path
     */
    public static String pathToAbsolutePlatformPath( final URI uri ) {
        String path = uri.getPath();
        return new File( path ).getAbsolutePath();
    }

}
