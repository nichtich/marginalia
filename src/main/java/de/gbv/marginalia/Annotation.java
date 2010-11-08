/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation.
 */
package de.gbv.marginalia;

import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfBoolean;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfRectangle;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Map;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents an Annotation.
 *
 * This class is needed because the Annotation classes of iText are too
 * protected. We want to freely manipulate Annotations and are not interested
 * in most of the Annotation types anyway.
 */
public class Annotation {

    // mandatory fields
    protected PdfName subtype;
    protected PdfRectangle rect;
    protected PdfNumber page;

    // optional fields
    protected PdfString content;
    protected PdfString name;
    protected PdfString date;
    protected PdfString creationdate;
    protected PdfString title;
    protected PdfNumber flags;
    protected PdfDictionary borderstyle;
    protected PdfArray border;
    protected PdfArray rgb;
    protected PdfNumber opacity;

    protected PdfArray coords;
    protected PdfString subject;

    /**
     * Constructs a new Annotation from a given PdfDictionary.
     * Of course the PdfDictionary should contain an annotation.
     */
    public Annotation(PdfDictionary annot) {
        this.subtype  = annot.getAsName(PdfName.SUBTYPE);
// text | caret | freetext | fileattachment | highlight | ink | line | link | circle | square |
// polygon | polyline | sound | squiggly | stamp | strikeout | underline 
        this.rect     = getAsRectangle(annot,PdfName.RECT);
        this.page     = annot.getAsNumber(PdfName.P);

// should not be null
        // TODO: throw exception if subtype or rect are null

        this.content  = annot.getAsString(PdfName.CONTENTS); // optional

        // more optional fields
        this.name = annot.getAsString(PdfName.NM);
        this.date = annot.getAsString(PdfName.M);
        this.flags = annot.getAsNumber(PdfName.F);

        this.borderstyle = getAsDictionary(annot,PdfName.BS);
        this.border = annot.getAsArray(PdfName.BORDER);

        this.title = annot.getAsString(PdfName.T);

        // skipped fields:
        // getAsDictionary(annot,PdfName.AP);
        // annot.getAsName(PdfName.AS);
        // getAsDictionary(annot,PdfName.A); // action
        // getAsDictionary(annot,PdfName.A); // additional action

        this.rgb = annot.getAsArray(PdfName.C);

        this.opacity = annot.getAsNumber(PdfName.CA);
        this.creationdate = annot.getAsString(PdfName.CREATIONDATE);
        this.subject = annot.getAsString(PdfName.SUBJECT);
        this.page = annot.getAsNumber(PdfName.PAGE);

        // this.popup = getAsDictionary(annot,PdfName.POPUP);
        // only for popup:
        // this.titlelabel = annot.getAsString(PdfName.T);

        // annot.getAsNumber(PdfName.STRUCTPARENT);

        // for subtype one of Highlight, Underline, Squiggly, or StrikeOut:
        this.coords = annot.getAsArray(PdfName.QUADPOINTS);

        // for subtype Ink
        // ...

        // Since PDF 1.5: 
        // RC = contents-richtext
    }

    /**
     * PDF Annotation flags.
     */
    public enum Flag {
        INVISIBLE(1,"invisible"),
        HIDDEN(2,"hidden"),
        PRINT(3,"print"),
        NOZOOM(4,"nozoom"),
        NOROTATE(5,"norotate"),
        NOVIEW(6,"noview"),
        READONLY(7,"readonly"),
        LOCKED(8,"locked");
        // TOGGLENOVIEW(9,"toggleview") TODO ?
        public final int bit;
        public final String name;
        Flag(int bit, String name) { 
            this.bit = 2^bit;
            this.name = name;
        }
    }


    protected Map<String,String> attrs = new HashMap<String,String>();

    protected void setAttr(String name, Object object) {
        if (object == null) return;
        String value = object.toString(); // TODO: encoding & exception
        this.attrs.put(name,value);
    }

