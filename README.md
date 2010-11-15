# Name
Marginalia - extract annotations from PDF files

# Description
This is just an experiment with PDF file format, iText, and maven

# Background

> We are using the computer as a paper simulator, which is like tearing 
> the wings off a 747 and driving it as a bus on the highway.
> -- Ted Nelson

PDF files are crap, but we somehow have to live with them for a while.
This application contains some experiment in at least making use of
annotations in PDF files. Annotations should be publications of their
own, but they are hidden in PDF or other proprietary software.

There is free software, open source PDF reader that supports annotations.
There is no eBook-reader or similar device that is a convenient to use
to make annotations, as a physical piece of paper and a pen. So far.

To get into the PDF file format, which in fact is more like a 
database or a file system, you need PDF parser libraries, like 
[iText](http://www.itextpdf.com/).

## PDF Reference

[Adobe PDF Reference Archives](http://www.adobe.com/devnet/pdf/pdf_reference_archive.html).

PDF annotations are defined in chapter 8.4 of the PDF reference (2006),
page 604-647. 
[see here](http://www.verypdf.com/document/pdf-format-reference/pg_0604.htm).

# Usage of the developer snapshot

First download, clone or fork the project, e.g.

   $ git clone git://github.com/nichtich/marginalia.git
   $ cd marginalia

You need maven2 to compile and run this application. Try

    $ mvn compile

If you are lucky, maven will install all required dependencies and compile.
You can then run marginalia in the development environment:

    $ mvn exec:java -Dexec.args="yourfile.pdf"

To create a single jar that includes all dependencies, call:

     $ mvn assembly:single

You can then copy the jar file to a place of your choice and run it via:

    $ java -jar target/marginalia-0.0.1dev-jar-with-dependencies.jar yourfile.pdf

## Extracting text

To extract annotated text, you can use the pdftotext command line tool from 
poppler (maybe I better move from iText to poppler). For instance if you have
an annotation on page 1 with:

    rect="52.559917,437.8619,286.3729,528.27844"

and page size is 595 x 842 pts (A4). Then the crop area can be calculated
with `x = 52`, `y = 842 - 528.27 = 313`, `W = 233`, `H = 91`.

    $ pdftotext -layout -nopgbrk -f 1 -l 1 -x 52 -y 313 -W 233 -H 91 your.pdf && cat your.txt

Sure this should be automized, and it does not cover details.

## Next steps

We could add custom XML elements to fix the ill-designed XFDF format.
The following format could also include the extracted text.

	<highlight ...>
	  <m:text>...</m:text>
	  <m:quad x1="" y1="" x2="" y2="" x3="" y3="" x4="" y4="">...</m:quad>
	  <m:quad x1="" y1="" x2="" y2="" x3="" y3="" x4="" y4="">...</m:quad>
	  ...
	</highlight>

# Author
Jakob Voss <jakob.voss@gbv.de>
