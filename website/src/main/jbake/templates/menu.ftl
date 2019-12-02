	<!-- Fixed navbar -->
	<div class="container">
		<nav class="navbar navbar-expand-lg navbar-light bg-light">
			<div class="animated fadeIn" style="animation-duration: 5s;">
				<a class="navbar-brand" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>"><img src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>images/MessageVortexLogo.png" alt="MessageVortex Logo" style="width: 30px; position:relative; top:-6px;"/><span style="font-size:36px;margin:3px;">MessageVortex</span></a>
			</div>
			<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
				<span class="navbar-toggler-icon"></span>
			</button>
			<div class="collapse navbar-collapse" id="navbarSupportedContent">
				<ul class="navbar-nav mr-auto">
					<li class="nav-item"><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>index.html" class="nav-link">Home</a></li>
					<li class="nav-item"><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>about.html" class="nav-link">About</a></li>
					<li class="nav-item"><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>documentation.html" class="nav-link">Documentation</a></li>
					<li class="nav-item"><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>download.html" class="nav-link">Download</a></li>
					<li class="nav-item"><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>${config.feed_file}" class="nav-link">Subscribe</a></li>
				</ul>
			</div>
		</nav>
	</div>
    <div class="container">