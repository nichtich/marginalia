<?xml version="1.0" encoding="utf-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:f="http://ns.adobe.com/xfdf/"
    xmlns:m="http://example.com/" 
    xmlns="http://www.w3.org/2000/svg"
    exclude-result-prefixes="f m">

    <xsl:output method="xml" encoding="utf-8" indent="yes" media-type="image/svg+xml"/>

    <xsl:param name="page">1</xsl:param>

    <xsl:template match="/">
    	<!--xsl:apply-templates match="f:annots"/-->
    	<xsl:apply-templates match="f:annots[@page=$page]"/>
    </xsl:template>

    <xsl:template match="f:annots">
        <xsl:comment>page <xsl:value-of select="$page"/></xsl:comment>
        <xsl:apply-templates select="m:pages/m:page[@number=$page]" mode="svg"/> 
    </xsl:template>

    <!-- so far we ignore @rotate (may be implemented with transform="") -->
    <xsl:template match="m:page" mode="svg">
        <svg height="{@top - @bottom}" width="{@right - @left}">
            <rect fill="#fff" stroke="black" stroke-width="1px">
                <xsl:apply-templates select="." mode="rect-coords"/>
            </rect>
            <!-- Transform to SVG coordinate system -->
            <g transform="scale(1, -1) translate(0,-{@top})">
                <xsl:apply-templates select="../../f:*"/>
            </g>
        </svg>
    </xsl:template>

    <xsl:template match="m:*" mode="rect-coords">
        <xsl:attribute name="x"><xsl:value-of select="@left"/></xsl:attribute>
        <xsl:attribute name="y"><xsl:value-of select="@bottom"/></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="@top - @bottom"/></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="@right - @left"/></xsl:attribute>
    </xsl:template>

    <xsl:template match="f:highlight|f:underline|f:ink">
	<rect stroke="{@color}" stroke-width="1px" stroke-opacity="0.5" stroke-dasharray="9,5" fill="none">
            <xsl:apply-templates select="m:rect" mode="rect-coords"/>
	</rect>
	<!-- highlight, underline etc. -->
        <xsl:if test="@coords">
          <xsl:call-template name="quads">
            <xsl:with-param name="list" select="@coords"/>
            <xsl:with-param name="color" select="@color"/>
          </xsl:call-template>
        </xsl:if>
        <!-- ink -->
        <xsl:apply-templates select="f:inklist/f:gesture">
          <xsl:with-param name="color" select="@color"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template name="quads">
        <xsl:param name="list"/>
        <xsl:param name="color"/>
        <xsl:variable name="x1" select="substring-before($list,',')"/>
	<xsl:variable name="a"  select="substring-after($list,',')"/>
      	<xsl:variable name="y1" select="substring-before($a,',')"/>
        <xsl:variable name="b"  select="substring-after($a,',')"/>
        <xsl:variable name="x2" select="substring-before($b,',')"/>
        <xsl:variable name="c" select="substring-after($b,',')"/>
        <xsl:variable name="y2" select="substring-before($c,',')"/>
        <xsl:variable name="d" select="substring-after($c,',')"/>
        <xsl:variable name="x3" select="substring-before($d,',')"/>
        <xsl:variable name="e" select="substring-after($d,',')"/>
        <xsl:variable name="y3" select="substring-before($e,',')"/>
        <xsl:variable name="f" select="substring-after($e,',')"/>
	<xsl:variable name="x4" select="substring-before($f,',')"/>
        <xsl:variable name="g" select="substring-after($f,',')"/>
        <xsl:variable name="y4" select="substring-before($g,',')"/>
        <xsl:variable name="h" select="substring-after($g,',')"/>

      <polygon stroke="none" fill="{$color}">
        <xsl:attribute name="points">
	<xsl:value-of select="$x1"/>
	<xsl:text>,</xsl:text>
	<xsl:value-of select="$y1"/>
        <xsl:text> </xsl:text>
	<xsl:value-of select="$x2"/>
	<xsl:text>,</xsl:text>
	<xsl:value-of select="$y2"/>
        <xsl:text> </xsl:text>
	<xsl:value-of select="$x4"/> <!-- TODO: check which, 3 or 4 -->
	<xsl:text>,</xsl:text>
	<xsl:value-of select="$y4"/>
        <xsl:text> </xsl:text>
	<xsl:value-of select="$x3"/>
	<xsl:text>,</xsl:text>
	<xsl:value-of select="$y3"/>
        </xsl:attribute>
      </polygon>
      <xsl:if test="$h != ''">
        <xsl:call-template name="quads">
          <xsl:with-param name="list" select="$h"/>
          <xsl:with-param name="color" select="$color"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:template>

    <xsl:template match="f:gesture">
      <xsl:param name="color"/>
      <polyline points="{translate(translate(.,',',' '),';',',')}" stroke="{$color}" fill="none"/>
    </xsl:template>

    <xsl:template match="*"/>

</xsl:transform>
