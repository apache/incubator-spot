<?php include '../../includes/config.php'; ?>
<?php
$bodyclass = "single single-post";
$pageTitle = "How Apache Spot (Incubating) Helps Create Well-Stocked Data Lakes and Catch Powerful Insights - Apache Spot";
$description = "";
$currentPage = 'blog';
?>
<?php include '../../includes/header.php'; ?>

            <div id="content">

                <div id="inner-content" class="wrap cf">

                    <main id="main" class="m-all t-2of3 d-5of7 cf" role="main" itemscope itemprop="mainContentOfPage" itemtype="http://schema.org/Blog">

                        <article id="post-113" class="cf post-113 post type-post status-publish format-standard hentry category-uncategorized" role="article" itemscope itemprop="blogPost" itemtype="http://schema.org/BlogPosting">

                            <header class="article-header entry-header">

                                <h1 class="entry-title single-title" itemprop="headline" rel="bookmark">How Apache Spot (Incubating) Helps Create Well-Stocked Data Lakes and Catch Powerful Insights</h1>

                                <p class="byline entry-meta vcard">

                                    <time class="updated entry-time" datetime="2016-08-08" itemprop="datePublished">
                                        August 8, 2016
                                    </time>
                                    </span>
                                </p>

                            </header>
                            <section class="entry-content cf" itemprop="articleBody">
                                <p>
                                    About four years ago, the era of the Big Data analytics began. Paired with advanced analytics, massive volumes of data can be culled to not only inform critical decisions, but also to simulate sophisticated “what if” scenarios that allow companies to gain competitive advantages by generating and predicting different scenarios. For example, a financial services company can more accurately determine what other products to offer a customer, and in what order, based on a wide variety of data, then use advanced analytics to gather insights. Creating a data lake that can be effectively used for predictive analytics raises tough questions — what data sources should we use?  How should this data be collected and ingested? What are the best algorithms to analyze the data, and how should we present these results to our decision maker?
                                </p>
                                <p>
                                    Apache Spot can help to solve most of these issues. Following is a description of the Apache Spot, which is designed to facilitate Big Data analytics scenarios like the financial services company’s question about the right product to offer customers.
                                </p>
                                <a href="<?php echo BASE_URL; ?>/library/images/ONI_Architecture-Diagram_1300_v4.png"><img src="<?php echo BASE_URL; ?>/library/images/ONI_Architecture-Diagram_1300_v4.png" alt="oni_architecture-diagram_1300_v4" /></a>
                                <h3><strong>Apache Spot Core Components</strong></h3>
                                <p>
                                    The Apache Spot Core is composed of three main components — data integration (collectors), data store (HDFS here, but can also be a non-SQL database) and machine learning.
                                </p>
                                <p>
                                    In this diagram, the top left shows Apache Spot Data Sources, which include the collection of the information that will be used to create a data lake. The process is simple. Define a pull or push from the source of information then capture this information on Apache Spot’s “collectors.” The collectors are processes that interpret the information that is sent, then write it to the HDFS system in the Apache Spot cluster. The HDFS stores the data lake and ensures that resources can grow while remaining economical at every size. The Apache Spot algorithms are part of machine learning and are used to detect the uncommon information in the data lake.
                                </p>
                                <h3><strong>Operational Analytics</strong></h3>
                                <p>
                                    As part of operational analytics, Apache Spot executes different batch processes that add information to machine learning results to provide meaning and context. Using the financial services product example, basic customer data could be augmented with information about other customers in the same region along with information about which products those customers recommended or complained about. Basically, the data scientists can “play” with the data using different algorithms to identify insights.
                                </p>
                                <h3><strong>Visualizing Results</strong></h3>
                                <p>
                                    The Apache Spot GUI displays the results that the machine learning algorithms generate. Results are represented such that it is easy to identify both the most common things as well as find the most suspicious or uncommon information that is part of the data lake.
                                </p>
                                <h3><strong>Customizable Open Source</strong></h3>
                                <p>
                                    Because Apache Spot is an open-source project, most of the components depicted here can be modified by the end user.
                                </p>
                            </section>
                            <footer class="article-footer">

                                filed under: <a href="../../category/uncategorized/" rel="category tag">Uncategorized</a>

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
