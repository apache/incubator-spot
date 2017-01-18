<?php include '../../includes/config.php'; ?>
<?php
$bodyclass = "single single-post";
$pageTitle = "Apache Spot (Incubating): Three Most-Asked Questions - Apache Spot";
$description = "";
$currentPage = 'blog';
?>
<?php include '../../includes/header.php'; ?>

            <div id="content">

                <div id="inner-content" class="wrap cf">

                    <main id="main" class="m-all t-2of3 d-5of7 cf" role="main" itemscope itemprop="mainContentOfPage" itemtype="http://schema.org/Blog">

                        <article id="post-62" class="cf post-62 post type-post status-publish format-standard hentry category-security-analytics tag-github tag-open-network-insight tag-open-source" role="article" itemscope itemprop="blogPost" itemtype="http://schema.org/BlogPosting">

                            <header class="article-header entry-header">

                                <h1 class="entry-title single-title" itemprop="headline" rel="bookmark">Apache Spot (Incubating): Three Most-Asked Questions</h1>

                                <p class="byline entry-meta vcard">

                                    <time class="updated entry-time" datetime="2016-03-29" itemprop="datePublished">
                                        March 29, 2016
                                    </time>
                                    </span>
                                </p>

                            </header>
                            <section class="entry-content cf" itemprop="articleBody">
                                <p>
                                    While this is not the first blog post about Apache Spot, it is the first one by a creator of the solution. As a security data scientist in Intel&#8217;s Data Center Group, I joined a small team to start thinking about solving really hard problems in cloud analytics. The team grew, and out of that effort, came Apache Spot. Since we started talking about the project, these are the three questions I am asked the most.
                                </p>
                                <p>
                                    <strong>What Is Apache Spot?</strong>
                                    <br />
                                    Apache Spot is an open source, flow and packet analytics solution built on Hadoop. It combines big data processing, at-scale machine learning, and unique security analytics to put potential threats in front of defenders. While I am a data scientist today, I was a security investigator just a few years ago. I wanted to develop a solution that would put new tools and technology in play for defenders, but without requiring them to walk away from security and get a math degree.
                                </p>
                                <p>
                                    We wanted to start with the hard problems, so we looked at the emerging need to analyze data that was produced at a scale outside what a lot of security solutions could handle. The data is being created today, and lack of visibility into that data gives attackers a profound advantage. Also, in this new era of security, many defenders (public and private sector) have to answer to their citizens and customers when these threats occur. In other words, an event that says &#8220;this attack was blocked&#8221; is insufficient; an organization needs to see what happened before, during, and after a particular machine was attacked at a particular time. The problem is summarized in a slide from a <a href="http://www.youtube.com/watch?v=mOZjMuBLYyM" target="_blank">FloCon talk</a>
                                    <br />
                                    <a href="<?php echo BASE_URL; ?>/library/images/FloCon2015.png" rel="attachment wp-att-66"><img class="aligncenter size-full wp-image-66" src="<?php echo BASE_URL; ?>/library/images/FloCon2015.png" alt="open source packet and flow analytics" /></a>
                                </p>
                                <p>
                                    The gist is that while processing is a challenge at higher scales, the amount of insight gained is higher when analyzing flows and packets from key protocols (like DNS). And that&#8217;s how we got here.
                                </p>
                                <p>
                                    <strong>Why Intel?</strong>
                                </p>
                                <p>
                                    At Intel, I have worked in IT, for a security product company (McAfee), and in the Data Center Group. Intel IT was an early pioneer of the concept of proactive investigations to protect intellectual property. McAfee (now Intel Security Group) has a broad customer base in the realms of network, endpoint, and content security, to name only a few. And the Intel Data Center group has strategic partnerships with Cloudera and Accenture, as well as some pretty cool analytics efforts of their own. Add the performance benefits we achieve with Intel Architecture, especially the Intel MPI Library and Intel Math Kernel Library, and it certainly makes sense to me.
                                </p>
                                <p>
                                    <strong>Why Open Source?</strong>
                                </p>
                                <p>
                                    I learned from my earlier efforts in security analytics, that to invite collaboration from academia, the public sector, and the private sector, open source software is an excellent choice. We are now seeking to build a community of developers, data scientists, and security enthusiasts to grow Apache Spot into something we can all be proud of. We have also chosen an Apache software license, so that it can enrich commercial software offerings as well.
                                </p>
                                <p>
                                    The greatest thing for me since we announced at RSA is to hear OTHER people talk about Apache Spot (formerly Open Network Insight or ONI), here are some of my favorites, from <a href="http://vision.cloudera.com/open-network-insight-changing-infosec-data-science-forever/" target="_blank">a Data Scientist @ eBay </a>, <a href="https://newsroom.accenture.com/news/accenture-introduces-the-accenture-cyber-intelligence-platform-to-help-organizations-continuously-predict-detect-and-combat-cyber-attacks.htm" target="_blank">a Security Provider</a>, and <a href="http://vision.cloudera.com/adaptive-security-at-big-data-scale-for-next-generation-digital-security/" target="_blank">a Big Data company</a>.
                                </p>
                                <p>
                                    Fork us on Github!
                                </p>
                                <p>
                                    Grant Babb
                                </p>
                            </section>
                            <footer class="article-footer">

                                filed under: <a href="../../category/security-analytics/" rel="category tag">Security Analytics</a>
                                <p class="tags">
                                    <span class="tags-title">Tags:</span><a href="../../tag/github/" rel="tag">github</a>, <a href="../../tag/open-network-insight/" rel="tag">open network insight</a>, <a href="../../tag/open-source/" rel="tag">open source</a>
                                </p>
                            </footer>

                        </article>

                    </main>

					<?php include '../../includes/sidebar.php'; ?>

                </div>

            </div>

            
			<?php include '../../includes/footer.php'; ?>

    </body>

</html>
<!-- end of site. what a ride! -->
