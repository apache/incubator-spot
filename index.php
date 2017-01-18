<?php include 'includes/config.php'; ?>

<?php
$bodyclass = "home";
$pageTitle = "Apache Spot";
$description = "";
$currentPage = 'home';
?>
<?php include 'includes/header.php'; ?>


            <div id="masthead">
                <div class="wrap cf">
                    <div class="m-all t-1of2 d-1of2">
                        <h1>Apache Spot (Incubating)</h1>
                        <h1 class="thin">A Community Approach to Fighting Cyber Threats</h1>
                    </div>
                    <div class="m-all t-1of2 d-1of2 contribute">
                        <h3>Help Spot expand his hunting ground to see deeper into the darkness of cyber threats.</h3>
                        <p class="btn-margin"><a href="<?php echo BASE_URL; ?>/contribute" class="y-btn">Become a Contributor</a></p>
                    </div>
                </div>
            </div>
            
            <div id="at-a-glance">
            	<div class="wrap cf">
            		<h1>Apache Spot at a Glance</h1>
            		<p>Apache Spot is a community-driven cybersecurity project, built from the ground up, to bring advanced analytics to all IT Telemetry data on an open, scalable platform. Spot expedites threat detection, investigation, and remediation via machine learning and consolidates all enterprise security data into a comprehensive IT telemetry hub based on open data models. Spot’s scalability and machine learning capabilities support an ecosystem of ML-based applications that can run simultaneously on a single, shared, enriched data set to provide organizations with maximum analytic flexibility. Spot harnesses a diverse community of expertise from Centrify, Cloudera, Cybraics, Endgame, Intel, Jask, Streamsets, and Webroot.</p>
            	</div>
            </div>

            <div id="content">
                <div id="inner-content" class="wrap cf">
                    <main id="main" class="m-all t-all d-all cf" role="main" itemscope itemprop="mainContentOfPage" itemtype="http://schema.org/Blog">
                        <h1 class="center">Apache Spot Advantages</h1>
                        <p style="text-align: center;"><img src="library/images/advantages-1.png" alt="advantages" width="958" height="433" class="aligncenter size-full" />
                        </p>
                    </main>
                </div>
            </div>

            <div id="and">
                <div class="wrap cf">
                    <div class="and-row">
                        <div class="and-cell">
                            <img src="library/images/and.png" alt="" />
                        </div>
                        <div class="and-cell">
                            <p>
                                Apache Spot is functional after just one day and just keeps improving through feedback and machine learning.
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <div id="how-it-works">
                <div class="wrap cf">
                    <div class="m-all t-1of3 d-1of3">
                        <h1>How It Works</h1>
                        <p>
                            Apache Spot uses machine learning as a filter for separating bad traffic from benign and to characterize the unique behavior of network traffic. A proven process, of context enrichment, noise filtering, whitelisting and heuristics, is also applied to network data to produce a shortlist of most likely security threats.
                        </p>
                        <p class="btn-margin">
                            <a href="https://github.com/apache/incubator-spot" class="y-btn">More Info</a>
                        </p>
                    </div>
                    <div class="m-all t-2of3 d-2of3">
                        <img src="library/images/how-it-works.png" alt="" />
                    </div>
                </div>
            </div>

            <div id="key-features">
                <div class="wrap cf">
                    <h1>Key Features</h1>
                    <div class="m-all t-1of2 d-1of2">
                        <div class="table">
                            <div class="table-row">
                                <div class="table-cell">
                                    <img src="library/images/magnify-icon.png" alt="" />
                                </div>
                                <div class="table-cell">
                                    <h3>Suspicious DNS packets</h3>
                                </div>
                            </div>
                        </div>
                        <p>
                            Apache Spot is capable of performing deep-packet inspection of DNS traffic to build a profile of probable and improbable DNS payloads. After visualizing, normalizing, and conducting pattern searches, the analyst has a shortlist of the most likely threats present in DNS traffic.
                        </p>
                    </div>
                    <div class="m-all t-1of2 d-1of2">
                        <div class="table">
                            <div class="table-row">
                                <div class="table-cell">
                                    <img src="library/images/threat-icon.png" alt="" />
                                </div>
                                <div class="table-cell">
                                    <h3>Threat Incident and Response</h3>
                                </div>
                            </div>
                        </div>
                        <p>
                            Given an IP address, Apache Spot gathers all the characteristics about the communication associated with it – the “social network” of that IP address.  Then Apache Spot builds a timeline of the conversations that originated with that IP.
                        </p>
                    </div>
                    <div class="m-all t-1of2 d-1of2">
                        <div class="table">
                            <div class="table-row">
                                <div class="table-cell">
                                    <img src="library/images/connects.png" alt="" />
                                </div>
                                <div class="table-cell">
                                    <h3>Suspicious Connects</h3>
                                </div>
                            </div>
                        </div>
                        <p>
                            Apache Spot uses advanced machine learning to build a model of the machines on the network and their communication patterns.  The connections between the machines that are the lowest probability are then visualized, filtered for noise, and searched for known patterns.  The result is the most likely threat patterns in the data, a few hundred flows picked from billions.
                        </p>
                    </div>
                    <div class="m-all t-1of2 d-1of2">
                        <div class="table">
                            <div class="table-row">
                                <div class="table-cell">
                                    <img src="library/images/storyboard-icon.png" alt="" />
                                </div>
                                <div class="table-cell">
                                    <h3>Storyboard</h3>
                                </div>
                            </div>
                        </div>
                        <p>
                            After an analyst has investigated a threat, the need still exists to communicate the event up and across the organization.  A “dashboard” gives quick answers to the questions you already know to ask.  What the analyst requires is a “storyboard,” something that tells who, what, where, and how of the story in words and interactive visualizations.
                        </p>
                    </div>
                    <div class="m-all t-1of2 d-1of2">
                        <div class="table">
                            <div class="table-row">
                                <div class="table-cell">
                                    <img src="library/images/SPOT_OpenDataModel-Icon_v1.png" alt="" />
                                </div>
                                <div class="table-cell">
                                    <h3>Open Data Models</h3>
                                </div>
                            </div>
                        </div>
                        <p>
                            Spot provides common open data model for network, endpoint, and user – Open Data Models. These Open Data Models provide a standard format of enriched event data that makes it easier to integrate cross application data to gain complete enterprise visibility and develop net new analytic functionality.  Spot’s Open Data Models helps organizations quickly share new analytics with one another as new threats are discovered.
                        </p>
                    </div>
                    <div class="m-all t-1of2 d-1of2">
                        <div class="table">
                            <div class="table-row">
                                <div class="table-cell">
                                    <img src="library/images/SPOT_Collabration-Icon_v2.png" alt="" />
                                </div>
                                <div class="table-cell">
                                    <h3>Collaboration</h3>
                                </div>
                            </div>
                        </div>
                        <p>
                            Spot’s Open Data Models help organizations quickly share new analytics with one another as new threats are discovered.  And, with Hadoop, organizations able to run these analytics against comprehensive historic data sets, helping organizations identify past threats that have slipped through the cracks. With this capability, Spot aims to give security professionals the ability to collaborate like cybercriminals do.
                        </p>
                    </div>

                    <div class="cf"></div>
                    <p class="btn-margin">
                        <a href="https://github.com/apache/incubator-spot" class="y-btn">More Info</a>
                    </p>
                </div>
            </div>
            
            <div id="open-data-models">
            	<div class="wrap cf">
            		<h1 class="center">Apache Spot Open Data Models (ODM)</h1>
            		<div class="m-all t-1of2 d-1of2 center">
            			<img src="library/images/odm.png" alt="Apache Spot Open Data Models">
            		</div>
            		<div class="m-all t-1of2 d-1of2">
            			<p>The primary use case initially supported by Spot includes Network Traffic Analysis for network flows (Netflow, sflow, etc.), DNS and Proxy.  The Spot open data model strategy aims to extend Spot capabilities to support a broader set of cybersecurity use cases.</p>
            			<h3>ODM at a Glance</h3>
            			<ul>
            				<li>Includes a growing catalog of packaged ingestion pipelines for common data sources</li>
            				<li>Enriched events provide full context leading to better analytics and faster incident response</li>
            				<li>Organizations maintain and control a single copy of their security data</li>
            			</ul>
            			<p class="btn-margin"><a href="<?php echo BASE_URL; ?>/project-components/open-data-models/" class="y-btn">Read More</a></p>
            		</div>
            	</div>
            </div>

            <div id="user-stories">
                <div class="wrap cf">
                    <h1>Spot Fosters a Rich Application Ecosystem</h1>
                    <p class="btn-margin center">
                        Spot accelerates the development of cybersecurity applications by providing a cybersecurity analytics framework.  This means more solutions can be created faster. This is because Spot allows organizations to focus developing the analytics and visualizations for applications that discover cybercrime rather than spending time building systems to ingest, integrate, store, and process myriad volumes or varieties of security data.
                    </p>
                    
                    <p class="center">Join the Apache Spot community and collaborate with us using a common framework.</p>
                    
                    <div class="community">
                    	<img src="library/images/community/endgame.png" alt="Endgame" />
                    	<img src="library/images/community/intel.png" alt="Intel" />
                    	<img src="library/images/community/webroot.png" alt="Webroot" />
                    	<img src="library/images/community/jask.png" alt="Jask" />
                    	<img src="library/images/community/cloudera.png" alt="Cloudera" />
                    	<img src="library/images/community/cloudwick.png" alt="Cloudwick" />
                    	<img src="library/images/community/cybraics.png" alt="Cybraics" />
                    	<img src="library/images/community/centrify.png" alt="Centrify" />
                    	<img src="library/images/community/ebay.png" alt="Ebay" />
                    	<img src="library/images/community/semantix.png" alt="Ebay" />
                    	<img src="library/images/community/rich-it.png" alt="Ebay" />
                    	
                    </div>

                </div>
            </div>

            <div id="use-case">
                <div class="wrap cf">

                    <h1>Use Case</h1>
                    <div class="quotes">

                        <div class="table">
                            <div class="table-row">
                                <div class="table-cell quote-man">
                                    <img src="library/images/quote-person.png" alt="" />
                                    <h3>Senior Analyst</h3>
                                </div>
                                <div class="table-cell">
                                    <p>
                                        ...that allows me to see and customize the data and scripts to my enviroment. I want control over how the solution works.”
                                    </p>
                                </div>
                            </div>
                        </div>

                        <div class="table">
                            <div class="table-row">
                                <div class="table-cell quote-man">
                                    <img src="library/images/quote-person.png" alt="" />
                                    <h3>Junior Analyst</h3>
                                </div>
                                <div class="table-cell">
                                    <p>
                                        ...that automatically alerts me to actionable suspicious events and ways to optimize my network in a timely fashion. Help me investigate these events and tell the story across stakeholders in my organization.”
                                    </p>
                                </div>
                            </div>
                        </div>

                        <div class="table">
                            <div class="table-row">
                                <div class="table-cell quote-man">
                                    <img src="library/images/quote-person.png" alt="" />
                                    <h3>Decision Maker</h3>
                                </div>
                                <div class="table-cell">
                                    <p>
                                        ...tells me the story of what happened in a way I can understand so I can make decisions as a result.”
                                    </p>
                                </div>
                            </div>
                        </div>

                    </div>

                </div>

                <div class="arrow">
                    <div class="wrap cf">
                        <p>
                            Identify the needle in the haystack with <strong>patterns</strong> that provide insight into potential threats.
                        </p>
                    </div>
                </div>
            </div>
            
<?php include 'includes/prefooter.php'; ?>
<?php include 'includes/footer.php'; ?>