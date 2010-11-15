/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation.
 */
package de.gbv.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

/**
 * Helper class to create XML as SAX stream.
 * This is how SAX should be designed for data-oriented XML. It is based on
 * the following assumptions:
 * 1. No mixed content
 * 2. No processing instructions and unparsed entities
 * 3. Namespace prefixes are defined but not changed
 * 4. All attributes are CDATA and don't have namespaces
 */
public class SimpleXMLCreator {
    private final ContentHandler target;
    private final Map<String,String> namespaces;
    private boolean xmlns;

    class Element {
        public final String uri, localName, qName;
        Element ( String prefix, String name ) {
            uri = namespaces.get(prefix);
            localName = name; 
            if ( prefix.equals("") ) {
                qName = name;
            } else {
                qName = prefix + ":" + name;
            }
        }
    }

    private Stack<Element> elements = new Stack<Element>();

    public SimpleXMLCreator(ContentHandler target, Map<String,String> namespaces, boolean xmlns ) 
      throws SAXException {
        this.target = target;
        this.namespaces = namespaces;
        this.xmlns = xmlns;
        if ( xmlns ) {
            for ( String prefix : namespaces.keySet() ) {
                target.startPrefixMapping( prefix, namespaces.get(prefix) );
            }
        }
    }

    public SimpleXMLCreator(ContentHandler target, Map<String,String> namespaces) {
        this.target = target;
        this.namespaces = namespaces;
        this.xmlns = false;
    }

    public SimpleXMLCreator startElement( String prefix, String name, Map<String,String> attributes ) 
      throws SAXException {
        Element e = new Element( prefix, name );
        AttributesImpl a = new AttributesImpl();

        if ( xmlns ) { // TODO: move this to XMLWriter?
            for ( String nsPrefix : namespaces.keySet() ) {
                String s = nsPrefix.equals("") ? "xmlns" : "xmlns:" + nsPrefix;
                a.addAttribute( "", "", s, "CDATA", namespaces.get(nsPrefix) );
            }
            xmlns = false;
        }

        if ( attributes != null ) {
            for( String key : attributes.keySet() ) {
                a.addAttribute( "", key, key, "CDATA", attributes.get(key) ); 
            }
        }

        target.startElement( e.uri, e.localName, e.qName, a );
        elements.push(e);

        return this;
    }

    public SimpleXMLCreator startElement( String prefix, String name )
      throws SAXException {
        return startElement( prefix, name, null );
    }

    public SimpleXMLCreator startElement( String name, Map<String,String> attributes )
      throws SAXException {
        return startElement( "", name, attributes );
    }

    public SimpleXMLCreator startElement( String name ) throws SAXException {
        return startElement( "", name, null );
    }

    public SimpleXMLCreator contentElement( String prefix, String name, 
      String content, Map<String,String> attributes ) throws SAXException {
        startElement( prefix, name, attributes );
        target.characters( content.toCharArray(), 0, content.length() );
        return endElement();
    }

    public SimpleXMLCreator contentElement( String prefix, String name, String content ) 
      throws SAXException {
        return contentElement( prefix, name, content, null );
    }

    public SimpleXMLCreator contentElement( String name, String content, 
      Map<String,String> attributes ) throws SAXException {
        return contentElement( "", name, content, attributes );
    }

    public SimpleXMLCreator contentElement( String name, String content ) 
      throws SAXException {
        return contentElement( "", name, content, null );
    }

    public SimpleXMLCreator emptyElement( String prefix, String name, 
      Map<String,String> attributes ) throws SAXException {
        return contentElement( prefix, name, "", attributes );
    }

    public SimpleXMLCreator emptyElement( String name, Map<String,String> attributes )
      throws SAXException {
        return contentElement( "", name, "", attributes );
    }

    public SimpleXMLCreator emptyElement( String prefix, String name ) 
      throws SAXException {
        return contentElement( prefix, name, "", null );
    }

    public SimpleXMLCreator emptyElement( String name ) throws SAXException {
        return contentElement( "", name, "", null );
    }

    public SimpleXMLCreator endElement() throws SAXException {
        if ( elements.isEmpty() ) return this;
        Element e = elements.pop();
        target.endElement( e.uri, e.localName, e.qName );

        if ( elements.isEmpty() && xmlns ) {
            for ( String prefix : namespaces.keySet() ) {
                target.endPrefixMapping( prefix );
            }
        }

        return this;
    }

    public void endAllElements() throws SAXException {
        while ( ! elements.isEmpty() ) {
            endElement();
        }
    }

    public void endAll() throws SAXException {
        endAllElements();
        target.endDocument();
    }
}