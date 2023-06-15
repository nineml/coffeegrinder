<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs"
                expand-text="yes"
                version="3.0">

<xsl:output method="text" encoding="utf-8" indent="no"/>
<xsl:strip-space elements="*"/>

<xsl:param name="label-color" select="'none'"/>
<xsl:param name="show-states" select="'false'"/>
<xsl:param name="elide-root" select="'false'"/>
<xsl:param name="rankdir" select="'TB'"/>
<xsl:param name="terminal-shape" select="'house'"/>
<xsl:param name="subgraph-color" select="'white'"/>

<xsl:template match="sppf">
  <xsl:text>digraph SPPF {{&#10;</xsl:text>
  <xsl:text>rankdir={$rankdir}&#10;</xsl:text>
  <xsl:apply-templates select="*[$elide-root = 'false' or not(@label = '$$')]"/>
  <xsl:text>}}&#10;</xsl:text>
</xsl:template>

<xsl:template match="sppf/*">
  <xsl:text>subgraph cluster_{@id/string()} {{&#10;</xsl:text>
  <xsl:text>style="filled"; color="{$subgraph-color}";&#10;</xsl:text>

  <xsl:if test="$label-color != 'none'">
    <xsl:text>label={local-name(.)}; </xsl:text>
    <xsl:text>fontsize="10pt"; fontcolor={$label-color}&#10;</xsl:text>
  </xsl:if>

  <xsl:variable name="shape"
                select="if (@type = 'state')
                        then 'box'
                        else if (@type = 'terminal')
                             then $terminal-shape
                             else 'oval'"/>

  <xsl:variable name="label" as="xs:string+">
    <xsl:sequence select="replace(replace(@label, '\\', '\\\\'), '&quot;', '\\&quot;')"/>
    <xsl:if test="@type = 'nonterminal' and $show-states != 'false'">
      <xsl:sequence select="replace(replace(@state, '\\', '\\\\'), '&quot;', '\\&quot;')"/>
    </xsl:if>
    
    <xsl:if test="@type = 'terminal' and @leftExtent and @rightExtent">
      <xsl:variable name="left" select="xs:integer(@leftExtent)"/>
      <xsl:variable name="right" select="xs:integer(@rightExtent)"/>
      <xsl:variable name="extent" as="xs:string">
        <xsl:choose>
          <xsl:when test="$left != $right and $left + 1 != $right">
            <xsl:sequence select="$left+1 || ' – ' || $right+1"/>
          </xsl:when>
          <xsl:when test="$left + 1 = $right">
            <xsl:sequence select="string($left+1)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="'ε'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="ambig" as="xs:string?">
        <xsl:if test="@trees ne '1'">
          <xsl:sequence select="' / ' || @trees"/>
        </xsl:if>
      </xsl:variable>

      <xsl:sequence select="'« ' || $extent || ' »' || $ambig"/>
    </xsl:if>
  </xsl:variable>

  <xsl:variable name="style"
                select="if (@type = 'state') then 'style=rounded' else ''"/>

  <xsl:text>node{@id/string()} [ label="{string-join($label, '&#10;')}" </xsl:text>
  <xsl:text>shape={$shape} {$style} ]&#10;</xsl:text>
  <xsl:apply-templates/>
  <xsl:text>}}&#10;</xsl:text>
</xsl:template>

<xsl:template match="pair">
  <xsl:text>node{../@id/string()} -&gt; anon{generate-id(.)};&#10;</xsl:text>
  <xsl:text>anon{generate-id(.)} [ label="" shape=circle width=0.25 height=0.25 fixedsize=true ]&#10;</xsl:text>
  <xsl:text>{{ rank=same;&#10;</xsl:text>
  <xsl:text>node{link[1]/@target/string()} -&gt; node{link[2]/@target/string()} </xsl:text>  
  <xsl:text>[ style=invis ]}}&#10;</xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="link">
  <xsl:text>node{../@id/string()} -&gt; node{@target/string()};&#10;</xsl:text>
</xsl:template>

<xsl:template match="pair/link" priority="100">
  <xsl:text>anon{generate-id(..)} -&gt; node{@target/string()};&#10;</xsl:text>
</xsl:template>

<xsl:template match="epsilon">
  <xsl:text>epsilon{generate-id(.)} [ label="ε" shape=none ]&#10;</xsl:text>
  <xsl:text>node{../@id/string()} -&gt; epsilon{generate-id(.)};&#10;</xsl:text>
</xsl:template>

</xsl:stylesheet>