    /**
     * Write the annotation in XFDF format.
     * @param output Where to write the XML to.
     */
    public void writeXML( PrintWriter out ) {

        // # FDF annotation attributes

        this.setAttr("page",this.page); 

        // # Common annotation attributes

        if ( this.rgb != null ) {
            // contains an array of three numbers between 0.0 and 1.0 in the
            // deviceRGB color space. In XFDF, each color is mapped to a value
            // between 0 and 255 then converted to hexadecimal (00 to FF)
            // this.setAttr("color",s);  // TODO: => #xxxxxx
        }

        this.setAttr("date",this.date); 

        if ( this.flags != null ) {
            int flags = this.flags.intValue();
            StringBuilder s = new StringBuilder();
            if ( flags != 0 ) {
                for ( Flag f : Flag.values() ) {
                    if ( ( flags & f.bit ) != 0 ) {
                        if ( s.length() > 0 ) s.append(",");
                        s.append( f.name );
                    }
                }
            }
            this.setAttr("flags",s);
        }

        this.setAttr("name",this.name);

        this.setAttr("rect",this.rect); 

        this.setAttr("title",this.title);

        // # Markup annotation attribute

        this.setAttr("creationdate",this.creationdate);

        this.setAttr("opacity",this.opacity);

        this.setAttr("subject",this.subject);

        // TODO: indent, coords, inreplyto, replytype, icon, state, statemodel, 
        //       start, end, head, tail, interior-color, leaderLength
        //       leaderExtend, caption, intent, leader-offset, caption-style
        //       caption-offset-h, caption-offset-v, interior-color, fringe

        // # Caret annotation attributes

        // TODO: symbol, fringe, 

        // # Polygon and Polyline annotation attributes

        // TODO: interior-color, head, tail, intent, 

        // # Freetext annotation attributes

        // TODO: justification, rotation, intent

        // # Stamp annotation attributes

        // TODO: icon, rotation

        // # Fileattachment annotation attributes

        // TODO: icon

        // # Sound annotation attributes : ignore this

        // # Popup annotation attributes

        // TODO: open

        // # link annotation attributes

        // TODO: Highlight, coords

        // # Redaction annotation attributes: ...


        String element = "";

        String attrstring = "";
        for ( Map.Entry<String,String> attr : this.attrs.entrySet() ) {
            attrstring += "\n  " + attr.getKey() + "=\"" + attr.getValue() + "\"";
        }

        String innerstring = "";

        // TODO: character encoding not UTF-8? escape?

        if (this.subtype.equals(PdfName.TEXT)) {
            element = "text";
          // TODO: most subtypes have the same content model, so unify
            // TODO: contents-richtext
            // TODO: this.content may be null. Either use content or rich-content
            innerstring = "<content>" + this.content  + "</content>\n";
            // TODO: popup
        } else if (this.subtype.equals(PdfName.HIGHLIGHT)) {
            element = "highlight";
            // TODO: contents-richtext
            // TODO: this.content may be null. Either use content or rich-content
            innerstring = "<content>" + this.content  + "</content>\n";
            // TODO: popup

        } else { // TODO
            element = this.subtype.toString();
        }

        out.println("<"+element+attrstring+">"+innerstring+"</"+element+">");
    }

    /**
     * Convert a PdfArray, that is in a PdfDictionary, to a PdfRectangle.
     * This method should better be included in iText.
     * @param dict Which PdfDictionary to get the PdfArray from.
     * @param name Name of the included PdfRectangle.
     */
    public static PdfRectangle getAsRectangle( PdfDictionary dict, PdfName name ) {
        PdfArray array = dict.getAsArray(name);
        if (array == null) return null;
        if (array.size() != 4) return null;
        float[] c = new float[4];
        for(int i=0; i<4; i++) {
            PdfNumber p = array.getAsNumber(i);
            if (p == null) return null;
            c[i] = p.floatValue();
        }
        return new PdfRectangle(c[0], c[1], c[2], c[3]);
    }

    /**
     * Get a named member of a PdfDictionary as PdfDictionary.
     * This method should better be included in iText.
     * @param dict Which PdfDictionary to get from.
     * @param name Name of the included PdfDictionary.
     */
    public static PdfDictionary getAsDictionary( PdfDictionary dict, PdfName name ) {
        PdfObject obj = dict.getDirectObject(name);
        if (obj == null || !obj.isDictionary()) return null;
        return (PdfDictionary)obj;
    }

    /**
     * Write a set of annotations as XFDF document.
     * @param output Where to write the XML to.
     * @param annots Which annotations to write.
     */
    public static void writeXFDF( PrintWriter out, Iterable<Annotation> annots ) {
        out.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        out.println( "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\" xml:space=\"preserve\">" );

        // The following parts may be added as varargs
        // - optionally write <f href="Document.pdf"/>
        // - optionally write <ids original="ID" modified="ID" />

        out.println( "<annots>" );
        for( Annotation annot : annots ) {
            annot.writeXML( out );
        }
        out.println( "</annots>" );
        out.println( "</xfdf>" );
    }
}