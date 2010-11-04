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

    // optional fields
    protected PdfString content;
    protected PdfString name;
    protected PdfString date;
    protected PdfNumber flags;
    protected PdfDictionary borderstyle;
    protected PdfArray border;
    protected PdfArray rgb;
    protected PdfNumber opacity;

    protected PdfArray quadpoints;

    /**
     * Constructs a new Annotation from a given PdfDictionary.
     * Of course the PdfDictionary should contain an annotation.
     */
    public Annotation(PdfDictionary annot) {
        this.subtype  = annot.getAsName(PdfName.SUBTYPE);
        this.rect     = getAsRectangle(annot,PdfName.RECT);
        // TODO: throw exception if subtype or rect are null

        this.content  = annot.getAsString(PdfName.CONTENTS); // optional

        // more optional fields
        this.name     = annot.getAsString(PdfName.NM);
        this.date     = annot.getAsString(PdfName.M);
        this.flags    = annot.getAsNumber(PdfName.F);
        this.borderstyle = getAsDictionary(annot,PdfName.BS);
        this.border = annot.getAsArray(PdfName.BORDER);

        // skipped fields:
        // getAsDictionary(annot,PdfName.AP);
        // annot.getAsName(PdfName.AS);
        // getAsDictionary(annot,PdfName.A); // action
        // getAsDictionary(annot,PdfName.A); // additional action

        this.rgb = annot.getAsArray(PdfName.C);

        this.opacity = annot.getAsNumber(PdfName.CA);

        // this.popup = getAsDictionary(annot,PdfName.POPUP);
        // only for popup:
        // this.titlelabel = annot.getAsString(PdfName.T);

        // annot.getAsNumber(PdfName.STRUCTPARENT);

        // for subtype one of Highlight, Underline, Squiggly, or StrikeOut:
        this.quadpoints = annot.getAsArray(PdfName.QUADPOINTS);

        // for subtype Ink
        // ...

    }

    public String toString() {
        String s = this.subtype + " at " + this.rect;
        if (this.date != null) s += "\n " + this.date;
        if (this.content != null) s += "\n '" + this.content + "'";
        return s;
    }

    // TODO: write in XFDF format

    /**
     * Converts a PdfArray to an PdfRectangle.
     * I wonder why this is not implemented in iText.
     */
    public static PdfRectangle getAsRectangle(PdfDictionary dict, PdfName name) {
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

    public static PdfDictionary getAsDictionary(PdfDictionary dict, PdfName name) {
        PdfObject obj = dict.getDirectObject(name);
        if (obj == null || !obj.isDictionary()) return null;
        return (PdfDictionary)obj;
    }

}