/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation.
 */
package de.gbv.xml;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.PrintWriter;

/**
 * Simple XML Printer for data-centric XML.
 */
public class SimpleXMLWriter extends DefaultHandler {
    private final PrintWriter writer;
    private boolean hasCharacters;
    private boolean hasChilds;
    private int indent = 0;
    private String indentString = "  ";

    public SimpleXMLWriter( PrintWriter writer ) {
        this.writer = writer;
    }

    public void startDocument () throws SAXException {
        writer.print("<?xml version='1.0' encoding='UTF-8'?>");
        if (writer.checkError()) throw new SAXException("I/O error");
    }

    public void startElement( String uri, String localName, String qName, Attributes attrs ) 
      throws SAXException {
        hasCharacters = false;
        printIndent();
        writer.print('<');
        writer.print( "".equals(qName) ? localName : qName );
        for ( int i = 0; i < attrs.getLength (); i++ ) {
            String aName = attrs.getLocalName(i);
            if ("".equals(aName))
                aName = attrs.getQName(i);
            writer.print(" " + aName + "=\"");
            String value = attrs.getValue(i);
            int length = value.length();
            for (int j = 0; j < length; j++) {
                printEscaped( value.charAt(j), true );
            }
            writer.print("\"");
        }
        writer.print('>');
        indent++;
        if (writer.checkError()) throw new SAXException("I/O error");
    }

    public void endElement( String uri, String localName, String qName ) {
        indent--;
        if ( hasCharacters ) {
            hasCharacters = false;
        } else {
            printIndent();
        }
        writer.print("</");
        writer.print( "".equals(qName) ? localName : qName );
        writer.print(">");
    }

    /**
     * Print character data inside an element.
     */
    public void characters (char[] ch, int start, int length) 
      throws SAXException {
        hasCharacters = true;
        for (int i = 0; i < length; i++) {
            printEscaped( ch[start+i], false );
        }
        if (writer.checkError()) throw new SAXException("I/O error");
    }

    /**
     * Flushes the writer.
     */
    public void endDocument () throws SAXException {
        writer.println("");
        writer.flush();
        if (writer.checkError()) throw new SAXException("I/O error");
    }

    // ---- internal methods ----

    protected void printIndent() {
        writer.println("");
        for ( int i = 0; i < indent; i++ ) {
            writer.print(indentString);
        }
    }

    protected void printEscaped( char c, boolean isAttr ) {
        switch (c) {
            case '<':
                writer.print("&lt;");
                break;
            case '>':
                writer.print("&gt;");
                break;
            case '&':
                writer.print("&amp;");
                break;
            case '"':
                writer.print( isAttr ? "&quot;" : "\"" );
               break;
            case '\r':
                writer.print("&#xD;");
                break;
            default:
                writer.print(c);
        }
    }

}