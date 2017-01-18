<?php include '../../includes/config.php'; ?>
<?php
$bodyclass = "single single-post";
$pageTitle = "Jupyter Notebooks for Data Analysis - Apache Spot";
$description = "";
$currentPage = 'blog';
?>
<?php include '../../includes/header.php'; ?>

            <div id="content">

                <div id="inner-content" class="wrap cf">

                    <main id="main" class="m-all t-2of3 d-5of7 cf" role="main" itemscope itemprop="mainContentOfPage" itemtype="http://schema.org/Blog">

                        <article id="post-136" class="cf post-136 post type-post status-publish format-standard hentry category-uncategorized" role="article" itemscope itemprop="blogPost" itemtype="http://schema.org/BlogPosting">

                            <header class="article-header entry-header">

                                <h1 class="entry-title single-title" itemprop="headline" rel="bookmark">Jupyter Notebooks for Data Analysis</h1>

                                <p class="byline entry-meta vcard">

                                    <time class="updated entry-time" datetime="2016-09-22" itemprop="datePublished">
                                        September 22, 2016
                                    </time>
                                    </span>
                                </p>

                            </header>
                            <section class="entry-content cf" itemprop="articleBody">
                                <p>
                                    <strong>Why Does Apache Spot Include iPython notebooks? </strong>
                                </p>
                                <p>
                                    The project team wants Apache Spot to be a versatile tool that can be used by anyone. This means that data scientists and developers need to be able to query and handle the source data to find all the information they need for their decision making. The iPython Notebook is an appropriate platform for easy data exploration. One of its biggest advantages is that it provides parallel and distributed computing to enable code execution and debugging in an interactive environment – thus the ‘i’ in iPython.
                                </p>
                                <p>
                                    The iPython notebook is a web based interactive computational environment that provides access to the Python shell. While iPython notebooks were originally designed to work with the Python language, they support a number of other programming languages, including Ruby, Scala, Julia, R, Go, C, C++, Java and Perl. There are also multiple additional packages that can be used to get the most out of this highly-customizable tool.
                                </p>
                                <p>
                                    Starting on version 4.0, most notebook functionalities are now part of the Project Jupyter, while iPython remains as the kernel to work with Python code in the notebooks.
                                </p>
                                <img src="<?php echo BASE_URL;?>/library/images/iPython-1.png" alt="ipython" class="aligncenter size-full wp-image-140" />
                                <p>
                                    <strong>IPython with Apache Spot for Network Threat Detection</strong>
                                </p>
                                <p>
                                    <em>NOTE:  This is not intended to be a step-by-step tutorial on how to code a threat analysis in Apache Spot, but more like an introduction on how to approach the suspicions of a security breach.</em>
                                </p>
                                <p>
                                    Although machine learning (ML) will do most of the work detecting anomalies in the traffic, Apache Spot also includes two notebook templates that can get you started on this. The <em>Threat_Investigation_master.ipynb</em> is designed to query the raw data table to find all connections in a day that are related to any threat you select – even connections that were not necessarily flagged as suspicious by ML on a first run. This gives us the chance to get a new data subset and here is where the fun begins.
                                </p>
                                <p>
                                    If you suspect of a specific type of attack in your network, you can get the whole story by answering the Five ‘W’s.
                                </p>
                                <p>
                                    <strong><em>What? </em></strong>
                                </p>
                                <p>
                                    Maybe there’s been an increase in the logs collected by the system, which indicates abnormal amounts of communication in your network. Or, the amount of POST requests in your network have risen overnight. This is the mystery that needs to be solved by researching through the anomalies previously detected by ML.
                                </p>
                                <p>
                                    <strong><em>Who?</em></strong>
                                </p>
                                <p>
                                    Assuming you have a network context, you can identify the name of the infected machine inside the network, as well as the name of the IP or DNS on the other side of the connection (if it is a known host). If you don’t have a network context or are using DHCP, this can be a little tricky to detect using only Netflow logs. But, that’s where DNS and Proxy logs, come in handy. Including a network context file with Apache Spot is really simple and can go a long way when identifying a threat.
                                </p>
                                <p>
                                    <strong><em>When?</em></strong>
                                </p>
                                <p>
                                    To have a broader visibility on the attack, you can customize the queries on the Threat investigation notebook to review the data through a wider time lapse – instead of just checking through the current day. With this, you could find an increase of a certain type of requests to one (or many) URIs and predict its future behavior.
                                </p>
                                <p>
                                    <strong><em>Where?</em></strong>
                                </p>
                                <p>
                                    When working only with DNS, having a destination URL might not say much about where your information is going to, but Apache Spot allows you to connect with a geolocation database to identify the location of the suspected attackers IP. Taking advantage of this option, you can visually locate the other end of the connection on a map. You might find that it’s pointing to a country banned by your company, indicating a leak.
                                </p>
                                <p>
                                    <strong><em>Why?</em></strong>
                                </p>
                                <p>
                                    This answer to “why” will depend highly on the result of the analysis. For instance, an excessive amount of POST requests from one machine inside the network to an unidentified URI can indicate a data mining attack. Tracing back to patient zero, you can find that this could have originated with a phishing email, malicious software installed by an employee or a one-time visitor’s infected machine that connected to your network.
                                </p>
                                <p>
                                    <strong>How to Get Answers to the Five Ws Questions</strong>
                                </p>
                                <p>
                                    All of the previous questions can be answered by looking at the raw data collected. Although performing elaborated queries directly to your database can seem tempting, this type of analysis with Hive, or even Impala, can be very time consuming. A better approach would be to use Pandas to read and transform your dataset into a relational structured dataframe. This lets you work with as if it were an offline structured relational database.
                                </p>
                                <p>
                                    Once you have your desired results and data subsets, you can use MatplotLib to easily graph your findings. (We cover this subject in more depth in another post.) Another advantage of the notebook is that you can download it as HTML or a PDF file to store locally and use it in a presentation – or just keep it for future reference.
                                </p>
                                <p>
                                    <strong>Wrap Up</strong>
                                </p>
                                <p>
                                    This post was meant to be just a brief introduction of how you can use iPython notebooks in Apache Spot to perform further data analysis and include it our executive report (in addition to the already included Story board). Although this is not the only way you can do this, it is a very interactive and fun way to do it. You’ll also see that the overall processing time is very short – thanks to the iPython notebook task parallelism ability.
                                </p>
                                <p>
                                    We want to hear from YOU! Have you used iPython notebooks before? How do you feel about having this tool in Apache Spot? If you’re interested in further data analysis through interactive charts, a new post is coming soon on D3 and jQuery data visualization. Also, check back soon to read more on this and other Cybersecurity subjects.
                                </p>
                            </section>
                            
                            <footer class="article-footer">
            
                              filed under: <a href="../../category/data-science/" rel="category tag">Data Science</a>, <a href="../../category/ipython-notebooks/" rel="category tag">Ipython Notebooks</a>, <a href="../../category/threat-analysis-tools/" rel="category tag">Threat Analysis Tools</a>
                              
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
