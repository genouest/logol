<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0">
<!-- History: 27-04-08 Fix 1330 Wrong position on reverse -->
<xsl:output omit-xml-declaration="yes"/>

<xsl:param name="offset">0</xsl:param>
<xsl:param name="fileid">0</xsl:param>

<xsl:template match="/">
<xsl:text>
</xsl:text>
     <xsl:apply-templates/>
<xsl:text>
</xsl:text>     
</xsl:template>

<xsl:template name="Min">
  <xsl:param name="positions"/>
  <xsl:for-each select="$positions">
     <xsl:sort order="ascending" select="." data-type="number"/>
     <xsl:if test="position()=1">
	 <xsl:choose>
		 <xsl:when test="$offset='0'">
	         <xsl:value-of select="."/>
		 </xsl:when>
		 <xsl:otherwise>
			<xsl:value-of select="$offset -1 - ."/>
		 </xsl:otherwise>
	</xsl:choose>
     </xsl:if>
  </xsl:for-each>
</xsl:template>

<xsl:template name="Max">
  <xsl:param name="positions"/>
  <xsl:for-each select="$positions">
     <xsl:sort order="descending" select="." data-type="number"/>
     <xsl:if test="position()=1">
	 <xsl:choose>
		 <xsl:when test="$offset='0'">
	         <xsl:value-of select=". - 1"/>
		 </xsl:when>
		 <xsl:otherwise>
			<xsl:value-of select="$offset - ."/>
		 </xsl:otherwise>
	</xsl:choose>
     </xsl:if>
  </xsl:for-each>
</xsl:template>

<xsl:template match="model">
   <xsl:element name="{name()}">
 <xsl:for-each select="@*"><xsl:attribute name="{name()}"><xsl:value-of select="."/>
  </xsl:attribute>
   </xsl:for-each>
  <begin>
     <xsl:call-template name="Min">
        <xsl:with-param name="positions" select="variable/data/begin"/>
     </xsl:call-template> 
  </begin>
  <end>
     <xsl:call-template name="Max">
        <xsl:with-param name="positions" select="variable/data/end"/>
     </xsl:call-template>
  </end>
  <errors>
     <xsl:value-of select="sum(variable/data/errors)"/>
  </errors>
  <distance>
     <xsl:value-of select="sum(variable/data/distance)"/>
  </distance>  
   <xsl:apply-templates>
        <xsl:sort select="data/begin" data-type="number" order="ascending"/>
  </xsl:apply-templates>
  </xsl:element>
</xsl:template>

<xsl:template match="sequences">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="match">
  <xsl:for-each select="model">
  <xsl:element name="match">
  <!--<xsl:for-each select="@*"><xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
  </xsl:for-each>
  -->
  <model>
      <xsl:value-of select="@id"/>
  </model>
  <id>
      <xsl:value-of select="$fileid"/>-<xsl:value-of select="../@id"/>
  </id>

  <begin>
     <xsl:call-template name="Min">
        <xsl:with-param name="positions" select="variable/data/begin"/>
     </xsl:call-template>
  </begin>
  <end>
     <xsl:call-template name="Max">
        <xsl:with-param name="positions" select="variable/data/end"/>
     </xsl:call-template>
  </end>
  <errors>
     <xsl:value-of select="sum(variable/data/errors)"/>
  </errors>
  <distance>
     <xsl:value-of select="sum(variable/data/distance)"/>
  </distance>

   <xsl:apply-templates>
        <xsl:sort select="data/begin" data-type="number" order="ascending"/>
  </xsl:apply-templates>
  
  </xsl:element>
  </xsl:for-each>
</xsl:template>

<xsl:template match="variable">
   <xsl:element  name="{name()}">
   <xsl:for-each select="@*"><xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
   </xsl:for-each>
  <xsl:apply-templates/>
   </xsl:element>
</xsl:template>

<xsl:template match="data">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="subdata">
  <xsl:apply-templates>
        <xsl:sort select="data/begin" data-type="number" order="ascending"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="begin">
<begin>
	 <xsl:choose>
		 <xsl:when test="$offset='0'">
	         <xsl:value-of select="."/>
		 </xsl:when>
		 <xsl:otherwise>
			<xsl:value-of select="$offset -1 - ."/>
		 </xsl:otherwise>
	</xsl:choose>
</begin>
</xsl:template>

<xsl:template match="end">
<end>
	 <xsl:choose>
		 <xsl:when test="$offset='0'">
	         <xsl:value-of select=". - 1"/>
		 </xsl:when>
		 <xsl:otherwise>
			<xsl:value-of select="$offset - ."/>
		 </xsl:otherwise>
	</xsl:choose>
</end>
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


</xsl:stylesheet>
