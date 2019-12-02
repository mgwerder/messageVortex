<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8"/>
    <title><#if (content.title)??><#escape x as x?xml>${content.title}</#escape><#else>MessageVortex</#if></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="MessageVortex is a state-of-the-art protocol providing anonymity without trusting any infrastructure. It is free and licensed under a very permissive license.">
    <meta name="author" content="Martin Gwerder">
    <meta name="keywords" content="Anonymity Messaging ">
    <meta name="generator" content="JBake">
	
	<!-- JQuery -->
	<script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/jquery-1.11.1.min.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/bootstrap.min.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/prettify.js"></script>

    <!-- Le styles -->
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/bootstrap.min.css" rel="stylesheet">
    <!--<link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/asciidoctor.css" rel="stylesheet">-->
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/prettify.css" rel="stylesheet">

    <!-- the animations -->
	<link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/animate.min.css" rel="stylesheet">
	
	<!-- reference your copy Font Awesome here (from our CDN or by hosting yourself) -->
	<link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>fontawesome5/css/fontawesome.css" rel="stylesheet">
	<link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>fontawesome5/css/brands.css" rel="stylesheet">
	<link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>fontawesome5/css/solid.css" rel="stylesheet">

    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/base.css" rel="stylesheet">

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/html5shiv.min.js"></script>
    <![endif]-->

    <!-- Fav and touch icons -->
    <!--<link rel="apple-touch-icon-precomposed" sizes="144x144" href="../assets/ico/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="../assets/ico/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72" href="../assets/ico/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed" href="../assets/ico/apple-touch-icon-57-precomposed.png">-->
    <link rel="shortcut icon" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>favicon.ico">
  </head>
  <body>
    <div id="wrap">
   