<?php include '../../includes/config.php'; ?>
<?php
$bodyclass = "single single-post";
$pageTitle = "Apache Spot (Incubating) and Cybersecurity — Using NetFlows to Detect Threats to Critical Infrastructure - Apache Spot";
$description = "";
$currentPage = 'blog';
?>
<?php include '../../includes/header.php'; ?>

            <div id="content">

                <div id="inner-content" class="wrap cf">

                    <main id="main" class="m-all t-2of3 d-5of7 cf" role="main" itemscope itemprop="mainContentOfPage" itemtype="http://schema.org/Blog">

                        <article id="post-117" class="cf post-117 post type-post status-publish format-standard hentry category-cybersecurity" role="article" itemscope itemprop="blogPost" itemtype="http://schema.org/BlogPosting">

                            <header class="article-header entry-header">

                                <h1 class="entry-title single-title" itemprop="headline" rel="bookmark">Apache Spot (Incubating) and Cybersecurity — Using NetFlows to Detect Threats to  Critical Infrastructure</h1>

                                <p class="byline entry-meta vcard">

                                    <time class="updated entry-time" datetime="2016-08-08" itemprop="datePublished">
                                        August 8, 2016
                                    </time>
                                    </span>
                                </p>

                            </header>
                            <section class="entry-content cf" itemprop="articleBody">
                                <p>
                                    The “first” documented cybersecurity case was the worm replication, which was initiated by Robert T. Morris on November 2, 1988. Wow! Here we are in 2016, 28 years later, with viruses and worms giving way to Trojan horses and polymorphic code. Nowadays, we are also fighting against DDoS, phishing, spear phishing attacks, command and controls along with APTs such as Aurora, Zeus, Red October and Stuxnet. What happened with our security controls on each attack?
                                </p>
                                <p>
                                    Despite heroic efforts, internal and external security controls, no matter if they are preventive, detective or corrective, can be bypassed by different situations or misconfigurations. Capabilities to detect bugs or vulnerabilities in code, protocol, etc. are still limited. When we consider how to close these gaps, we have two options:
                                </p>
                                <ol>
                                    <li>
                                        Collect information from each device that is part of the environment.
                                    </li>
                                    <li>
                                        Collect information from the critical infrastructure that is used for most, if not all, of the systems.
                                    </li>
                                </ol>
                                <p>
                                    This blog will describe the second approach.
                                </p>
                                <p>
                                    Critical infrastructure, including nationally significant infrastructure, can be broadly defined as the systems, assets, facilities and networks that provide essential services. For nations, this means protecting the national security, economic security and prosperity as well as the health and safety of their citizenry. If we extrapolate this definition on the IT enterprise environments, we can define the critical infrastructure as the service, or services, that need to be up and running properly 99.99999% of the time, most of the time to support other critical infrastructure, such as databases, HR systems, manufacturing systems, etc.
                                </p>
                                <p>
                                    Effectively protecting critical infrastructure means that millions, if not billions, of different scenarios must be identified and monitored. To do this, the cybersecurity problem must be broken into small pieces.
                                </p>
                                <p>
                                    Let’s consider DNS — your communications to the Web. How can you know which communications are being established by your critical servers? And, what if you want to do it for most of your infrastructure?
                                </p>
                                <p>
                                    First idea: Use NetFlow, which is a network protocol that helps us collect IP traffic information and monitor network traffic. NetFlow has the details on the communications of all of your network traffic. However, the normal data on an enterprise environment includes billions of NetFlow events per day. To use this data to identify issues, it must be stored and analyzed. Storage alone is costly. Analyzing what amount Big Data stores is an entire other challenge.
                                </p>
                                <p>
                                    Apache Spot offers a solution. It was designed to gather, store and analyze Big Data. In fact, Apache Spot is an ideal solution for this cybersecurity challenge. Apache Spot can integrate many different data sources in a data lake then add operational context to the data by linking configuration, inventory, service databases and other data stores. This helps you to prioritize the actions to take under different attack, malware, APT and hacking scenarios. With Apache Spot, attacks that bypass our external or internal security controls can be identified. By delivering risk-prioritized, actionable insights, Apache Spot can support the growing need for security analytics.
                                </p>
                                <p>
                                    Not only can Apache Spot collect, store and analyze billions of NetFlow packets, but it can also be adapted to meet the unique requirements of your organization. How? Apache Spot is an open-source project.
                                </p>
                                <p>
                                    <strong>But Wait, There’s More</strong>
                                </p>
                                <p>
                                    Check out “<a href="../how-apache-spot-helps-create-well-stocked-data-lakes-and-catch-powerful-insights/"><u>How Apache Spot Helps Create Well-Stocked Data Lakes and Catch Powerful Insights</u></a>” to learn more about the underlying Apache Spot architecture.
                                </p>
                                <p>
                                    This is the first of a series of blogs that we will be writing about cybersecurity, so check back to read more.
                                </p>
                            </section>
                            <footer class="article-footer">

                                filed under: <a href="../../category/cybersecurity/" rel="category tag">Cybersecurity</a>

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
