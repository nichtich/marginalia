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
import java.util.Collections;

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
    private PdfDictionary dict;

    // mandatory fields
    protected PdfName subtype;
    protected PdfRectangle rect;
    protected PdfNumber page;

    // optional fields
    protected PdfString content;
    protected PdfNumber flags;
    protected PdfDictionary borderstyle;
    protected PdfArray border;
    protected PdfArray rgb;

    protected PdfArray coords;

    /**
     * Constructs a new Annotation from a given PdfDictionary.
     * Of course the PdfDictionary should contain an annotation.
     */
    public Annotation(PdfDictionary annot) {
        this.subtype  = annot.getAsName(PdfName.SUBTYPE);
// text | caret | freetext | fileattachment | highlight | ink | line | link | circle | square |
// polygon | polyline | sound | squiggly | stamp | strikeout | underline 

        this.content  = annot.getAsString(PdfName.CONTENTS); // optional

        // skipped fields:
        // getAsDictionary(annot,PdfName.AP);
        // annot.getAsName(PdfName.AS);
        // getAsDictionary(annot,PdfName.A); // action
        // getAsDictionary(annot,PdfName.A); // additional action

        // this.popup = getAsDictionary(annot,PdfName.POPUP);
        // only for popup:

        // annot.getAsNumber(PdfName.STRUCTPARENT);

        this.coords = annot.getAsArray(PdfName.QUADPOINTS);

        // Since PDF 1.5: 
        // RC = contents-richtext

        this.dict = annot;
    }

    /**
     * PDF Annotation keys that directly map to XML attributes.
     */
    public static abstract class Field {
        public final String attr;
        public final PdfName name;
        Field(String attr, String name) {
            this.attr = attr;
            this.name = new PdfName(name);
        }
        protected PdfObject getObjectFrom( PdfDictionary dict ) {
            return dict.get( this.name );
        }
        public String getFrom(PdfDictionary dict) {
            PdfObject object = this.getObjectFrom( dict );
            if ( object == null ) return null;
            return object.toString();
        }
    }

    public static class NumberField extends Field {
        NumberField(String attr, String name) { super(attr, name); }
        protected PdfObject getObjectFrom( PdfDictionary dict ) {
            return dict.getAsNumber( this.name );
        }
    }

    public static class StringField extends Field {
        StringField(String attr, String name) { super(attr, name); }
        protected PdfObject getObjectFrom( PdfDictionary dict ) {
            return dict.getAsString( this.name );
        }
    }

    public static class RectField extends Field {
        RectField(String attr, String name) { super(attr, name); }
        /**
         * Convert a PdfArray, that is in a PdfDictionary, to a PdfRectangle.
         * This method should better be included in iText.
         * @param dict Which PdfDictionary to get the PdfArray from.
         */
        protected PdfObject getObjectFrom( PdfDictionary dict ) {
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
        public String getFrom(PdfDictionary dict) {
            PdfRectangle r = (PdfRectangle)this.getObjectFrom( dict );
            if ( r == null ) return null;
            return r.left()+","+r.bottom()+","+r.right()+","+r.top();
        }
    }

    public static class ColorField extends Field {
        ColorField(String attr, String name) { super(attr, name); }
        public String getFrom( PdfDictionary dict ) {
            PdfArray array = dict.getAsArray( this.name );
            if ( array == null ) return null;
            // TODO
            // contains an array of three numbers between 0.0 and 1.0 in the
            // deviceRGB color space. In XFDF, each color is mapped to a value
            // between 0 and 255 then converted to hexadecimal (00 to FF)
            // this.setAttr("color",s);  // TODO: => #xxxxxx
            return null;
        }
    }

    /**
     * PDF Annotation flags.
     */
    public static enum Flag {
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

    public static class FlagField extends Field {
        FlagField(String attr, String name) { super(attr, name); }
        public String getFrom( PdfDictionary dict ) {
            PdfNumber number = dict.getAsNumber( this.name );
            if ( number == null ) return null;
            int flags = number.intValue();
            StringBuilder s = new StringBuilder();
            if ( flags != 0 ) {
                for ( Flag f : Flag.values() ) {
                    if ( ( flags & f.bit ) != 0 ) {
                        if ( s.length() > 0 ) s.append(",");
                        s.append( f.name );
                    }
                }
            }
            return s.toString();
        }
    }

    /**
     * Annotation attributes as XFDF attributes and PDF keys.
     */
    public static final Field[] fields = new Field[] {
        new NumberField ("page","Page"),
        new ColorField  ("color","C"),
        new StringField ("date","M"),
        new FlagField   ("flags","F"),
        new StringField ("name","NM"),
        new RectField   ("rect","Rect"),
        new StringField ("title","T"),
        new StringField   ("creationdate","CreationDate"),
        new NumberField ("opacity","CA"),
        new StringField ("subject","Subj"),
        new StringField ("intent ","IT"),
        // TODO: coords : QuadPoints
        // TODO: inreplyto : IRT.name
        new StringField ("replyType","RT"),
        new StringField ("icon","Name"),
        new StringField ("state","State"),
        new StringField ("statemodel","StateModel"),
        // TODO: start & end : L
        // TODO: head & tail : LE
        new ColorField  ("interior-color","IC"),
        new NumberField ("leaderLength","LL"),
        new NumberField ("leaderExtend","LLE"),
        new StringField ("caption","Cap"),
        new NumberField ("leader-offset","LLO"),
        new StringField ("caption-style","CP"),
        // TODO: caption-offset-h & caption-offset-v : C0
        new RectField   ("fringe","RD"),
        // TODO: symbol : Sy with none <=> None, paragraph <=> P
        // TODO: justification : Q with left <=> 0, centered <=> 1, right <=> 2
        new NumberField ("rotation","Rotate"),
        // omitted sound annotation fields: bits, channels, encoding, rate
        // TODO: new BooleanField ("open","Open"), yes / no ?
        new StringField ("Highlight","H"),
        new StringField ("overlay-text","OverlayText")
        // TODO: overlay-text-repeat : Repeat = true / false
    };

    /* Border effect attributes */
    // new NumberField ("intensity","I"),
    // style : S (with value-mapping)
    /* Border style attributes */
    // new NumberField ("width","W"),
    // dashes: D (comma-separated list of 1 or more numbers) 
    // style
    /* Border array attributes */
    // HCornerRadius & VCornerRadius & Width & DashPattern : Border

    // omitted attributes for: embedded file, stream, file specification, 
    // destination syntax, remote go-to, launch, named action, URI, Mac OS file

    //public static final Map<PdfName,String> subtypes;
    //public static final Field[] fields = new Field[] {
/*
    static {
       subtypes = new HashMap<PdfName,String>();
       subtypes.add(
    }
*/

    public static final Map<PdfName, String> subtypes;
    static {
        HashMap<PdfName, String> map = new HashMap<PdfName, String>();
        map.put(PdfName.TEXT, "text");
        map.put(PdfName.HIGHLIGHT, "highlight");
        map.put(PdfName.INK, "ink");
        map.put(PdfName.UNDERLINE, "underline");
        map.put(PdfName.POPUP, "popup");
        subtypes = Collections.unmodifiableMap(map);
    }

    /**
     * Write the annotation in XFDF format.
     * @param output Where to write the XML to.
     */
    public void writeXML( PrintWriter out ) {
        Map<String,String> attrs = new HashMap<String,String>();

        for ( Field a : this.fields ) {
            String value = a.getFrom( this.dict );
            if (value != null) { // TODO: encoding & exception
                attrs.put(a.attr,value);
            }
        }

        String attrstring = "";
        for ( Map.Entry<String,String> attr : attrs.entrySet() ) {
            attrstring += "\n  " + attr.getKey() + "=\"" + attr.getValue() + "\"";
        }

        String innerstring = "";
        // TODO: character encoding not UTF-8? escape?

        String element = subtypes.get(this.subtype);
        if (element == null) { // TODO
            element = this.subtype.toString();
        }

        // TODO: this.content may be null. Either use content or rich-content
        if (this.content != null && !this.content.equals("")) {
            innerstring = "\n  <content>" + content  + "</content>\n";
        }
            // TODO: contents-richtext
            // TODO: popup

        out.println("<"+element+attrstring+">"+innerstring+"</"+element+">");
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