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

# Usage
You need maven2 to compile and run this application. Then try:

    $ mvn assembly:single

If you are lucky, maven will install all required dependencies, compile,
and create a single jar that includes all dependencies. You can run it:

    $ java -jar target/marginalia-0.0.1dev-jar-with-dependencies.jar file.pdf

Or try:

    $ mvn compile
    $ mvn exec:java -Dexec.args="..."

# Author
Jakob Voss <jakob.voss@gbv.de>
