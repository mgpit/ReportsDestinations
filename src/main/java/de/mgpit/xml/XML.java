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
package de.mgpit.xml;


import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * A <strong>very simple</strong> fluent API for creating an XML DOM.
 * <p>
 * Does not support for Namespaces.
 * 
 * @author mgp
 *
 */
public class XML {

    private static final SimpleDateFormat XML_DATE = new SimpleDateFormat( "yyyy-MM-dd" );
    private static final SimpleDateFormat XML_DATE_TIME = new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ss" );

    /**
     * Holds the document.
     */
    private final Document document;
    /**
     * Holds the node to which new children will be appended.
     */
    private Stack hierarchy = new Stack();
    /**
     * Holds the node created last.
     */
    private Node last;

    private Charset charset;

    /**
     * Factory method - creates a new {@code XML} document.
     * 
     * @return a new {@code XML}
     * @throws Exception
     */
    public static final XML newDocument() throws Exception {
        return new XML();
    }

    public static final XML newDocument( String charsetName ) throws Exception {
        return new XML( charsetName );
    }

    /**
     * Factory method - creates a new {@code XMLFragment}.
     * <p>
     * A {@code XMLFragment} can be added to the to the {@code XML} from which it has been split off.
     * 
     * @param xml
     *            {@code XML} from which the {@code XMLFragment} will be split off
     * @return a new {@code XMLFragment}
     * @throws Exception
     */
    public static final XMLFragment newFragment( XML xml ) throws Exception {
        return xml.new XMLFragment();
    }

    private XML() throws Exception {
        if ( !this.isFragment() ) {
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = builderFactory.newDocumentBuilder();
            document = builder.newDocument();
            last = document;
            hierarchy.push( last );
        } else {
            document = null;
        }
    }

    private XML( String charsetName ) throws Exception {
        this();
        this.charset = Charset.forName( charsetName );
    }

    /**
     * Checks if this is a fragment.
     * 
     * @return {@code false}
     */
    protected boolean isFragment() {
        return false;
    }

    /**
     * Gets this' {@code Document}
     * 
     * @return
     */
    protected Document document() {
        return this.document;
    }

    /**
     * Gets this' current parent {@code Node}.
     * <p>
     * New {@code Node}s will be added to that {@code Node} as children.
     * 
     * @return
     */
    private Node current() {
        if ( hierarchy.isEmpty() ) {
            return null;
        } else {
            return (Node) hierarchy.peek();
        }
    }

    private boolean hasCurrent() {
        return current() != null;
    }

    /**
     * Adds a {@code Node} to this {@code XML}.
     * 
     * @param node
     * @return
     * @throws IllegalStateException
     */
    public XML add( Node node ) throws IllegalStateException {
        Node current = current();
        if ( hasCurrent() && current.hasChildNodes() ) {
            NodeList children = current.getChildNodes();
            if ( children.getLength() == 1 && children.item( 0 )
                                                      .getNodeType() != Node.ELEMENT_NODE ) {
                throw new IllegalStateException( "Cannot append Child Nodes to a Node which has Text!" );
            }
        }
        last = node;
        if ( hasCurrent() ) {
            current.appendChild( node );
        } else {
            nest();
        }
        return this;
    }

    /**
     * Adds a {@code Element} to the {@code Node} last created.
     * 
     * @param tagName
     *            {@code Element}'s tag name
     * @return this {@code XML}
     * @throws IllegalStateException
     *             if the {@code Node} last created already has text.
     */
    public XML add( String tagName ) throws IllegalStateException {
        Element newChild = document().createElement( tagName );
        return add( newChild );
    }

    /**
     * Adds a {@code XMLFragment} to the {@code Node} last created.
     * 
     * @param fragment
     *            the {@code XMLFragment} to add
     * @return this {@code XML}
     * @throws IllegalStateException
     */
    public XML add( XMLFragment fragment ) throws IllegalStateException {
        Node n = fragment.get();
        return add( n );
    }

    /**
     * Adds a {@code Text} node to the {@code Node} last created.
     *
     * @param data
     *            {@code Text}'s content
     * @return this {@code XML}
     * @throws IllegalStateException
     *             if the {@code Node} last created already has children.
     */
    public XML withData( String data ) throws IllegalStateException {
        if ( last.hasChildNodes() ) {
            throw new IllegalStateException( "Cannot append Text to a Node which has children!" );
        }
        Text newChild = document().createTextNode( (data == null ? "" : data) ); // TODO: Should I really???
        last.appendChild( newChild );
        return this;
    }

    public XML withData( boolean b ) throws IllegalStateException {
        return withData( Boolean.valueOf( b )
                                .toString() );
    }

    public XML withData( Date d ) throws IllegalStateException {
        return withData( XML_DATE_TIME.format( d ) );
    }

    public XML withDateData( Date d ) throws IllegalStateException {
        return withData( XML_DATE.format( d ) );
    }

    /**
     * Adds an {@code Attribute} to the {@code Node} last added.
     *
     * @param name
     *            {@code Attribute}'s name
     * @param value
     *            {@code Attribute}'s value
     * @return this {@code XML}
     */
    public XML attribute( String name, String value ) {
        if ( value != null ) {
            ((Element) last).setAttribute( name, value );
        }
        return this;
    }

    public XML attribute( String name, boolean b ) {
        return attribute( name, Boolean.valueOf( b )
                                       .toString() );
    }

    public XML attribute( String name, Date d ) {
        return attribute( name, XML_DATE_TIME.format( d ) );
    }

    public XML dateAttribute( String name, Date d ) {
        return attribute( name, XML_DATE.format( d ) );
    }

    /**
     * Makes the {@code Node} last added to the new parent node for adding.
     * 
     * @return this {@code XML}
     */
    public XML nest() {
        hierarchy.push( last );
        return this;
    }

    /**
     * 
     * @return this {@code XML}
     * @throws IllegalStateException
     *             if already at top level
     */
    public XML unnest() throws IllegalStateException {
        if ( hierarchy.isEmpty() ) {
            throw new IllegalStateException( "Cannot close() beyond Documen or Fragment root!" );
        }
        hierarchy.pop();
        return this;
    }

    /**
     * Gets the XML document or fragment root Node.
     * 
     * @return
     * @throws IllegalStateException
     */
    public Node get() throws IllegalStateException {
        if ( hierarchy.isEmpty() ) {
            throw new IllegalStateException( "XML is empty!" );
        }
        return (Node) hierarchy.elementAt( 0 );
    }

    public String toString() {
        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();

            if ( this.isFragment() ) {
                transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            }
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

            if ( charsetDefined() ) {
                transformer.setOutputProperty( OutputKeys.ENCODING, this.charset.name() );
            }

            DOMSource xmlSource = new DOMSource( get() );
            StringWriter outputTargetContent = new StringWriter();
            Result outputTarget = new StreamResult( outputTargetContent );

            transformer.transform( xmlSource, outputTarget );
            return outputTargetContent.toString();
        } catch ( Exception toBeMadeUnchecked ) {
            throw new RuntimeException( toBeMadeUnchecked );
        }

    }

    private boolean charsetDefined() {
        return this.charset != null;
    }

    /**
     * A XML Fragment.
     * 
     * @author mgp
     *
     */
    public class XMLFragment extends XML {
        private final XML xml = XML.this;

        private XMLFragment() throws Exception {
            super();
        }

        /**
         * Gets this fragment's {@code Document}
         */
        protected Document document() {
            return this.xml.document;
        }

        /**
         * Checks if this is a fragment.
         * 
         * @return {@code true}
         */
        protected boolean isFragment() {
            return true;
        }
    }

}
