<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs"
                expand-text="yes"
                version="3.0">

<xsl:output method="text" encoding="utf-8" indent="no"/>
<xsl:strip-space elements="*"/>

<xsl:param name="label-color" select="'none'"/>

<xsl:template match="/*" priority="100">
  <xsl:text>digraph tree {{&#10;</xsl:text>
  <xsl:next-match/>
  <xsl:text>}}&#10;</xsl:text>
</xsl:template>

<xsl:template match="state|symbol">
  <xsl:text>subgraph cluster_{@id/string()} {{&#10;</xsl:text>
  <xsl:text>style=filled; color=white;&#10;</xsl:text>

  <xsl:if test="$label-color != 'none'">
    <xsl:text>label={@id}&#10;</xsl:text>;
    <xsl:text>fontsize="10pt"; fontcolor={$label-color}&#10;</xsl:text>;
  </xsl:if>

  <xsl:variable name="label" as="xs:string+">
    <xsl:sequence select="replace(@label, '&quot;', '\\&quot;')"/>
  </xsl:variable>

  <xsl:variable name="shape"
                select="if (self::state) 
                        then 'box'
                        else if (@type = 'terminal')
                             then 'house'
                             else 'oval'"/>
  <xsl:variable name="style"
                select="if ($shape = 'box') then 'style=rounded' else ''"/>

  <xsl:text>node{@id/string()} [ label="{string-join($label, ' ')}" </xsl:text>
  <xsl:text>shape={$shape} {$style} ]&#10;</xsl:text>

  <xsl:for-each select="state|symbol">
    <xsl:text>node{../@id/string()} -&gt; node{@id/string()};&#10;</xsl:text>
  </xsl:for-each>

  <xsl:apply-templates/>
  <xsl:text>}}&#10;</xsl:text>
</xsl:template>

<xsl:template match="epsilon">
  <xsl:text>epsilon{generate-id(.)} [ label="ε" shape=none ]&#10;</xsl:text>
  <xsl:text>node{../@id/string()} -&gt; epsilon{generate-id(.)};&#10;</xsl:text>
</xsl:template>

</xsl:stylesheet>
