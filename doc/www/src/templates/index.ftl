<#include "header.ftl">
	
	<#include "menu.ftl">

  <!--- forward to about page -->
  <script>
       window.location.href = "<#if (content.rootpath)??>${content.rootpath}<#else></#if>about.html"
  </script>

<#include "footer.ftl">