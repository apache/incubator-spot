<!doctype html>

<!--[if lt IE 7]><html lang="en-US" class="no-js lt-ie9 lt-ie8 lt-ie7"><![endif]-->
<!--[if (IE 7)&!(IEMobile)]><html lang="en-US" class="no-js lt-ie9 lt-ie8"><![endif]-->
<!--[if (IE 8)&!(IEMobile)]><html lang="en-US" class="no-js lt-ie9"><![endif]-->
<!--[if gt IE 8]><!-->
<html lang="en-US" class="no-js">
    <!--<![endif]-->

    <head>
        <meta charset="utf-8">

        <meta http-equiv="X-UA-Compatible" content="IE=edge">

        <title>
			<?php if (isset($pageTitle)) {
	            echo $pageTitle;
	        } else {
	            echo "Apache Spot";
	        } ?>        	
        </title>

        <meta name="HandheldFriendly" content="True">
        <meta name="MobileOptimized" content="320">
        <meta name="viewport" content="width=device-width, initial-scale=1"/>

        <link rel="apple-touch-icon" href="<?php echo BASE_URL; ?>/library/images/apple-touch-icon.png">
        <link rel="icon" href="<?php echo BASE_URL; ?>/favicon.png">
        <!--[if IE]>
        <link rel="shortcut icon" href="http://spot.incubator.apache.org/favicon.ico">
        <![endif]-->
        <meta name="msapplication-TileColor" content="#f01d4f">
        <meta name="msapplication-TileImage" content="<?php echo BASE_URL; ?>/library/images/win8-tile-icon.png">
        <meta name="theme-color" content="#121212">

        <link rel='dns-prefetch' href='//fonts.googleapis.com' />
        <link rel='dns-prefetch' href='//s.w.org' />
        <link rel="alternate" type="application/rss+xml" title="Apache Spot &raquo; Feed" href="<?php echo BASE_URL; ?>/feed/" />

        <link rel='stylesheet' id='googleFonts-css'  href='http://fonts.googleapis.com/css?family=Lato%3A400%2C700%2C400italic%2C700italic' type='text/css' media='all' />
        <link rel='stylesheet' id='bones-stylesheet-css'  href='<?php echo BASE_URL; ?>/library/css/style.css' type='text/css' media='all' />
        <!--[if lt IE 9]>
        <link rel='stylesheet' id='bones-ie-only-css'  href='http://spot.incubator.apache.org/library/css/ie.css' type='text/css' media='all' />
        <![endif]-->
        <link rel='stylesheet' id='mm-css-css'  href='<?php echo BASE_URL; ?>/library/css/meanmenu.css' type='text/css' media='all' />
        <script type='text/javascript' src='<?php echo BASE_URL; ?>/library/js/libs/modernizr.custom.min.js'></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
        <script type='text/javascript' src='<?php echo BASE_URL; ?>/library/js/jquery-migrate.min.js'></script>
        <script type='text/javascript' src='<?php echo BASE_URL; ?>/library/js/jquery.meanmenu.js'></script>

		<script>
		  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
		  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
		  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
		  })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');
		
		  ga('create', 'UA-87470508-1', 'auto');
		  ga('send', 'pageview');
		
		</script>
    </head>

    <body class="<?php if (isset($bodyclass)) {
            echo $bodyclass;
        } else {
            echo "page";
        } ?>" itemscope itemtype="http://schema.org/WebPage">

        <div id="container">
			<div class="social-sidebar">
				<a href="mailto:info@open-network-insight.org"><span class="icon-envelope"></span></a>
				<a href="https://twitter.com/ApacheSpot" target="_blank"><span class="icon-twitter"</a>
				<a href="http://slack.apache-spot.io/" target="_blank"><span class="icon-slack"></span></a>				
			</div>
            <header class="header" role="banner" itemscope itemtype="http://schema.org/WPHeader">

                <div id="inner-header" class="wrap cf">

                    <p id="logo" class="h1" itemscope itemtype="http://schema.org/Organization">
                        <a href="<?php echo BASE_URL; ?>" rel="nofollow"><img src="<?php echo BASE_URL; ?>/library/images/logo.png" alt="Apache Spot" /></a>
                    </p>

                    <nav role="navigation" itemscope itemtype="http://schema.org/SiteNavigationElement">
                        <ul id="menu-main-menu" class="nav top-nav cf">
                            <li id="menu-item-129" class="menu-item menu-item-type-custom menu-item-object-custom menu-item-129">
                                <a target="_blank" href="https://github.com/apache/incubator-spot#try-the-apache-spot-ui-with-example-data">Get Started</a>
                            </li>
                            <li id="menu-item-5" class="menu-item menu-item-type-custom menu-item-object-custom menu-item-5">
                                <a target="_blank" href="https://github.com/apache/incubator-spot.git">GitHub</a>
                            </li>
                            <li id="menu-item-130" class="menu-item menu-item-type-custom menu-item-object-custom menu-item-130 <?php if($currentPage =='contribute'){echo 'active';}?>">
                                <a target="_blank" href="<?php echo BASE_URL; ?>/contribute" target="_blank">Contribute</a>
                            </li>
                            <li id="menu-item-106" class="menu-item menu-item-type-custom menu-item-object-custom menu-item-106">
                                <a target="_blank" href="<?php echo BASE_URL; ?>/doc">Documentation</a>
                            </li>
                            <li class="menu-item menu-item-has-children <?php if( in_array($currentPage, array('project-components','visualizations','machine-learning','open-data-models','ingestion'), true )) {echo 'active';}?>">
                                <a href="#">Project Components</a>
                                <ul class="sub-menu">
                                	<li class="<?php if($currentPage =='open-data-models'){echo 'active';}?>"><a href="<?php echo BASE_URL; ?>/project-components/open-data-models">Open Data Models</a></li>
                                	<li class="<?php if($currentPage =='ingestion'){echo 'active';}?>"><a href="<?php echo BASE_URL; ?>/project-components/ingestion">Ingestion</a></li>
                                	<li class="<?php if($currentPage =='machine-learning'){echo 'active';}?>"><a href="<?php echo BASE_URL; ?>/project-components/machine-learning">Machine Learning</a></li>
                                	<li class="<?php if($currentPage =='visualization'){echo 'active';}?>"><a href="<?php echo BASE_URL; ?>/project-components/visualization">Visualization</a></li>
                                </ul>
                            </li>
                            <li id="menu-item-13" class="menu-item menu-item-type-post_type menu-item-object-page menu-item-13 <?php if($currentPage =='blog'){echo 'active';}?>">
                                <a href="<?php echo BASE_URL; ?>/blog">Blog</a>
                            </li>
                        </ul>
                    </nav>

                </div>

            </header>

            <div id="mobile-nav"></div>