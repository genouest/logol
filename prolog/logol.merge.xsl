<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0">

<xsl:output omit-xml-declaration="yes"/>

<xsl:param name="fileid">0</xsl:param>

<xsl:template match="/">
<xsl:text>
</xsl:text>
     <xsl:apply-templates/>
<xsl:text>
</xsl:text>     
</xsl:template>

<xsl:template match="sequences">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="match">
  <xsl:element name="match">
   <xsl:apply-templates/>  
  </xsl:element>
</xsl:template>

<xsl:template match="variable">
   <xsl:element  name="{name()}">
   <xsl:for-each select="@*"><xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
   </xsl:for-each>
  <xsl:apply-templates/>
   </xsl:element>
</xsl:template>

<xsl:template match="size">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="content">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="errors">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="distance">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="text">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="begin">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="end">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="model">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="fastaHeader">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="fastaHeader">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="sequenceBegin">
</xsl:template>

<xsl:template match="sequenceEnd">
</xsl:template>

<xsl:template match="id">
  <id>
      <xsl:value-of select="$fileid"/>-<xsl:value-of select="."/>
  </id>
 </xsl:template>



</xsl:stylesheet>
