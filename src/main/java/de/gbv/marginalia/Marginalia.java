package de.gbv.marginalia;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import jargs.gnu.CmdLineParser;

import com.itextpdf.text.Rectangle;

import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;

/**
 * Marginalia command line client.
 *
 * This is just a skeleton that can only parse PDF files and list all 
 * annotations for each page. To extend this script, you need to have 
 * a look at the PDF file format specification.
 */
public class Marginalia {

    private static void printUsage() {
        System.err.println("usage: marginalia file.pdf");
    }

    public static void main( String[] args ) {
        if ( args.length == 0 ) {
            printUsage();
            System.exit(2);
        }

        try {
            PrintWriter writer = new PrintWriter(System.out);

            // String[] otherArgs = parser.getRemainingArgs();
            for ( int i = 0; i < args.length; ++i ) {
                inspect(writer, args[i]);

                // other actions:
                // remove all annotations from the document
                // reader.removeAnnotations()
            }

            writer.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Inspect a PDF file and write the info to a writer
     * @param writer Writer to a text file
     * @param filename Path to the PDF file
     * @throws IOException
     */
    public static void inspect(PrintWriter writer, String filename)
        throws IOException {
        writer.println(filename);
        writer.flush();

        PdfReader reader = new PdfReader(filename);

        writer.println("Number of pages: "+reader.getNumberOfPages());
        Rectangle mediabox = reader.getPageSize(1);
        writer.print("Size of page 1: [");
        writer.print(mediabox.getLeft());
        writer.print(',');
        writer.print(mediabox.getBottom());
        writer.print(',');
        writer.print(mediabox.getRight());
        writer.print(',');
        writer.print(mediabox.getTop());
        writer.println("]");
        writer.print("Rotation of page 1: ");
        writer.println(reader.getPageRotation(1));
        writer.print("Page size with rotation of page 1: ");
        writer.println(reader.getPageSizeWithRotation(1));
        writer.println();
        writer.flush();

        for (int pageNum=1; pageNum<=reader.getNumberOfPages(); pageNum++) {

            PdfDictionary pageDic = reader.getPageN(pageNum);

            PdfArray annotsArray = pageDic.getAsArray(PdfName.ANNOTS);
            if ( annotsArray == null || annotsArray.isEmpty() ) {
                writer.println("page "+pageNum+" contains no annotations");
                continue;
            }

            writer.println("page "+pageNum+" has "+annotsArray.size()+" annotations");

            for(int i=0; i<annotsArray.size(); i++) {
                PdfObject obj = annotsArray.getDirectObject(i);
                if (!obj.isDictionary()) continue;
                inspectAnnotation((PdfDictionary)obj, writer);
            }
        }
        writer.println();
        writer.flush();
    }

    public static void inspectAnnotation(PdfDictionary annot, PrintWriter out) {
        //if (!annot.getAsName(PdfName.TYPE).compareTo(PdfName.ANNOT))
        PdfName subtype = annot.getAsName(PdfName.SUBTYPE);
        out.println("SUBTYPE: "+subtype);
        if (subtype.equals(PdfName.TEXT)) {
            out.println( annot.getAsString(PdfName.CONTENTS) );
        }
        // TODO: other annotation types
        // See PDF reference 1.7 (2006), chapter 8.4, p. 604-647
    }
}
